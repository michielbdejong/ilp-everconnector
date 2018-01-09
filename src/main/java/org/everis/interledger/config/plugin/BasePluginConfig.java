package org.everis.interledger.config.plugin;

import org.everis.interledger.config.PropertiesConfig;
import org.interledger.InterledgerAddress;

import javax.money.CurrencyUnit;
import javax.money.Monetary;

public abstract class BasePluginConfig {
    /*
     * TODO:(0.?) Allow dynamic registration of plugin's types.
     * For now use just hard-coded ones
     */
    public final Class pluginClass;
    public final Class configClass;
    public final String /*connector account on remote peer/ledger*/ account;
    public final CurrencyUnit currency;
    public final InterledgerAddress ledgerPrefix;
    public final String listeningHostOrIP;
    public final String listeningPort;


    public final int    secsReconnect;

    public BasePluginConfig(Class pluginClass, PropertiesConfig propConfig) {
        this.pluginClass = pluginClass;
        this.configClass = this.getClass();
        account     = propConfig.getCleanString("plugin.account");
        currency    = Monetary.getCurrency(propConfig.getCleanString("plugin.currency"));
        secsReconnect = propConfig.getInteger("plugin.secsReconnect");
        ledgerPrefix  = InterledgerAddress.of(propConfig.getString("plugin.ledgerPrefix") );
        listeningHostOrIP = propConfig.getString("plugin.listeningHostOrIP");
        listeningPort  = propConfig.getString("plugin.listeningPort");
    }

    private static final String[] ALLOWED_PLUGIN_LIST = {
        "PaymentChannelPlugin",
        "MockSettelmentLedgerPlugin"
    };
    private static final int paymentChannelIdx = 0, mockSettlementLedger = 1;

    public static BasePluginConfig build(final String configFile){
        PropertiesConfig propConfig = new PropertiesConfig(configFile);
        String pluginClassName = propConfig.getCleanString("plugin.className");

        if (pluginClassName.equals(ALLOWED_PLUGIN_LIST[paymentChannelIdx]) ){
            return new PaymentChannelConfig(propConfig);
        } else if (pluginClassName.equals(ALLOWED_PLUGIN_LIST[mockSettlementLedger]) ) {
            return new MockSettlementLedgerConfig(propConfig);
        } else {
            String sError = "plugin.className '"+pluginClassName+"' not allowed." +
                " Allowed values ";
            throw new RuntimeException("plugin.className not allowed.");
        }
    }
}
