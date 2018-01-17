package org.everis.interledger.connector;

import org.everis.interledger.plugins.BasePlugin;
import org.interledger.InterledgerAddress;

public class Route {
    public final InterledgerAddress addressPrefix;
    public final BasePlugin plugin;

    Route (final InterledgerAddress addressPrefix, final BasePlugin plugin){
        if (! addressPrefix.getValue().endsWith(".")) {
            throw new RuntimeException("address"+ addressPrefix.getValue() + " must end with '.' ");
        }
        this.addressPrefix = addressPrefix;
        this.plugin        = plugin;
    }
}
