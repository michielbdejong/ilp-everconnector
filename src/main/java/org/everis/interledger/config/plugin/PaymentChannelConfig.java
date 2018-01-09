package org.everis.interledger.config.plugin;

import org.everis.interledger.config.PropertiesConfig;
import org.everis.interledger.plugins.PaymentChannelPlugin;

import java.math.BigInteger;

public class PaymentChannelConfig extends BasePluginConfig {

        public final String host;
        public final String port;
        public final String secret;
        public final BigInteger maxIOYAmmount; // "I Owe You"  max account
        public final BigInteger maxYOMAmmount; // "You Owe me" max account

        PaymentChannelConfig(PropertiesConfig propConfig){
            super( PaymentChannelPlugin.class, propConfig );
            host          = propConfig.getCleanString("plugin.peer_plugin.host");
            port          = propConfig.getCleanString("plugin.peer_plugin.port");
            secret        = propConfig.getCleanString("plugin.peer_plugin.secret");
            maxIOYAmmount = new BigInteger(""+propConfig.getCleanString("plugin.trustLine.maxIOYAmount"));
            maxYOMAmmount = new BigInteger(""+propConfig.getCleanString("plugin.trustLine.maxYOMAmount"));
        }
    }
