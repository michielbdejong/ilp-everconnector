package org.everis.interledger.config.plugin;

import org.everis.interledger.config.PropertiesConfig;
import org.everis.interledger.plugins.ILPOverHTTPPlugin;

import java.math.BigInteger;


public class ILPOverHTTPConfig extends BasePluginConfig {

        public final String listening_host;
        public final int    listening_port;
        public final String remote_host;
        public final int    remote_port;
        public final String secret; // TODO:(0) Check if secret is really used in ILP-over-HTTP
        public final BigInteger maxIOYAmmount; // "I Owe You"  max account
        public final BigInteger maxYOMAmmount; // "You Owe me" max account

        ILPOverHTTPConfig(PropertiesConfig propConfig){
            super( ILPOverHTTPPlugin.class, propConfig );
            secret        = propConfig.getCleanString("plugin.peer_plugin.secret");

            listening_host = propConfig.getCleanString("plugin.peer_plugin.listening_host");
            listening_port = propConfig.getInteger("plugin.peer_plugin.listening_port");
            remote_host    = propConfig.getCleanString("plugin.peer_plugin.remote_host");
            remote_port    = propConfig.getInteger("plugin.peer_plugin.remote_port");


            maxIOYAmmount = new BigInteger(""+propConfig.getCleanString("plugin.trustLine.maxIOYAmount"));
            maxYOMAmmount = new BigInteger(""+propConfig.getCleanString("plugin.trustLine.maxYOMAmount"));
        }
    }
