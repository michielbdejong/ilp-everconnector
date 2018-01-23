package org.everis.interledger.org.everis.interledger.common;

import java.security.Timestamp;
import java.time.Instant;

import org.interledger.InterledgerAddress;
import org.interledger.cryptoconditions.Condition;
import org.interledger.cryptoconditions.Fulfillment;
import org.interledger.ilp.InterledgerPayment;

// TODO:(0.5) Try to remove and use InterledgerPayment from Quilt?

public class ILPTransfer {

    public final String ILP_TX_UUID;
    public final InterledgerAddress destinationAccount;
    public final String amount;
    public final Instant expiresAt;
    public final Condition condition;
    // TODO:(1) make final?
    public final byte[] endToEndData;


    /**
     *
     * @param ILP_TX_UUID
     * @param destinationAccount
     * @param amount
     * @param expiresAt
     * @param condition
     * @param endToEndData
     */
    public ILPTransfer(
            String ILP_TX_UUID,
            InterledgerAddress destinationAccount,
            String amount,
            Instant expiresAt,
            Condition condition,
            byte[] endToEndData
            ) {

        this.ILP_TX_UUID = ILP_TX_UUID;
        if (destinationAccount.isLedgerPrefix()) {
            throw new RuntimeException("'"+destinationAccount.getValue()+"'" +
                    " can NOT be a ledger prefix. " +
                    " It must be a valid destination account");
        }
        this.destinationAccount = destinationAccount;

        this.amount = amount;
        this.expiresAt = expiresAt;
        this.endToEndData = endToEndData;

        this.condition = condition;
    }

    @Override
    public String toString() {
        return "Transfer {" +
            "UUID=" + ILP_TX_UUID +
            ", destinationAccount=" + destinationAccount +
            ", amount=" + amount +
            ", expiration=" + expiresAt +
            '}';
    }
}
