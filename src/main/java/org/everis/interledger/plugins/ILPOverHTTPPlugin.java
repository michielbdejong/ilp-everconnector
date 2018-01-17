package org.everis.interledger.plugins;

// REF 1: https://github.com/interledger/rfcs/pull/349
// REF 2: ILP-over-HTTP JS Implementation: https://github.com/michielbdejong/ilp-plugin-http/blob/master/index.js

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;
import io.vertx.core.*;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import org.everis.interledger.config.plugin.ILPOverHTTPConfig;
import org.interledger.InterledgerAddress;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.interledger.InterledgerPacketType;
import org.interledger.InterledgerProtocolException;
import org.interledger.ilp.InterledgerProtocolError;


/**
 * Entity of Plugin to connect any sender, receiver or connector with a remoteLedgerAdaptor.
 */
// TODO:(0) Rename as BaseBTPPlugin


public class ILPOverHTTPPlugin extends BasePlugin {

    public class HTTPSServer extends AbstractVerticle {
        final IRequestHandler requestHandler;
        final String verticleID;
        final String listeningHost;
        final int listeningPort;
        final String tls_key_path;
        final String tls_crt_path;
        final String remote_host;
        final int    remote_port;

        // REF 3: VertX HTTPS server: https://github.com/vert-x3/vertx-examples/blob/master/core-examples/src/main/java/io/vertx/example/core/http/https/Server.java
        public HTTPSServer(
                IRequestHandler requestHandler,
                String verticleID,
                String listeningHost,
                int listeningPort,
                String tls_key_path,
                String tls_crt_path,
                String remote_host,
                int    remote_port
        ) {
            this.requestHandler = Objects.requireNonNull(requestHandler);
            this.verticleID     = Objects.requireNonNull(verticleID    );
            this.listeningHost  = Objects.requireNonNull(listeningHost );
            this.listeningPort  = Objects.requireNonNull(listeningPort );
            this.tls_key_path   = Objects.requireNonNull(tls_key_path  );
            this.tls_crt_path   = Objects.requireNonNull(tls_crt_path  );
            this.remote_host    = Objects.requireNonNull(remote_host   );
            this.remote_port    = remote_port;
        }

        private File _getCWD()  {
            return Paths.get(".").toAbsolutePath().normalize().toFile();
        }

        private io.vertx.core.buffer.Buffer _readRelativeFile(String fileName) {
            final File cwd  = _getCWD();
            final String sCanonicalPath;
            try {
                sCanonicalPath = new File(cwd, fileName).getCanonicalPath();
            } catch (IOException e) {
                throw new RuntimeException("error "+e.toString()+" while trying to read "+fileName);
            }
            final io.vertx.core.buffer.Buffer fileBuffer  = vertx.fileSystem().readFileBlocking(sCanonicalPath);
            return fileBuffer;
        }

        private HttpServerOptions _getHTTPServerOptions() {
            final HttpServerOptions serverOptions = new HttpServerOptions().
                    setHost(listeningHost).
                    setPort(listeningPort);
            final boolean useSSL = true;
            if (useSSL) {
                serverOptions.setSsl(true);
                serverOptions.setPemKeyCertOptions( //Assume PEM encoding
                        new PemKeyCertOptions()
                                .setKeyValue(_readRelativeFile(tls_key_path))
                                .setCertValue(_readRelativeFile(tls_crt_path))
                );
                // TODO:(0?) Use key Store with password protection
                // .setKeyStoreOptions(
                //    new JksOptions().
                //        setPath("server-keystore.jks").
                //        setPassword("wibble") )
            }
            return serverOptions;
        }

