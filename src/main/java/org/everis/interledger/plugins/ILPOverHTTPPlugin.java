package org.everis.interledger.plugins;

// REF 1: RFC: https://github.com/interledger/rfcs/pull/349
// REF 2: ILP-over-HTTP JS Implementation: https://github.com/michielbdejong/ilp-plugin-http/blob/master/index.js
// REF 3: Writting HTTP servers and clients: http://vertx.io/docs/vertx-core/java/#_writing_http_servers_and_clients


/*
 * ILP Over HTTP
 *
 * Interledger payment fields: (such as destination address)
 * local to a specific hop fields: (such as the transfer amount)
 *
 * POST / HTTP/1.1
 * ILP-Destination: g.crypto.bitcoin.1XPTgDRhN8RFnzniWCddobD9iKZatrvH4.~asdf1234
 * ILP-Condition: x73kz0AGyqYqhw/c5LqMhSgpcOLF3rBS8GdR52hLpB8=
 * ILP-Expiry: 2017-12-07T18:47:59.015Z
 * ILP-Amount: 1000
 *
 *   Field            Type            Modified at  Description
 *                                    Each Hop?
 * -----------------------------------------------------------
 *   ILP-Destination  ILP Address     N            Destination address of the payment
 * -----------------------------------------------------------
 *   ILP-Condition    32 Bytes        N            Exec. cond. of payment
 *                    Base64-Encoded
 *                    String (
 *                     With Padding)
 * -----------------------------------------------------------
 *   ILP-Expiry       ISO 8601        Y            Expiry of the transfer
 *                    UTC Timestamp
 * -----------------------------------------------------------
 *   ILP-Amount       Unsigned        Y            Transfer amount, denominated in
 *                    64-Bit Integer               he minimum divisible units of the ledger.
 *                                                 ote that this is the local transfer amount,
 *                                                 ot the destination amount as in the original
 *                                                 LP Payment Packet Format
 * -----------------------------------------------------------
 *   <body>           Binary,         N            End-to-end data used by
 *                    32767 Bytes max              Transport Layer protocols
 * -----------------------------------------------------------
 *
 */

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;
import io.vertx.core.*;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import org.everis.interledger.config.ExecutorConfigSupport;
import org.everis.interledger.config.VertXConfigSupport;
import org.everis.interledger.config.plugin.ILPOverHTTPConfig;
import org.everis.interledger.org.everis.interledger.common.ILPTransfer;
import org.interledger.InterledgerAddress;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.interledger.InterledgerPacketType;
import org.interledger.InterledgerProtocolException;
import org.interledger.cryptoconditions.PreimageSha256Condition;
import org.interledger.cryptoconditions.PreimageSha256Fulfillment;
import org.interledger.ilp.InterledgerProtocolError;

/**
 * Entity of Plugin to connect any sender, receiver or connector with a remoteLedgerAdaptor.
 */

public class ILPOverHTTPPlugin extends BasePlugin {
    static final String HEADER_ILP_ERROR_CODE         = "ilp-error-code";
    static final String HEADER_ILP_ERROR_NAME         = "ilp-error-name";
    static final String HEADER_ILP_ERROR_TRIGGERED_BY = "ilp-error-triggered-by";
    static final String HEADER_ILP_ERROR_FORWARDED_BY = "ilp-error-forwarded-by";
    static final String HEADER_ILP_ERROR_TRIGGERED_AT = "ilp-error-triggered-at";
    /*
     * TODO:(0) ilp-error-message doesn't look to be part of the RFCS, but it's needed
     *         to trace errors
     */
    static final String HEADER_ILP_ERROR_MESSAGE      = "ilp-error-message";

    /* HTTPS server listening for incomming requests */
    public class HTTPSServer extends AbstractVerticle {
        final Optional<IRequestHandler> requestHandler;
        final String listeningHost;
        final int listeningPort;
        final String tls_key_path;
        final String tls_crt_path;
        final String remote_host;
        final int    remote_port;

