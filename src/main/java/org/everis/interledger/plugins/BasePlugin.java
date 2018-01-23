package org.everis.interledger.plugins;

import org.everis.interledger.config.plugin.BasePluginConfig;
import org.everis.interledger.connector.SimpleConnector;
import org.everis.interledger.org.everis.interledger.common.ILPTransfer;
import org.interledger.InterledgerPacketType;
import org.interledger.InterledgerProtocolException;
import org.interledger.cryptoconditions.PreimageSha256Fulfillment;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

// TODO:(quilt) Move to quilt/ilp-core (or quilt/ilp-connector-core or similar)
public abstract class BasePlugin {

    protected SimpleConnector parentConnector; // Ref to the parent connector instantiating this plugin

    protected final BasePluginConfig basePluginConfig; // Common config applying to all shorts of plugins

    protected final Optional<IRequestHandler> requestHandler;
    /*
     * REF: https://github.com/interledger/rfcs/blob/master/0004-ledger-plugin-interface/0004-ledger-plugin-interface.md
     *
     * REF:  https://github.com/interledger/rfcs/issues/359
     *
	 *  Sender --sendData-> Connector 1 --sendData-> Connector 2 --sendData-> Receiver
     *                           |                        |                      |
     *             <--sendMoney--/          <--sendMoney--/        <--sendMoney--/
     */
    public BasePlugin(BasePluginConfig config, Optional<IRequestHandler> requestHandler){
        this.basePluginConfig = config;
        this.requestHandler = requestHandler;
    }

    /*
     * TODO:(0.5) This interface design is "arbitrary". Rethink how to do it.
     *  The idea is to use it as follows:
     *
     *  'external' will be in practice another peer connector or
     *  client connecting to  our (listening) plugin in our connector:
     *  The registered 'IRequestHandler' list will allow, for example,
     *  to query an external application (web shop server) to check if
     *  the arriving condition has been previously "mapped" to
     *
     *     -- setup during plugin initialization --
     *     plugin -> plugin : register IRequestHandler 1 as indicated in plugin config file
     *     plugin -> plugin : register IRequestHandler 2 as indicated in plugin config file
     *     ...
     *     -- request arriving to our plugin --
     *     external -> plugin : request
     *     plugin   -> plugin : forward to registered IRequestHandler list
     *     plug
     */
    public interface IRequestHandler {

        // class ILPResponse {
        //     // TODO:(Quilt) There is no concept of type in Quilt org.interledger.InterledgerPacket
        //     public final int packetType;
        //     public final Optional<InterledgerProtocolException> optILPException;
        //     public final Optional<String> optBase64Fulfillment;

        //     public ILPResponse(
        //         int packetType,
        //         Optional<InterledgerProtocolException> optILPException,
        //         Optional<String> optBase64Fulfillment )
        //     {
        //         if (packetType == InterledgerPacketType.INTERLEDGER_PROTOCOL_ERROR &&
        //             ! optILPException.isPresent()) {
        //             throw new RuntimeException("packetType equals ILP error but optILPException is not present");
        //         } else if (packetType == InterledgerPacketType.ILP_PAYMENT_TYPE &&
        //             !optBase64Fulfillment.isPresent() ) {
        //             throw new RuntimeException("packetType equals ILP_PAYMENT_TYPE but optBase64Fulfillment is not present");
        //         } else if (packetType == -1) {
        //             // Nothing todo. -1 => Continue processing (forward to next connector or handler) {
        //         }
        //         this.packetType = packetType;
        //         this.optILPException = optILPException;
        //         this.optBase64Fulfillment = optBase64Fulfillment;
        //     }
        //}

    	void onRequestReceived(ILPTransfer ilpTransfer, CompletableFuture<DataResponse>  result );
	}

    protected enum Status { CONNECTED, DISCONNECTED; }
    // public abstract LedgerInfo getLedgerInfo();

    /**
     * connect the plugin from any peer/ledger
     * Note: connect can fail or execute asyncronously.
     *    Use isConnected to check conection status
     */
    public abstract CompletableFuture<Void> connect();

    /**
     * disconnect the plugin from any peer/ledger
     */
    public abstract CompletableFuture<Void> disconnect();

    /**
     * verify that the plugin is properly connected to a remoteLedgerAdaptor.
     * @return boolean
     */
    public abstract boolean isConnected() ;

	// TODO:(?) getBalance needed?
	// public abstract CompletableFuture<String> getBalance ();


    public static class DataResponse {
        public final int FORWARD = -1;
        public final int packetType;
        public final Optional<InterledgerProtocolException> optILPException;
        public final Optional<PreimageSha256Fulfillment> optFulfillment;
        public final byte[] endToEndData;
        //     public final Optional<String> optBase64Fulfillment;

        private final String sErrorInvalidPacketType = "packetType must be one of := \n"
           + "   InterledgerPacketType.INTERLEDGER_PROTOCOL_ERROR ("+InterledgerPacketType.INTERLEDGER_PROTOCOL_ERROR+") to indicate an error/exception\n"
           + "   InterledgerPacketType.ILP_PAYMENT_TYPE ("+InterledgerPacketType.ILP_PAYMENT_TYPE+") to indicate an (resolved) fulfillment\n"
           + "   FORWARD("+FORWARD+") to indicate that processing must continue (use only in custom handlers)";

        public DataResponse(
                int packetType,
                Optional<PreimageSha256Fulfillment> optionalFulfillment,
                byte[] endToEndData,
                Optional<InterledgerProtocolException> optILPException) {
            if (packetType == InterledgerPacketType.INTERLEDGER_PROTOCOL_ERROR &&
                ! optILPException.isPresent()) {
                throw new RuntimeException("packetType equals ILP error but optILPException is not present");
            } else if (packetType == InterledgerPacketType.ILP_PAYMENT_TYPE &&
                !optionalFulfillment.isPresent() ) {
                throw new RuntimeException("packetType equals ILP_PAYMENT_TYPE but optBase64Fulfillment is not present");
            } else if (packetType == FORWARD) {
                // Nothing todo. -1 => Continue processing (forward to next connector or handler) {
            } else {
                throw new RuntimeException(sErrorInvalidPacketType);
            }
            this.packetType = packetType;
            this.optFulfillment = optionalFulfillment;
            this.optILPException = optILPException;
            this.endToEndData = endToEndData;
        }
    }
    // REF: https://github.com/interledger/rfcs/blob/de237e8b9250d83d5e9d9dec58e7aca88c887b57/0000-ilp-over-http.md
    // REQUEST:
    // > ILP-Condition: x73kz0AGyqYqhw/c5LqMhSgpcOLF3rBS8GdR52hLpB8=
	// > ILP-Destination: g.crypto.bitcoin.1XPTgDRhN8RFnzniWCddobD9iKZatrvH4.~asdf1234
    // > ILP-Expiry: 2017-12-07T18:47:59.015Z ?
    // > ILP-Amount: 1000 ?
    // >
    // > body

    // RESPONSE:
    // < HTTP/1.1 200 OK
    // < ILP-Fulfillment: cz/9RGv1PVjhKIOoyPvWkAs8KrBpIJh8UrYsQ8j34CQ=<
    // <
    // < body
    public abstract void sendData(ILPTransfer ilpTransfer,  CompletableFuture<BasePlugin.DataResponse> result);

    public void setParentConnector(SimpleConnector parentConnector) {
        this.parentConnector = parentConnector;
    }


    public String getConfigFile() {
        return basePluginConfig.configFile;
    }

}
