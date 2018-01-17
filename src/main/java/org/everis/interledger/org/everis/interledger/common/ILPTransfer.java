package org.everis.interledger.org.everis.interledger.common;

import java.security.Timestamp;
import java.time.Instant;

import org.interledger.InterledgerAddress;
import org.interledger.cryptoconditions.Condition;
import org.interledger.cryptoconditions.Fulfillment;
import org.interledger.ilp.InterledgerPayment;

// TODO:(0) Remove this class and use standard ILP Packets.
public class ILPTransfer {

    private static int generator = 0;
    private final String ILP_TX_UUID;

    private final InterledgerAddress destinationAccount;
    private final String amount;
    private final Instant expiresAt;
    // TODO:(1) make final?

    private final InterledgerPayment payment;

    private final Condition condition;

        /**
     * @param destinationAccount
     * @param amount
     * @param payment ILP packet associated to the transfer. Right now we are not using it. Next step.
     */
    private ILPTransfer(
            String ILP_TX_UUID,
            InterledgerAddress destinationAccount,
            String amount,
            Instant expiresAt,
            InterledgerPayment payment,
            Condition condition) {

        this.ILP_TX_UUID = ILP_TX_UUID;
        this.destinationAccount = destinationAccount;

        this.amount = amount;
        this.expiresAt = expiresAt;
        this.payment = payment;

        this.condition = condition;
    }

    public String getUUID() {
        return ILP_TX_UUID;
    }

    public InterledgerAddress getDestinationAccount() {
        return destinationAccount;
    }

    public InterledgerPayment getPayment() {
        return payment;
    }

    public String getAmount() {
        return amount;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Condition getCondition() {
        return condition;
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