        // REF 3: VertX HTTPS server:
        // https://github.com/vert-x3/vertx-examples/blob/master/core-examples/
        // src/main/java/io/vertx/example/core/http/https/Server.java
        private HTTPSServer(
                Optional<IRequestHandler> requestHandler,
                String listeningHost,
                int listeningPort,
                String tls_key_path,
                String tls_crt_path,
                String remote_host,
                int    remote_port
        ) {
            this.requestHandler = Objects.requireNonNull(requestHandler);
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
            final boolean disableSSL = parentConnector.config.developmentMode && pluginConfig.developmentDisableTLS;
            if (!disableSSL) {
                serverOptions.setSsl(true);
                serverOptions.setPemKeyCertOptions( //Assume PEM encoding
                        new PemKeyCertOptions()
                                .setKeyValue(_readRelativeFile(tls_key_path))
                                .setCertValue(_readRelativeFile(tls_crt_path))
                );
                // TODO:(1) Use key Store with password protection
                // .setKeyStoreOptions(
                //    new JksOptions().
                //        setPath("server-keystore.jks").
                //        setPassword("wibble") )
            }
            return serverOptions;
        }

        private void __respondWithError(HttpServerResponse response, Throwable e) {
            e.printStackTrace();
            final MultiMap res_headers = response.headers();
            final InterledgerProtocolException finalExp = (e instanceof InterledgerProtocolException)
                ? (InterledgerProtocolException) e
                : new InterledgerProtocolException(InterledgerProtocolError.builder()
                   .errorCode(InterledgerProtocolError.ErrorCode.T00_INTERNAL_ERROR)
                   .triggeredByAddress(parentConnector.config.ilpAddress)
                   .triggeredAt(Instant.now())
                   .data(e.getCause()!=null?e.getCause().toString().getBytes():e.toString().getBytes())
                   .build());
            final InterledgerProtocolError ilpError = finalExp.getInterledgerProtocolError();
            final List<InterledgerAddress> forwByList = ilpError.getForwardedByAddresses();
            final String CSVForwardedBy;
            if (forwByList.size() == 0) {
                CSVForwardedBy = "";
            } else {
                final StringBuffer aux = new StringBuffer();
                for (InterledgerAddress address : forwByList) {
                aux.append(address.getValue());
                aux.append(',');
                }
                aux.deleteCharAt(aux.length()-1); // Remove last ','
                CSVForwardedBy = new String(aux);
            }
            final String sMessage;
            if (ilpError.getData().isPresent()) {
                sMessage = new String(ilpError.getData().get()/* TODO:(0) Encode in Base64? */);
            } else {
                sMessage = "";
            }
            response.setStatusCode(400);
            res_headers.set(HEADER_ILP_ERROR_CODE        , ilpError.getErrorCode().getCode());
            res_headers.set(HEADER_ILP_ERROR_NAME        , ilpError.getErrorCode().getName());
            res_headers.set(HEADER_ILP_ERROR_TRIGGERED_BY, ilpError.getTriggeredByAddress().getValue());
            res_headers.set(HEADER_ILP_ERROR_TRIGGERED_AT, ilpError.getTriggeredAt().toString());
            res_headers.set(HEADER_ILP_ERROR_FORWARDED_BY, CSVForwardedBy);
            res_headers.set(HEADER_ILP_ERROR_MESSAGE     , sMessage);
            response.end("");
        }


