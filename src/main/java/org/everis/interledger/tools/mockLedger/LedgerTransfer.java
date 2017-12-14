package org.everis.interledger.tools.mockLedger;


/**
 * Local Ledger transfer.
 * This transfer is not aware at all of ILP transfers.
 *
 */
public class LedgerTransfer {
    public final LocalAccount from;
    public final LocalAccount to;
    public final int ammount;

    LedgerTransfer(LocalAccount from, LocalAccount to, int ammount){
        if (ammount <= 0) {
            throw new RuntimeException("ammount must be bigger than ZERO");
        }
        this.from = from;
        this.to   = to;
        this.ammount = ammount;
    }

}
