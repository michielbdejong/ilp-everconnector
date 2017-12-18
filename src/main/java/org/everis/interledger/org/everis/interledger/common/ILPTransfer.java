package org.everis.interledger.org.everis.interledger.common;

import java.security.Timestamp;

import org.interledger.InterledgerAddress;
import org.interledger.cryptoconditions.Condition;
import org.interledger.cryptoconditions.Fulfillment;
import org.interledger.ilp.InterledgerPayment;

public class ILPTransfer {

    private class ErrorMessages {
        private ErrorMessages() {
        }

        private final static String NO_STATUS = "The transaction still do not have a status.";
        private final static String FLOW_STATUS = "The flow of status is incorrect";
    }

    public enum TransferStatus {
        PREPARED,
        EXECUTED,
        REJECTED
    }

    private static int generator = 0;
    private final String ILP_TX_UUID;

    private final InterledgerAddress destinationAccount;
    private final InterledgerAddress sourceAccount;
    private final Integer amount;

    private Timestamp expiration;
    // TODO:(1) make final?
    private TransferStatus status;

    private final InterledgerPayment payment;

    private final Condition condition;

    private final Fulfillment fulfillment;

        /**
     * @param sourceAccount
     * @param destinationAccount
     * @param amountValue
     * @param payment ILP packet associated to the transfer. Right now we are not using it. Next step.
     */
    private ILPTransfer(
            String ILP_TX_UUID,
            InterledgerAddress sourceAccount,
            InterledgerAddress destinationAccount,
            Integer amountValue,
            InterledgerPayment payment,
            Condition condition,
            Fulfillment fulfillment) {
        this.ILP_TX_UUID = ILP_TX_UUID;
        this.destinationAccount = destinationAccount;
        this.sourceAccount = sourceAccount;

        this.status = null;
        this.amount = amountValue;
        this.payment = payment;

        this.condition = condition;
        this.fulfillment = fulfillment;
    }

    /**
     * @param sourceAccount
     * @param destinationAccount
     * @param amountValue
     * @param payment ILP packet associated to the transfer. Right now we are not using it. Next step.
     */
    public ILPTransfer(
            String UUID,
            InterledgerAddress sourceAccount,
            InterledgerAddress destinationAccount,
            Integer amountValue,
            InterledgerPayment payment,
            Condition condition) {
        this(UUID, sourceAccount,
             destinationAccount,
             amountValue,
             payment,
             condition,
             null);
    }

    /**
     *  Recreates from existing transfer and new fulfillment
     *
     *  @throws RuntimeException if fulfillment does NOT match condition
     */
    public ILPTransfer(ILPTransfer other, Fulfillment newFulfillment) {
        this(other.ILP_TX_UUID,
             other.sourceAccount,
             other.destinationAccount,
             other.amount,
             other.payment,
             other.condition,
             newFulfillment);
        if (   other.fulfillment!=null &&
             ! other.fulfillment.equals(newFulfillment)) {
            throw new RuntimeException("Trying to re-asign a different fulfillment to a transfer that was " +
                    "already been asigned one");
        }
        if (!fulfillment.verify(condition, new byte[] {})) {
            throw new RuntimeException("fulfillment does NOT verify transfer condition");
        }
        this.status = other.status;
    }

    public String getUUID() {
        return ILP_TX_UUID;
    }

    public TransferStatus getStatus() throws RuntimeException {
        if (this.status != null) {
            return this.status;
        } else {
            throw new RuntimeException(ErrorMessages.NO_STATUS);
        }
    }

    /**
     * Change the status of the transfer to the transaction to PREPARED.
     * @throws RuntimeException Throws the exception in case of wrong flow of status.
     */
    public void setPreparedStatus() throws RuntimeException {
        if (this.status != null) {
            throw new RuntimeException(ErrorMessages.FLOW_STATUS);
        }
        this.status = TransferStatus.PREPARED;
    }

    /**
     * Change the status of the transfer to the transaction to EXECUTED.
     * @throws RuntimeException Throws the exception in case of wrong flow of status.
     */
    public void setExecutedStatus() throws RuntimeException {
        if (this.status != TransferStatus.PREPARED) {
            throw new RuntimeException(ErrorMessages.FLOW_STATUS);
        }
        this.status = TransferStatus.EXECUTED;
    }

    /**
     * Change the status of the transfer to the transaction to REJECTED.
     * @throws RuntimeException Throws the exception in case of wrong flow of status.
     */
    public void setRejectedStatus() throws RuntimeException {
        if (this.status != TransferStatus.PREPARED) {
            throw new RuntimeException(ErrorMessages.FLOW_STATUS);
        } else {
            this.status = TransferStatus.REJECTED;
        }
    }

    public InterledgerAddress getDestinationAccount() {
        return destinationAccount;
    }

    public InterledgerAddress getSourceAccount() {
        return sourceAccount;
    }

    public InterledgerPayment getPayment() {
        return payment;
    }

    public Integer getAmount() {
        return amount;
    }

    public Condition getCondition() {
        return condition;
    }

    public Fulfillment getFulfillment() {
        if (fulfillment == null) {
            throw new RuntimeException("Fulfillment not initialized");
        }
        return fulfillment;
    }

    @Override
    public String toString() {
        return "Transfer {" +
            "UUID=" + ILP_TX_UUID +
            ", destinationAccount=" + destinationAccount +
            ", sourceAccount=" + sourceAccount +
            ", amount=" + amount +
//            ", expiration=" + expiration +
            ", status=" + (status == null ? "NULL" : status) +
//            ", payment=" + payment +
//            ", condition=" + condition +
//            ", fulfillment=" + fulfillment +
            '}';
    }
}