        @Override
        public void start() throws Exception {
            HttpServer server = vertx.createHttpServer( _getHTTPServerOptions() );

            server.requestHandler(req -> {
                System.out.println("debug: ILPOverHTTPPlugin requestHandler starting HTTP incoming request processing");
                final HttpServerResponse response = req.response();

                try {
                    MultiMap headers = req.headers();
                    final byte[] endToEndData = null /* TODO:(0) Add body as endToEndData*/;
                    final byte[] conditionFingerprint =
                            Base64.getUrlDecoder().decode(headers.get("ilp-condition"));
                    ILPTransfer ilpTransfer = new ILPTransfer(
                        /*String*/ "TODO:(0) ILP_TX_UUID",
                        InterledgerAddress.of(headers.get("ilp-destination")),
                        headers.get("ilp-amount"),
                        Instant.from(ZonedDateTime.parse(headers.get("ilp-expiry"))),
                        new PreimageSha256Condition(1 /*cost*/ /*cost*/, conditionFingerprint),
                        endToEndData
                    );
                    // TODO:(0) Check that incomming transfer is not expired.

                    CompletableFuture<BasePlugin.DataResponse> ilpResponseFuture = new CompletableFuture<> ();
                    parentConnector.handleRequestOrForward(this.requestHandler, ilpTransfer, ilpResponseFuture);

                    ExecutorConfigSupport.executor.submit(() -> {
                        System.out.println("deleteme ILPOverHTTPPlugin requestHandler 2.0");
                        boolean retry;
                        do {
                            retry = false;
                            try {
                                String sResponse = "";
                                final BasePlugin.DataResponse ilpResponse = ilpResponseFuture.get();
                                if (ilpResponse.packetType == InterledgerPacketType.INTERLEDGER_PROTOCOL_ERROR) {
                                    System.out.println("deleteme ILPOverHTTPPlugin requestHandler 3");
                                    throw ilpResponse.optILPException.get();
                                } else if (ilpResponse.packetType == DataResponse.FORWARD) {
                                    /*
                                     * This must never happen. If local handler/s can not resolve, the request
                                     * must be forwarded to next hop until either it's resolved or an exception is raised
                                     */
                                    __respondWithError(response, new RuntimeException("No valid response and no error"));
                                    return;
                                } else {
                                    // TODO:(0) Write recieved endToEndData in sResponse (HTTP body)
                                    response.putHeader("ILP-Fulfillment", ilpResponse.optFulfillment.get().getPreimage());
                                    response.setStatusCode(200);
                                    response.end(sResponse);
                                    return;
                                }
                            } catch (InterruptedException e) {
                                System.out.println("deleteme ILPOverHTTPPlugin requestHandler 5");
                                // TODO:(1) Log interrupted exception
                                retry = true; // Retry
                            } catch (Throwable e) {
                                __respondWithError(response, e.getCause() != null ? e.getCause() : e);
                            }
                        } while (retry);
                  });
                }catch (Throwable e) {
                    __respondWithError(response, e.getCause()!=null ? e.getCause() : e);
                }
            }).listen(listeningPort);
            System.out.println("ILP-over-HTTP plugin listening @ " + listeningPort);
        }
    }

    final ILPOverHTTPConfig pluginConfig;
    HTTPSServer ilpHTTPSServer;

    // TODO:(1) In a production plugin re-read current balance after reload/reboot
    /*
     * TODO:(0) Update balanceWithPeer after successfully having receive the payment receipt (fulfillment)
     * TODO:(0) Check maxIOYAmmount/maxYOUAmmount not rebased before forwarding to peer.
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
            ILPOverHTTPConfig pluginConfig, Optional<IRequestHandler> requestHandler) {
        super(pluginConfig, requestHandler);
        this.pluginConfig = pluginConfig;
        maxIOYAmmount = this.pluginConfig.maxIOYAmmount;
        maxYOMAmmount = this.pluginConfig.maxIOYAmmount;
    }

    /**
     * connect the plugin with the remoteLedgerAdaptor in parameter.
     */
    @Override
    public CompletableFuture<Void> connect() {
        final CompletableFuture<Void> result = new CompletableFuture<Void>();
        /* TODO:(0.5) Retry. It's quite possible that in some setups both peers are restarted at the same time or
         * the remote peer is temporally down. Remove from list just after a security timeout period.
         */
        // (only needed for the client roll, server does not need to reconnect, just listen)

        this.ilpHTTPSServer = new HTTPSServer(
                requestHandler,
                this.pluginConfig.listening_host,
                this.pluginConfig.listening_port,
                this.parentConnector.config.tls_key_path,
                this.parentConnector.config.tls_crt_path,
                this.pluginConfig.remote_host,
                this.pluginConfig.remote_port
            );

        MessagePassingQueue.Consumer<Vertx> runner = vertx -> {
            try {
                vertx.deployVerticle(ilpHTTPSServer);
            } catch (Throwable t) {
            }
        };

        runner.accept(VertXConfigSupport.getVertx());

        result.complete(null); // TODO:(0.1) Check if that's OK for CompletableFuture<Void>
        this.status = Status.CONNECTED;

        return result;
    }

