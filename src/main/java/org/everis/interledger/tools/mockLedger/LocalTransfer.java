package org.everis.interledger.tools.mockLedger;


import java.math.BigInteger;

/**
 * Local Ledger transfer.
 * This transfer is not aware at all of ILP transfers.
 * A LocalTransfer is an intention to move money. Money it's not moved until
 * LocalLedger.executeTransfer(localTransfer) end successfully without errors
 */
public class LocalTransfer {
    public final LocalAccount from;
    public final LocalAccount to;
    public final BigInteger ammount;

    LocalTransfer(LocalAccount from, LocalAccount to, BigInteger ammount){
        if (ammount.compareTo(BigInteger.ZERO) <= 0) {
            throw new RuntimeException("ammount must be bigger than ZERO");
        }
        this.from = from;
        this.to   = to;
        this.ammount = ammount;
    }

}
