package org.everis.interledger.config.plugin;

import org.everis.interledger.config.PropertiesConfig;
import org.everis.interledger.plugins.MockSettlementLedgerPlugin;

import javax.money.CurrencyUnit;
import javax.money.Monetary;

public class MockSettlementLedgerConfig extends BasePluginConfig {
        /**
         * Next basePluginConfig params are arbitraty, just to have something in place for testing
         */
        public final String host;
        public final String port;
        public final String /* settlement ledger connector's */ account_pass;

        MockSettlementLedgerConfig(PropertiesConfig propConfig){
            super( MockSettlementLedgerPlugin.class, propConfig );
            host          = propConfig.getCleanString("plugin.settlement_ledger.host");
            port          = propConfig.getCleanString("plugin.settlement_ledger.port");
            account_pass  = propConfig.getCleanString("plugin.settlement_ledger.connector_account_password");
        }
    }