    @Override
    public CompletableFuture<Void> disconnect() {
        // TODO:(0.5) IMPLEMENT
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
     * @param ilpTransfer
     * @return
     */
    @Override
    public void sendData(ILPTransfer ilpTransfer, CompletableFuture<DataResponse> result){
        // if (!isConnected()) {
        //     throw new RuntimeException("plugin not connected.");
        // }
        // CREATING A WEB CLIENT: REF: http://vertx.io/docs/vertx-web-client/java/
        WebClientOptions options = new WebClientOptions()
                .setUserAgent("My-App/1.2.3")
                .setFollowRedirects(false)
                .setSsl(parentConnector.config.developmentMode && pluginConfig.developmentDisableTLS ? false : true)
                .setTrustAll(
                    pluginConfig.ignoreTLSCerts
                    /* TODO:(?) check also if old problem persists: https://github.com/eclipse/vert.x/issues/1398 */);
        options.setKeepAlive(true);
        WebClient client = WebClient.create(VertXConfigSupport.getVertx(), options);
        // POST TO THE SERVER
        io.vertx.core.buffer.Buffer buffer =  io.vertx.core.buffer.Buffer.buffer();
        // TODO:(0) uncommented this line blocks "forever" if (ilpTransfer.endToEndData.length>0) buffer.setBytes(0,ilpTransfer.endToEndData);
        HttpRequest<io.vertx.core.buffer.Buffer> request1 = client
            .post(this.pluginConfig.remote_port, this.pluginConfig.remote_host, "/") ;
        request1
            .timeout(30000 /* TODO:(0.5) hardcoded */)
            .putHeader("ILP-Condition"   , ilpTransfer.condition.getFingerprintBase64Url())
            .putHeader("ILP-Expiry"      , ilpTransfer.expiresAt.toString())
            .putHeader("ILP-Destination" , ilpTransfer.destinationAccount.getValue())
            .putHeader("ILP-Amount"      , ilpTransfer.amount)
            .sendBuffer(buffer, /*handle response */ ar -> {
            // C&P from VertX tutorial:
            //     WARNING: responses are fully buffered,
            //          use BodyCodec.pipe to pipe the response
            //          to a write stream
            // NOTE: By default no decoding is applied
            //       Custom response body decoding can be achieved using BodyCodec
            //       Ex. To decode as JSON:
            //         HttpResponse<JsonObject> response = ar.result();
            //         JsonObject body = response.body();
            if (!ar.succeeded()) {
                String sError = "ilp-over-http request failed due to \n"+ar.cause().getMessage();
                InterledgerProtocolException finalExp =
                    new InterledgerProtocolException(
                        InterledgerProtocolError.builder()
                            .errorCode(InterledgerProtocolError.ErrorCode.F02_UNREACHABLE)
                            .triggeredAt(Instant.now())
                            .triggeredByAddress(parentConnector.config.ilpAddress)
                            .data(sError.getBytes())
                            .build());
                result.completeExceptionally(finalExp);

                return;
            }
            final HttpResponse<io.vertx.core.buffer.Buffer> response = ar.result();
            if (response.statusCode() == 200) {
                final DataResponse delayedResult = new DataResponse(
                    InterledgerPacketType.ILP_PAYMENT_TYPE,
                    Optional.of(new PreimageSha256Fulfillment(Base64.getDecoder().decode(response.getHeader("ilp-fulfillment")))),
                    new byte[] {},
                    Optional.empty()
                    /* TODO:(0) response.bodyAsString().getBytes()*/);
                result.complete(delayedResult);
            } else {
                final String sCode = response.getHeader(HEADER_ILP_ERROR_CODE);
                final String sName = response.getHeader(HEADER_ILP_ERROR_NAME);
                final String sTriggeredBy = response.getHeader(HEADER_ILP_ERROR_TRIGGERED_BY);
                final List<InterledgerAddress> forwaredByList =
                        Arrays.stream(response.getHeader(HEADER_ILP_ERROR_FORWARDED_BY).split(","))
                                .filter(s -> !s.isEmpty())
                                .map(s -> InterledgerAddress.of(s))
                                .collect(Collectors.toList());

                Instant triggeredAt = Instant.from(ZonedDateTime.parse(response.getHeader(HEADER_ILP_ERROR_TRIGGERED_AT)));
                String message = response.getHeader(HEADER_ILP_ERROR_MESSAGE);

               final InterledgerProtocolError ilpError = InterledgerProtocolError.builder()
                   .errorCode(InterledgerProtocolError.ErrorCode.of(sCode, sName))
                   .triggeredByAddress(InterledgerAddress.of(sTriggeredBy))
                   .forwardedByAddresses(forwaredByList)
                   .triggeredAt(Instant.now())
                   .data(response.getHeader(HEADER_ILP_ERROR_MESSAGE).getBytes())
                   .build();
               result.completeExceptionally(new InterledgerProtocolException(ilpError));
            }
         });
    }

}
