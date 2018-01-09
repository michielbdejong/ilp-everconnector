package org.everis.interledger.org.everis.interledger.common;

import org.interledger.InterledgerAddress;

import javax.money.CurrencyUnit;
import javax.money.Monetary;

/**
 * Entity reference of the ledger with the aim to be store in Plugin once connected
 */
// TODO:(quilt) This class must be standard in quilt/ilp-core (or quilt/ilp-ledger-support or similar)
public class LedgerInfo {
    private final InterledgerAddress ledgerPrefix;
    private final CurrencyUnit ledgerCurency;

    /**
     * Constructor with an ILP ilpAddress prefix and a Currency unit.
     * @param ledgerPrefix
     * @param ledgerCurency
     */
    public LedgerInfo(InterledgerAddress ledgerPrefix, CurrencyUnit ledgerCurency) {
        this.ledgerPrefix = ledgerPrefix;
        this.ledgerCurency = ledgerCurency;
    }

    //getters and setters
    public InterledgerAddress getLedgerPrefix() {
        return ledgerPrefix;
    }

    public CurrencyUnit getLedgerCurency() {
        return ledgerCurency;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("\n-LEDGER-INFO-------------------------------------------");
        str.append("\nPrefix " + ledgerPrefix.toString());
        str.append("\nCurrencyUnit " + ledgerCurency.toString());
        str.append("\n-------------------------");
        return str.toString();
    }
}
