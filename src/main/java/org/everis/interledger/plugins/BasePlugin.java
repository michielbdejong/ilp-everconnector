package org.everis.interledger.plugins;

import org.everis.interledger.config.plugin.BasePluginConfig;
import org.everis.interledger.connector.GenericConnector;
import org.everis.interledger.org.everis.interledger.common.LedgerInfo;

import java.nio.Buffer;
import java.util.concurrent.CompletableFuture;

// TODO:(quilt) Move to quilt/ilp-core (or quilt/ilp-connector-core or similar)
public abstract class BasePlugin {
    private GenericConnector parentConnector; // Ref to the parent connector instantiating this plugin

    public final BasePluginConfig config;
    /*
     * REF: https://github.com/interledger/rfcs/blob/master/0004-ledger-plugin-interface/0004-ledger-plugin-interface.md
     *
     * REF:  https://github.com/interledger/rfcs/issues/359
     *
	 * Sender --sendData-> Connector 1 --sendData-> Connector 2 --sendData-> Receiver
     * |                        |                        |
     * `----sendMoney->         `----sendMoney->         `----sendMoney->
     */
    BasePlugin(BasePluginConfig config){
        this.config = config;
    }

    protected interface IRequestHandler {
        // TODO:(0) This interface design is "arbitrary". Rethink how to do it.
    	public void onRequestReceived(Buffer request);
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

	public abstract LedgerInfo getInfo();
	public abstract String getAccount();
	public abstract void registerRequestHandler(IRequestHandler requestHandler );
	public abstract void deregisterRequestHandler ( );

	// TODO:(?) getBalance needed?
	// public abstract CompletableFuture<String> getBalance ();


	public abstract CompletableFuture<Buffer> sendData(Buffer data);
    public abstract CompletableFuture<Void>   sendMoney(String amount);

    public void setParentConnector(GenericConnector parentConnector) {
        this.parentConnector = parentConnector;
    }
}