        public void run() {
            VertxOptions options = new VertxOptions();

            MessagePassingQueue.Consumer<Vertx> runner = vertx -> {
                try {
                    vertx.deployVerticle(verticleID);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            };

            Vertx vertx = Vertx.vertx(options);
            runner.accept(vertx);
        };

        @Override
        public void start() throws Exception {
            HttpServer server = vertx.createHttpServer( _getHTTPServerOptions() );

            server.requestHandler(req -> {
                MultiMap headers = req.headers();
                InterledgerAddress destination = InterledgerAddress.of(headers.get("ilp-destination"));
                String Base64ExecCondition = headers.get("ilp-condition");
                Instant expiresAt = Instant.from(ZonedDateTime.parse(headers.get("ilp-expiry")));
                String amount = headers.get("ilp-amount");
                ByteBuffer endToEndData = null /* TODO:(0) Add body as endToEndData*/;
                CompletableFuture<IRequestHandler.ILPResponse> ilpResponse =
                        this.requestHandler.onRequestReceived(
                                destination, Base64ExecCondition, expiresAt, amount, endToEndData);
                final HttpServerResponse response = req.response();
                final MultiMap res_headers = response.headers();
                boolean retry;
                do {
                    retry = false;
                    try {
                        // TODO:(0) Blocking call!! exec in thread pool
                        IRequestHandler.ILPResponse ilpRespone = ilpResponse.get();
                        if (ilpRespone.packetType == InterledgerPacketType.INTERLEDGER_PROTOCOL_ERROR) {
                            throw ilpRespone.optILPException.get();
                        }
                        response.setStatusCode(200);


                    } catch (InterruptedException e) {
                        // TODO:(0) Log interrupted exception
                        retry = true; // Retry
                    } catch (ExecutionException e) {
                        response.setStatusCode(400);
                        final InterledgerProtocolError ilpError =
                                (e.getCause() instanceof InterledgerProtocolException)
                                        ? ((InterledgerProtocolException) e.getCause()).getInterledgerProtocolError()
                                        : InterledgerProtocolError.builder()
                                        .errorCode(InterledgerProtocolError.ErrorCode.T00_INTERNAL_ERROR)
                                        .triggeredByAddress(parentConnector.config.ilpAddress)
                                        // .forwardedByAddresses(ImmutableList.of())
                                        .triggeredAt(Instant.now())
                                        // .data()
                                        .build();
                        res_headers.set("ilp-error-Code", ilpError.getErrorCode().getCode());
                        res_headers.set("ilp-error-Name", ilpError.getErrorCode().getName());
                        res_headers.set("ilp-error-Triggered-By", ilpError.getTriggeredByAddress().toString());
                        res_headers.set("ilp-error-Triggered-At", ilpError.getTriggeredAt().toString());
                        res_headers.set("ilp-error-Message", e.toString());
                    }
                } while (retry);
                response.end("");
/////           switch (obj.type)
/////               case IlpPacket.Type.TYPE_ILP_FULFILL:
/////                   statusCode = 200
/////                   headers = {
/////                           'ilp-fulfillment': obj.data.fulfillment.toString('base64'),
/////                   }
/////           res.writeHead(statusCode, headers)
/////           res.end(obj.data.data)
/////       return new Promise(resolve =>
/////               this.server.listen(this.opts.port, () =>
/////                       logPlugin('listening for http on port ' + this.opts.port)
/////                       this._connected = true
/////                       this.emit('connect')
/////                       resolve()

            }).listen(listeningPort);
        }
    }

    final ILPOverHTTPConfig pluginConfig;
    final Vertx vertx = Vertx.vertx();
    HTTPSServer ilpHTTPSServer;
    final IRequestHandler requestHandler;

    /*
     * TODO:(1) In a production plugin re-read current balance
     *         after reload/reboot
     */
    private BigInteger balanceWithPeer = BigInteger.ZERO;
    final private BigInteger maxIOYAmmount ;
    final private BigInteger maxYOMAmmount ;

    private Status status = Status.DISCONNECTED;

    /**
     * Connector with a PluginConnection object.
     * @param pluginConfig   : Config for initial setup
     */
    public ILPOverHTTPPlugin(
            ILPOverHTTPConfig pluginConfig, IRequestHandler requestHandler) {
        super(pluginConfig);
        this.pluginConfig = pluginConfig;
        maxIOYAmmount = this.pluginConfig.maxIOYAmmount;
        maxYOMAmmount = this.pluginConfig.maxIOYAmmount;
        this.requestHandler = requestHandler;
    }

    /**
     * connect the plugin with the remoteLedgerAdaptor in parameter.
     */
    @Override
    public CompletableFuture<Void> connect() {
        final CompletableFuture<Void> result = new CompletableFuture<Void>();
        // TODO:(0) Retry. It's quite possible that in some setups both peers are restarted at the same time.
        // (only needed for the client roll, server does not need to reconnect, just listen)
        final String verticleID = "1"; // TODO:(0)
        final String tls_key_path = "./certs_vault/tls_key.pem";
        final String tls_crt_path = "./certs_vault/tls_cert.pem";
        ;
        ;

        this.ilpHTTPSServer = new HTTPSServer(
                this.requestHandler,
                verticleID,
                this.pluginConfig.listening_host,
                this.pluginConfig.listening_port,
                tls_key_path,
                tls_crt_path,
                this.pluginConfig.remote_host,
                this.pluginConfig.remote_port
            );
        // TODO:(0) use thread pool from executor. Not new thread
        new Thread(() -> {
            // TODO:(1) Do a "get head" or HTTP 2.0 connection
            result.complete(null); // TODO:(0) Check if that's OK for CompletableFuture<Void>
            this.status = Status.CONNECTED;
        }).start();
        return result;
    }

