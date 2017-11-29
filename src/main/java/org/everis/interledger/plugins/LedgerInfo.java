package org.everis.interledger.plugins;

import org.interledger.InterledgerAddress;
import java.math.BigInteger;
import java.util.List;
import javax.money.CurrencyUnit;

public class LedgerInfo {
    private final InterledgerAddress ledgerPrefix;
    private final CurrencyUnit currencyUnit;
    private final Integer currencyPrecision;
    private final Integer currencyScale;
    private final List<InterledgerAddress> connectorAddresses;
    private final BigInteger minBalance;
    private final BigInteger maxBalance;


    public static class Builder {
        private InterledgerAddress ledgerPrefix;
        private CurrencyUnit currencyUnit;
        private Integer currencyPrecision;
        private Integer currencyScale;
        private List<InterledgerAddress> connectorAddresses;
        private BigInteger minBalance;
        private BigInteger maxBalance;


        public Builder ledgerPrefix(InterledgerAddress ledgerPrefix) {
            this.ledgerPrefix = ledgerPrefix;
            return this;
        }

        public Builder currencyUnit(CurrencyUnit currencyUnit) {
            this.currencyUnit = currencyUnit;
            return this;
        }

        public Builder currencyPrecision(Integer currencyPrecision) {
            this.currencyPrecision = currencyPrecision;
            return this;
        }

        public Builder currencyScale(Integer currencyScale) {
            this.currencyScale = currencyScale;
            return this;
        }

        public Builder connectorAddresses(List<InterledgerAddress> connectorAddresses) {
            this.connectorAddresses = connectorAddresses;
            return this;
        }

        public Builder minBalance(BigInteger minBalance) {
            this.minBalance = minBalance;
            return this;
        }

        public Builder maxBalance(BigInteger maxBalance) {
            this.maxBalance = maxBalance;
            return this;
        }

        public LedgerInfo build() {
            return new LedgerInfo(this);
        }
    }


    public LedgerInfo(Builder builder) {
        this.ledgerPrefix = builder.ledgerPrefix;
        this.currencyUnit = builder.currencyUnit;
        this.currencyPrecision = builder.currencyPrecision;
        this.currencyScale = builder.currencyScale;
        this.connectorAddresses = builder.connectorAddresses;
        this.minBalance = builder.minBalance;
        this.maxBalance = builder.maxBalance;
    }


    public InterledgerAddress getLedgerPrefix() {
        return ledgerPrefix;
    }

    public CurrencyUnit getCurrencyUnit() {
        return currencyUnit;
    }

    public Integer getCurrencyPrecision() {
        return currencyPrecision;
    }

    public Integer getCurrencyScale() {
        return currencyScale;
    }

    public List<InterledgerAddress> getConnectorAddresses() {
        return connectorAddresses;
    }

    public BigInteger getMinBalance() {
        return minBalance;
    }

    public BigInteger getMaxBalance() {
        return maxBalance;
    }
}