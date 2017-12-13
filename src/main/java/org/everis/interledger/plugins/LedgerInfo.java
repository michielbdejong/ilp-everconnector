package org.everis.interledger.plugins;

import org.interledger.InterledgerAddress;

import javax.money.CurrencyUnit;

/**
 * Entity reference of the ledger with the aim to be store in Plugin once connected
 */
public class LedgerInfo {
    private InterledgerAddress ledgerPrefix;
    private CurrencyUnit ledgerCurency;


    /**
     * Constructor with an ILP address prefix and a Currency unit.
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

    public void setLedgerPrefix(InterledgerAddress ledgerPrefix) {
        this.ledgerPrefix = ledgerPrefix;
    }

    public CurrencyUnit getLedgerCurency() {
        return ledgerCurency;
    }

    public void setLedgerCurency(CurrencyUnit ledgerCurency) {
        this.ledgerCurency = ledgerCurency;
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
