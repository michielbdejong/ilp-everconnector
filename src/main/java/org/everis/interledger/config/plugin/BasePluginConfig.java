package org.everis.interledger.config.plugin;

import org.everis.interledger.config.PropertiesConfig;
import org.interledger.InterledgerAddress;
import org.interledger.ilqp.LiquidityCurve;
import org.interledger.ilqp.LiquidityPoint;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public abstract class BasePluginConfig {
    /*
     * TODO:(0.?) Allow dynamic registration of plugin's types.
     * For now use just hard-coded ones
     */
    public final Class pluginClass;
    public final Class configClass;
    public final String /*connector account on remote peer/ledger*/ account;
    public final CurrencyUnit currency;
    public final InterledgerAddress ledgerPrefix; // TODO:(0) when we receive getInfo() compare and raise exception if do not match
    public final LiquidityCurve liquidityCurve;
    public final String configFile;

    public final int    secsReconnect;

    public BasePluginConfig(Class pluginClass, PropertiesConfig propConfig) {
        this.pluginClass = pluginClass;
        this.configClass = this.getClass();
        account          = propConfig.getCleanString("plugin.account");
        currency         = Monetary.getCurrency(propConfig.getCleanString("plugin.currency"));
        secsReconnect    = propConfig.getInteger("plugin.secsReconnect");
        ledgerPrefix     = InterledgerAddress.of(propConfig.getString("plugin.ledgerPrefix") );
        String liquidityPoints = propConfig.getString("plugin.liquidity_points");
        this.configFile  =  propConfig.CONFIG_FILE;

        List<LiquidityPoint> points = Arrays.stream(liquidityPoints.split(";"))
            .collect(Collectors.toList()).stream()
            .map(in_out_string -> in_out_string.split(":"))
            .collect(Collectors.toList()).stream()
            .map(in_out_tuple -> LiquidityPoint.builder()
                    .inputAmount (new BigInteger(in_out_tuple[0]))
                    .outputAmount(new BigInteger(in_out_tuple[1])).build())
            .collect(Collectors.toList());
        LiquidityCurve.Builder builder = LiquidityCurve.Builder.builder();
        points.forEach(point -> builder.liquidityPoint(point));
        this.liquidityCurve = builder.build();
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
            return new ILPOverHTTPConfig(propConfig);
        } else if (pluginClassName.equals(ALLOWED_PLUGIN_LIST[mockSettlementLedger]) ) {
            return new MockSettlementLedgerConfig(propConfig);
        } else {
            String sError = "plugin.className '"+pluginClassName+"' not allowed." +
                " Allowed values ";
            throw new RuntimeException("plugin.className not allowed.");
        }
    }
}