    @Override
    public CompletableFuture<Void> disconnect() {
        // TODO:(0) IMPLEMENT
        this.status = Status.DISCONNECTED;
        CompletableFuture<Void> result = new CompletableFuture<Void>();
        return result;
    }

    @Override
    public boolean isConnected() {
        return this.status == Status.CONNECTED;
    }


    /**
     * first step of the ILP flow to prepare a transfer by transferring the funds on the hold account and put
     * the transfer status as "PREPARED".
     *
     * @param ILPConditionBase64Encoded,
     * @param destinantion,
     * @param endToEndData
     */
    @Override
    public CompletableFuture<DataResponse> sendData(
            String ILPConditionBase64Encoded,
            InterledgerAddress destinantion,
            Instant ilpExpiry /* TODO:(0) Drop?*/,
            Optional<ByteBuffer> endToEndData
    ){
        if (!isConnected()) {
            // TODO:(0) Use correct ILP Exception
            throw new RuntimeException("plugin not connected.");
        }
        CompletableFuture<DataResponse> result = new CompletableFuture<DataResponse>();
        // TODO:(0) Implement
        // REF: http://vertx.io/docs/vertx-web-client/java/

        // CREATING A WEB CLIENT
        WebClientOptions options = new WebClientOptions()
                .setUserAgent("My-App/1.2.3")
                .setFollowRedirects(false);
        options.setKeepAlive(false);
        WebClient client = WebClient.create(vertx, options);

        // POST TO THE SERVER

        io.vertx.core.buffer.Buffer buffer =  io.vertx.core.buffer.Buffer.buffer();
        if (endToEndData.isPresent()) buffer.setBytes(0,endToEndData.get());
        HttpRequest<io.vertx.core.buffer.Buffer> request1 = client
            .post(443, "TODO:(0)", "TODO:(0) /some-uri");
        request1
            .ssl(true)
            .timeout(5000)
            .putHeader("TODO:(0)", "TODO:(0)")
            .sendBuffer(buffer, /*handle response */ ar -> {
            // WARNING: responses are fully buffered,
            //          use BodyCodec.pipe to pipe the response
            //          to a write stream
            // NOTE: By default no decoding is applied
            //       Custom response body decoding can be achieved using BodyCodec
            //       Ex. To decode as JSON:
            //         HttpResponse<JsonObject> response = ar.result();
            //         JsonObject body = response.body();
            if (ar.succeeded()) {
                final HttpResponse<io.vertx.core.buffer.Buffer> response = ar.result();
                if (response.statusCode() == 200) {
                    final DataResponse delayedResult = new DataResponse(
                        response.getHeader("ilp-fulfillment"),
                        response.bodyAsString().getBytes());
                    result.complete(delayedResult);
                } else {
                    //                  return IlpPacket.serializeIlpReject({
                    final String sCode = response.getHeader("ilp-error-code");
                    final String sName = response.getHeader("ilp-error-name");
                    final String sTriggeredBy = response.getHeader("ilp-error-triggered-by");
                    final List<InterledgerAddress> forwaredByList =
                            Arrays.stream(response.getHeader("ilp-error-forwarded-by").split(","))
                                    .map(s -> InterledgerAddress.of(s))
                                    .collect(Collectors.toList());

                    Instant triggeredAt = Instant.from(ZonedDateTime.parse(response.getHeader("ilp-error-triggered-at")));
                    String message = response.getHeader("ilp-error-message");

                   final InterledgerProtocolError ilpError = InterledgerProtocolError.builder()
                       .errorCode(InterledgerProtocolError.ErrorCode.of(sCode, sName))
                       .triggeredByAddress(InterledgerAddress.of(sTriggeredBy))
                       .forwardedByAddresses(forwaredByList)
                       .triggeredAt(Instant.now())
                       // .data()
                       .build();
                   result.completeExceptionally(new InterledgerProtocolException(ilpError));
                }
            }
          });
        return result;
    }

    /**
     * Send Fulfillment or any data needed for the payment
     *
     * @param amount
     */
    @Override
    public CompletableFuture<Void> sendMoney(String amount) {
        if (!this.isConnected()) {
            // TODO:(0) Use correct ILP Exception
            throw new RuntimeException("Plugin not connected");
        }

        // ILPTransfer transfer = this.ilpPendingTransfers.get(transferId);
        // TODO:(0) Implement
        CompletableFuture<Void> result = new CompletableFuture<Void>();
        return result;

    }

}
