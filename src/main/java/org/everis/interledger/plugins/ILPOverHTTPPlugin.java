package org.everis.interledger.plugins;

// REF 1: ILP-over-HTTP JS Implementation: https://github.com/michielbdejong/ilp-plugin-http/blob/master/index.js
// REF 2: VertX HTTPS server: https://github.com/vert-x3/vertx-examples/blob/master/core-examples/src/main/java/io/vertx/example/core/http/https/Server.java

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;
import io.vertx.core.*;
import io.vertx.core.http.HttpServerResponse;
import org.everis.interledger.config.plugin.PaymentChannelConfig;
import org.everis.interledger.org.everis.interledger.common.LedgerInfo;
import org.everis.interledger.tools.mockILPNetwork.MockHosts;
import org.interledger.InterledgerAddress;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.Buffer;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;

import io.vertx.core.net.PemKeyCertOptions;
import org.interledger.InterledgerProtocolException;
import org.interledger.ilp.InterledgerProtocolError;


/**
 * Entity of Plugin to connect any sender, receiver or connector with a remoteLedgerAdaptor.
 */
// TODO:(0) Rename as BaseBTPPlugin


public class ILPOverHTTPPlugin extends BasePlugin {

    public class HTTPSServer extends AbstractVerticle {
        final String verticleID;
        final String listeningHost;
        final int listeningPort;
        final String tls_key_path;
        final String tls_crt_path;
        final IRequestHandler requestHandler;
        final URL peerUrl;

