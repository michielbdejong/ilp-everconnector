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
}
