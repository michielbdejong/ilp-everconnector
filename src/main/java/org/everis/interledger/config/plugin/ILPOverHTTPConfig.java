package org.everis.interledger.config.plugin;

import org.everis.interledger.config.PropertiesConfig;
import org.everis.interledger.plugins.ILPOverHTTPPlugin;

import java.math.BigInteger;

// TODO:(0.5) Make BasePluginConfig a member instead of inheriting
public class ILPOverHTTPConfig extends BasePluginConfig {

    public final String listening_host;
    public final int    listening_port;
    public final String remote_host;
    public final String remote_path;
    public final int    remote_port;
    public final BigInteger maxIOYAmmount; // "I Owe You"  max account
    public final BigInteger maxYOMAmmount; // "You Owe me" max account
    public final boolean ignoreTLSCerts;
    public final boolean developmentDisableTLS;

    ILPOverHTTPConfig(PropertiesConfig propConfig){
        super( ILPOverHTTPPlugin.class, propConfig );
        listening_host = propConfig.getCleanString("plugin.peer_plugin.listening_host");
        listening_port = propConfig.getInteger("plugin.peer_plugin.listening_port");
        remote_host    = propConfig.getCleanString("plugin.peer_plugin.remote_host");
        remote_path    = propConfig.getCleanString("plugin.peer_plugin.remote_path");
        remote_port    = propConfig.getInteger("plugin.peer_plugin.remote_port");
        ignoreTLSCerts = propConfig.getBoolean("plugin.peer_plugin.ignoreRemoteTLSCertificates");
        developmentDisableTLS = propConfig.getBoolean("plugin.development.tls.disable");
        maxIOYAmmount  = new BigInteger(""+propConfig.getCleanString("plugin.trustLine.maxIOYAmount"));
        maxYOMAmmount  = new BigInteger(""+propConfig.getCleanString("plugin.trustLine.maxYOMAmount"));
    }
}
