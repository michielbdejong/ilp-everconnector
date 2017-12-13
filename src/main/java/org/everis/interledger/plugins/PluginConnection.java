package org.everis.interledger.plugins;

import org.interledger.InterledgerAddress;

/**
 * Entity that represents the identifiers of the Plugin for the related account on the ledger to test the connection
 * between the plugin and the ledger.
 */
public class PluginConnection {
    private InterledgerAddress pluginAccountAddress;
    private String pluginAccountPassword;

    /**
     * Constructor with an ILP address and a password from the plugin's account on the ledger
     * @param pluginAccountAddress
     * @param pluginAccountPassword
     */
    public PluginConnection(InterledgerAddress pluginAccountAddress, String pluginAccountPassword) {
        this.pluginAccountAddress = pluginAccountAddress;
        this.pluginAccountPassword = pluginAccountPassword;
    }

    //getters and setters
    public InterledgerAddress getPluginAccountAddress() {
        return pluginAccountAddress;
    }

    public void setPluginAccountAddress(InterledgerAddress pluginAccountAddress) {
        this.pluginAccountAddress = pluginAccountAddress;
    }

    public String getPluginAccountPassword() {
        return pluginAccountPassword;
    }

    public void setPluginAccountPassword(String pluginAccountPassword) {
        this.pluginAccountPassword = pluginAccountPassword;
    }

    @Override
    public String toString() {
        return "Plugin connector of " + pluginAccountAddress;
    }
}