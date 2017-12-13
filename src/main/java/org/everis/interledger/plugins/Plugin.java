package org.everis.interledger.plugins;

import org.interledger.cryptoconditions.Fulfillment;

/**
 * Entity of Plugin to connect any sender, receiver or connector with a ledger.
 */
public class Plugin {
    private PluginConnection pluginConnection;
    private Ledger ledger;

    /**
     * Connector with a PluginConnection object.
     * @param pluginConnection
     */
    public Plugin(PluginConnection pluginConnection) {
        this.pluginConnection = pluginConnection;
    }
    
    /**
     * Connector who creates too the instance of the plugin connection with the linked account 
     * to this plugin.
     * @param linkedAccount Account which will be linked to this instance of the plugin. It will
     *                      take the values of the account and call the base constructor.
     */
    public Plugin(Account linkedAccount) {
        this.ledger = null;
        this(new PluginConnection(linkedAccount));
    }

    /**
     * connect the plugin with the ledger in parameter.
     * @param ledger
     */
    public void connect(Ledger ledger) {
        ledger.connect(this.pluginConnection);
        this.ledger = ledger;
    }

    /**
     * disconnect the plugin from any ledger connected.
     */
    public void disconnect() {
        ledger.disconnect(this.pluginConnection.getPluginAccountAddress());
        this.ledger = null;
    }

    /**
     * verify that the plugin is right connected to a ledger.
     * @return
     */
    public boolean isConnected() {
        return this.ledger.isPluginConnected(this.pluginConnection.getPluginAccountAddress());
    }

    /**
     * call the prepare transfer method on the ledger.
     * @param newTransfer
     */
    public void sendTransfer(Transfer newTransfer) {
        this.ledger.prepareTransaction(newTransfer);
    }

    /**
     * call the fulfill condition method on the ledger.
     * @param transferId
     * @param fulfillment
     */
    public void fulfillCondition(int transferId, Fulfillment fulfillment) {
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
        str.append("\n" +  this.ledger == null ? "-NO-LEDGER------------" : this.getLedgerInfo());
        str.append("\nLinked Account " + this.pluginConnection.getAccount());
        str.append("\n-------------------------");
        return str.toString();
    }
}
