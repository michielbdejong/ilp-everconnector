package org.everis.interledger.config.plugin;

import org.everis.interledger.config.PropertiesConfig;
import org.everis.interledger.plugins.ILPOverHTTPPlugin;

import java.math.BigInteger;


public class PaymentChannelConfig extends BasePluginConfig {

        public final String listening_host;
        public final String listening_port;
        public final String remote_host;
        public final String remote_port;
        public final String secret;
        public final BigInteger maxIOYAmmount; // "I Owe You"  max account
        public final BigInteger maxYOMAmmount; // "You Owe me" max account
        public final boolean wsServerListeningForRequest;

        PaymentChannelConfig(PropertiesConfig propConfig){
            super( ILPOverHTTPPlugin.class, propConfig );
            secret        = propConfig.getCleanString("plugin.peer_plugin.secret");

            String sRole  = propConfig.getCleanString("plugin.peer_plugin.websocket_role");
            if (!sRole.equals("client") && !sRole.equals("server")) {
                throw new RuntimeException(
                     "plugin.peer_plugin.websocket_role must be 'client' or 'server' but "
                   + sRole + " was found in PaymentChannelConfig file");
            }
            wsServerListeningForRequest = sRole.equals("server");
            if(wsServerListeningForRequest) {
                listening_host = propConfig.getCleanString("plugin.peer_plugin.listening_host");
                listening_port = propConfig.getCleanString("plugin.peer_plugin.listening_port");
                remote_host    = "DOES NOT APPLICATE";
                remote_port    = "DOES NOT APPLICATE";
            } else {
                listening_host = "DOES NOT APPLICATE";
                listening_port = "DOES NOT APPLICATE";
                remote_host    = propConfig.getCleanString("plugin.peer_plugin.remote_host");
                remote_port    = propConfig.getCleanString("plugin.peer_plugin.remote_port");
            }

            maxIOYAmmount = new BigInteger(""+propConfig.getCleanString("plugin.trustLine.maxIOYAmount"));
            maxYOMAmmount = new BigInteger(""+propConfig.getCleanString("plugin.trustLine.maxYOMAmount"));
        }
    }
