package org.everis.interledger.plugins;

import java.security.Timestamp;

import org.interledger.InterledgerAddress;
import org.interledger.cryptoconditions.Condition;
import org.interledger.cryptoconditions.Fulfillment;
import org.interledger.ilp.InterledgerPayment;

public class Transfer {

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
    private int id;

    private InterledgerAddress destinationAccount;
    private InterledgerAddress sourceAccount;
    private Integer amount;

    private Timestamp expiration;
    private TransferStatus status;

    private InterledgerPayment payment;

    private Condition condition;
    private Fulfillment fulfillment;

    public Transfer(

            InterledgerAddress sourceAccount,
            InterledgerAddress destinationAccount,
            Integer amountValue/*,
            InterledgerPayment payment*/) {

        this.id = generator;
        generator = generator + 1;

        this.destinationAccount = destinationAccount;
        this.sourceAccount = sourceAccount;

        this.status = null;
        this.amount = amountValue;
        //this.payment = payment;
    }

    public Integer getId() {
        return id;
    }

    public TransferStatus getStatus() {
        if (this.status != null) {
            return this.status;
        } else {
            throw new RuntimeException(ErrorMessages.NO_STATUS);
        }
    }

    public void prepareTransaction() {
        if (this.status != null) {
            throw new RuntimeException(ErrorMessages.FLOW_STATUS);
        } else {
            this.status = TransferStatus.PREPARED;
        }
    }

    public void executeTransaction() {
        if (this.status != TransferStatus.PREPARED) {
            throw new RuntimeException(ErrorMessages.FLOW_STATUS);
        } else {
            this.status = TransferStatus.EXECUTED;
        }
    }

    public void rejectTransaction() {
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

    public void setPayment(InterledgerPayment paymentValue) {
        this.payment = paymentValue;
    }

    public Integer getAmount() {
        return amount;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition conditionValue) {
        this.condition = conditionValue;
    }

    public Fulfillment getFulfillment() {
        return fulfillment;
    }

    public void setFulfillment(Fulfillment fulfillmentValue) {
        this.fulfillment = fulfillmentValue;
    }
}