        public HTTPSServer(
                IRequestHandler requestHandler,
                String verticleID,
                String listeningHost,
                int listeningPort,
                String tls_key_path,
                String tls_crt_path,
                URL peerUrl ) {
            this.requestHandler = requestHandler;
            this.verticleID     = verticleID    ;
            this.listeningHost  = listeningHost ;
            this.listeningPort  = listeningPort ;
            this.tls_key_path   = tls_key_path  ;
            this.tls_crt_path   = tls_crt_path  ;
            this.peerUrl        = peerUrl       ;
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
/////       req.response().putHeader("content-type", "text/html").end("<html><body><h1>Hello from vert.x!</h1></body></html>");
                MultiMap headers = req.headers();
                InterledgerAddress destination         = InterledgerAddress.of(headers.get("ilp-destination"));
                String Base64ExecCondition = headers.get("ilp-condition"  );
                Instant expiresAt          = Instant.from(ZonedDateTime.parse(headers.get("ilp-expiry")));
                String amount              = headers.get("ilp-amount");
                Buffer endToEndData        = null /* TODO:(0) Add body as endToEndData*/;
                CompletableFuture<IRequestHandler.ILPResponse> ilpResponse =
                    this.requestHandler.onRequestReceived(
    	                destination, Base64ExecCondition, expiresAt, amount, endToEndData );
                final HttpServerResponse response = req.response();
                final MultiMap res_headers = response.headers();
                boolean retry;
                do {
                    retry = false;
                    try {
                         // TODO:(0) Blocking call!! exec in thread pool
                        IRequestHandler.ILPResponse ilpRespone = ilpResponse.get();
                        if (ilpRespone.packetType == InterledgerPacketType.INTERLEDGER_PROTOCOL_ERROR){
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
                              ? ((InterledgerProtocolException)e.getCause()).getInterledgerProtocolError()
                              : InterledgerProtocolError.builder()
                               .errorCode(InterledgerProtocolError.ErrorCode.T00_INTERNAL_ERROR)
                               .triggeredByAddress(parentConnector.config.ilpAddress)
                               // .forwardedByAddresses(ImmutableList.of())
                               .triggeredAt(Instant.now())
                               // .data()
                               .build();
                        res_headers.set("ilp-error-Code"        , ilpError.getErrorCode().getCode());
                        res_headers.set("ilp-error-Name"        , ilpError.getErrorCode().getName());
                        res_headers.set("ilp-error-Triggered-By", ilpError.getTriggeredByAddress().toString());
                        res_headers.set("ilp-error-Triggered-At", ilpError.getTriggeredAt().toString() );
                        res_headers.set("ilp-error-Message"     , e.toString());
                    }
                } while(retry);
                response.end("");

/////           let statusCode
/////           let headers
/////           switch (obj.type) {
/////               case IlpPacket.Type.TYPE_ILP_FULFILL:
/////                   statusCode = 200
/////                   headers = {
/////                           'ilp-fulfillment': obj.data.fulfillment.toString('base64'),
/////         }
/////                   break
/////               case IlpPacket.Type.TYPE_ILP_REJECT:
/////                   statusCode = 400
/////                   headers = {

/////         }
/////                   break
/////               default:
/////                   throw new Error('unexpected response type ' + obj.type)
/////           }
/////           logServerResponse(statusCode, headers, obj.data.data)
/////           res.writeHead(statusCode, headers)
/////           res.end(obj.data.data)
/////   }).catch(err => {
/////                   logServerResponse(500, err)
/////                   res.writeHead(500)
/////                   res.end(err.message) // only for debugging, you probably want to disable this line in production
/////           })
///// })
/////       })
/////       return new Promise(resolve => {
/////               this.server.listen(this.opts.port, () => {
/////                       logPlugin('listening for http on port ' + this.opts.port)
/////                       this._connected = true
/////                       this.emit('connect')
/////                       resolve()
/////               })
/////)

        }).listen(listeningPort);
    }
}

    final PaymentChannelConfig pluginConfig;
    boolean USE_MOCK_NETWORK = true;

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
            PaymentChannelConfig pluginConfig) {
     /*
      *     InterledgerAddress ledgerPrefix,
      *     LocalLedgerILPAdaptor ledger,
      *     LedgerConnectionConfig ledgerConnection) {
      */
        super(pluginConfig);
        this.pluginConfig = pluginConfig;
        maxIOYAmmount = this.pluginConfig.maxIOYAmmount;
        maxYOMAmmount = this.pluginConfig.maxIOYAmmount;
    }

    /*

    debug(`processing btp packet ${JSON.stringify(btpPacket)}`)
    try {
    } catch (err) {
      debug(`Error processing BTP packet of type ${btpPacket.type}: `, err)
      const error = jsErrorToBtpError(err)
      const requestId = btpPacket.requestId
      const { code, name, triggeredAt, data } = error

      await this._handleOutgoingBtpPacket(null, {
        type: BtpPacket.TYPE_ERROR,
        requestId,
        data: {
          code,
          name,
          triggeredAt,
          data,
          protocolData: []
        }
      })
    }
     */

    private CompletableFuture<Void> _launchListeningWebSocketServer(){
        final CompletableFuture<Void> result = new CompletableFuture<Void>();
        new Thread(() -> {
            if (USE_MOCK_NETWORK) {
                    MockHosts.registerPlugin(pluginConfig.listening_host, pluginConfig.listening_port, this);
                    result.complete(null);
                    System.out.println("Connector '"+this.parentConnector.config.ilpAddress +
                        "': plugin '"+this.getClass().getCanonicalName()+"' listening for ws clients from ledger||peer '"+ basePluginConfig.ledgerPrefix+"'\n"
                      + " listening @ "+pluginConfig.listening_host +":" +pluginConfig.listening_port);
            } else {
                // Instantiate new websocket server listening for client connections
                result.completeExceptionally(
                       new RuntimeException("Not implemented"));
                // TODO:(0) IMPLEMENT
            }
            this.status = Status.CONNECTED;
        }).start();
        return result;
    }

    /**
     * connect the plugin with the remoteLedgerAdaptor in parameter.
     */
    @Override
    public CompletableFuture<Void> connect() {
        final CompletableFuture<Void> result = new CompletableFuture<Void>();
        // TODO:(0) Retry. It's quite possible that in some setups both peers are restarted at the same time.
        // (only needed for the client roll, server does not need to reconnect, just listen)
        new Thread(() -> {
            if (USE_MOCK_NETWORK) {
                BasePlugin peer;
                while (true  /* Try to reconnect forever and ever */) {
                    try {
                        peer = MockHosts.getInstance(this.pluginConfig.remote_host, this.pluginConfig.remote_port);
                        if (!(peer instanceof ILPOverHTTPPlugin)) {
                            throw new RuntimeException("found peer but was not of the expected class type");
                        }
                        this.peerPaymentChannelPlugin = (ILPOverHTTPPlugin) peer;
                        result.complete(null);
                        System.out.println("Connector '" + this.parentConnector.config.ilpAddress + "': plugin '" + this.getClass().getCanonicalName() + "' connected to ledger||peer '" + basePluginConfig.ledgerPrefix + "'");
                        break;
                    }catch (Exception e){
                        System.out.println("Retrying conection to "+this.pluginConfig.remote_host +":"+ this.pluginConfig.remote_port);
                        try {
                            Thread.sleep(1000);
                        }catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            } else {
                // Connect to running websocket client
                result.completeExceptionally(
                        new RuntimeException("Not implemented"));
                // TODO:(0) IMPLEMENT
            }
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

    @Override
    public LedgerInfo getInfo(){
        if (!isConnected()) {
            // TODO:(0) Use correct ILP Exception
            throw new RuntimeException("plugin not connected");
        }
        // TODO:(0) Implement
        // TODO:(0) Check currency from ledger info, not from initial pluginConfig
        LedgerInfo result = new LedgerInfo(InterledgerAddress.of("peer.TODO:(0)"), pluginConfig.currency);
        return result;
    }

    @Override
	public String getAccount() {
        return this.pluginConfig.account;
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
            Optional<Buffer> endToEndData
    ){
        if (!isConnected()) {
            // TODO:(0) Use correct ILP Exception
            throw new RuntimeException("plugin not connected.");
        }
        // TODO:(0) Implement
        /*
         * sendData (packet) {
         *  const obj = IlpPacket.deserializeIlpPrepare(packet)
         *  const headers = {
         *    'ilp-destination':  obj.destination,
         *    'ilp-condition':    obj.executionCondition.toString('base64'),
         *    'ilp-expiry':       obj.expiresAt.toISOString(),
         *    'ilp-amount':       obj.amount,
         *  }
         *  return fetch(this.opts.peerUrl, {
         *    method: 'POST',
         *    headers,
         *    body: obj.data
         *  }).then(res => {
         *    return res.buffer().then(body => {
         *      logClientResponse(res.status, res.headers.raw(), body)
         *      if (res.status === 200) {
         *        return IlpPacket.serializeIlpFulfill({
         *          fulfillment: Buffer.from(res.headers.get('ilp-fulfillment'), 'base64'),
         *          data: body
         *        })
         *      } else {
         *        return IlpPacket.serializeIlpReject({
         *          code:          res.headers.get('ilp-error-code'),
         *          name:          res.headers.get('ilp-error-name'),
         *          triggeredBy:   res.headers.get('ilp-error-triggered-by'),
         *          triggeredAt:   new Date(res.headers.get('ilp-error-triggered-at')),
         *          message:       res.headers.get('ilp-error-message'),
         *          data: body
         *        })
         *      }
         *    })
         *  }).catch(err => {
         *    return IlpPacket.serializeIlpReject({
         *      code:          'P00',
         *      name:          'plugin bug',
         *      triggeredBy:   'ilp-plugin-http',
         *      triggeredAt:   new Date(),
         *      message:       err.message,
         *      data: Buffer.from([])
         *    })
         *  })
         *}
         */
        CompletableFuture<DataResponse> result = new CompletableFuture<DataResponse>();
        throw new RuntimeException("TODO:(0) Not implemented"); // TODO:(0)
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
