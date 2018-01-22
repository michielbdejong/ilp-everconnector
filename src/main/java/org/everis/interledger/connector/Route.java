package org.everis.interledger.connector;

import org.everis.interledger.plugins.BasePlugin;
import org.interledger.InterledgerAddress;
import org.interledger.ilqp.LiquidityCurve;
import org.interledger.ilqp.LiquidityPoint;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public class Route {

    public final InterledgerAddress addressPrefix;
    public final BasePlugin plugin;
    private final List<LiquidityPoint> liquidityCurve;
    // TODO:(0.5) Remove hard-coded percentage and use liquidityCurve instead
    private final BigDecimal percentageApplied = new BigDecimal("105");
    private final BigInteger minimumAllowedAmmount;
    private final BigInteger maximumAllowedAmmount;

    Route (final InterledgerAddress addressPrefix, final BasePlugin plugin, final LiquidityCurve liquidityCurve){
        if (! addressPrefix.getValue().endsWith(".")) {
            throw new RuntimeException("address"+ addressPrefix.getValue() + " must end with '.' ");
        }
        final int curveSize = liquidityCurve.getLiquidityPoints().size();
        if (curveSize < 2) {
            throw new RuntimeException("liquidity curve must have at least 2 points (start and end)");
        }
        this.addressPrefix  = addressPrefix;
        this.plugin         = plugin;
        this.liquidityCurve = liquidityCurve.getLiquidityPoints();
        this.minimumAllowedAmmount = this.liquidityCurve.get(0).getInputAmount();
        this.maximumAllowedAmmount = this.liquidityCurve.get(curveSize-1).getInputAmount();
    }

    public BigInteger interpolateFromInput(BigInteger input) {
        //
        if (input.compareTo(this.minimumAllowedAmmount) < 0) {
            throw new RuntimeException("incomming payment is smaller than minimum allowed ammount ");
        }
        if (input.compareTo(this.maximumAllowedAmmount) > 0) {
            throw new RuntimeException("incomming payment is  bigger than maximum allowed ammount ");
        }
        // TODO:(0.5) Use liquidity points. Temporally use hard-coded percentage.
        BigInteger result = new BigDecimal(input).multiply(percentageApplied).toBigInteger();
        return result;

    }
}
