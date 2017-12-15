package org.everis.interledger.plugins;

import org.everis.interledger.org.everis.interledger.common.ILPTransfer;
import org.everis.interledger.org.everis.interledger.common.LedgerInfo;
import org.everis.interledger.tools.mockLedger.LocalLedgerILPAdaptor;
import org.interledger.InterledgerAddress;
import org.interledger.cryptoconditions.Fulfillment;

/**
 * Entity of Plugin to connect any sender, receiver or connector with a ledger.
 */
public class Plugin {

    final private LedgerConnection ledgerConnection;
    private LocalLedgerILPAdaptor ledger;

    private LedgerInfo ledgerInfo = null; // cache info. It's retrieved during connection.


    /**
     * Keeps credentials and any other usefull info to help connect to the ledger
     * This info will vary for each different ledger
     * This class is used as input to the connect phase.
     * It represents data needed to find the ledger on the network, authenticate to the ledger,...
     *
     * Once connected info about the remote ledger can be fetched throught LedgerInfo (connected_)ledgder.getInfo();
     */
    static class LedgerConnection {
        final String host; // not used, just left as example data
        final String port; // not used, just left as example data
        final String accound_id;
        final String pass;
        final InterledgerAddress connectorAddress;

        public LedgerConnection(String account_id, String pass, InterledgerAddress connectorAddress){
           this.accound_id = account_id;
           this.pass = pass;
           this.host = "mockHost";
           this.port = "mockPort";
           this.connectorAddress = connectorAddress;
        }
    }

    /**
     * Connector with a PluginConnection object.
     * @param ledger: Represent an in-memory-ledger that must exists before the ledger can connect
     *                It can be created at startup (static void main ...) by injection from Spring, ...
     *                But the important point is that a ledger is instantiated first, then the plugin
     *                will try to connect to it.
     * @param ledgerConnection
     */
    public Plugin(LocalLedgerILPAdaptor ledger, LedgerConnection ledgerConnection) {
        this.ledger = ledger;
        this.ledgerConnection = ledgerConnection;
    }


    /**
     * This static class simulates a network connection to the ledger
     *
     * In this case we just instantiate a new in-memory-ledger
     * @param host
     * @param port
     * @param connector_account_on_ledger
     * @param connector_password_on_ledger
     * @return
     */
    /**
     * connect the plugin with the ledger in parameter.
     */
    public void connect() {
        this.ledger.onILPConnectRequest(ledgerConnection.connectorAddress, ledgerConnection.accound_id, ledgerConnection.pass);
        this.ledgerInfo = ledger.getInfo();
    }

    /**
     * disconnect the plugin from any ledger connected.
     */
    public void disconnect() {
        this.ledger.disconnect(this.ledgerConnection.connectorAddress);
        this.ledgerInfo = null; // <-- Ummm
    }

    /**
     * verify that the plugin is right connected to a ledger.
     * @return boolean
     */
    public boolean isConnected() {
        return this.ledgerInfo != null;
    }

    /**
     * call the prepare transfer method on the ledger.
     * @param newTransfer
     */
    public void prepareTransfer(ILPTransfer newTransfer) {
        this.ledger.prepareTransaction(newTransfer);
    }

    /**
     * call the fulfill condition method on the ledger.
     * @param transferId
     * @param fulfillment
     */
    public void fulfillCondition(String transferId, Fulfillment fulfillment) {
        this.ledger.fulfillCondition(transferId, fulfillment);
    }

    /**
     * call the reject transfer method on the ledger.
     * @param transferId
     */
    public void rejectTransfer(int transferId) {
        this.ledger.rejectTransfer(transferId);
    }
    
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("-PLUGIN-ACCOUNTS---------");
        str.append("\n-------------------------");
        str.append("\n" +  this.ledger == null ? "-NO-LEDGER------------" : this.ledgerInfo);
        str.append("\nLinked Account " + this.ledgerConnection.connectorAddress);
        str.append("\n-------------------------");
        return str.toString();
    }
}
