package org.everis.interledger.plugins;

import org.interledger.InterledgerAddress;
import org.interledger.cryptoconditions.Condition;
import org.interledger.ilp.InterledgerPayment;
//import org.interledger.subprotocols.SubprotocolData;

import java.math.BigInteger;
import java.time.Instant;
//import java.util.List;

public class Transfer {
    private final TransferId transferId;
    private final InterledgerAddress sourceAccount;
    private final BigInteger amount;
    private final InterledgerAddress destinationAccount;
    private final InterledgerAddress ledgerPrefix;
    private final InterledgerPayment interledgerPaymentPacket;
    private final Condition executionCondition;
    private final Condition cancellationCondition;
    private final Instant expireAt;
    //private final List<SubprotocolData> subprotocolData;


    public static class Builder {
        private TransferId transferId;
        private InterledgerAddress sourceAccount;
        private BigInteger amount;
        private InterledgerAddress destinationAccount;
        private InterledgerAddress ledgerPrefix;
        private InterledgerPayment interledgerPaymentPacket;
        private Condition executionCondition;
        private Condition cancellationCondition;
        private Instant expireAt;


        public Builder transferId(TransferId transferId) {
            this.transferId = transferId;
            return this;
        }

        public Builder sourceAccount(InterledgerAddress sourceAccount) {
            this.sourceAccount = sourceAccount;
            return this;
        }

        public Builder amount(BigInteger amount) {
            this.amount = amount;
            return this;
        }

        public Builder destinationAccount(InterledgerAddress destinationAccount) {
            this.destinationAccount = destinationAccount;
            return this;
        }

        public Builder ledgerPrefix(InterledgerAddress ledgerPrefix) {
            this.ledgerPrefix = ledgerPrefix;
            return this;
        }

        public Builder interledgerPaymentPacket(InterledgerPayment interledgerPaymentPacket) {
            this.interledgerPaymentPacket = interledgerPaymentPacket;
            return this;
        }

        public Builder executionCondition(Condition executionCondition) {
            this.executionCondition = executionCondition;
            return this;
        }

        public Builder cancellationCondition(Condition cancellationCondition) {
            this.cancellationCondition = cancellationCondition;
            return this;
        }

        public Builder expireAt(Instant expireAt) {
            this.expireAt = expireAt;
            return this;
        }

        public Transfer build() {
            return new Transfer(this);
        }
    }


    public Transfer(Builder builder) {
        this.transferId = builder.transferId;
        this.sourceAccount = builder.sourceAccount;
        this.amount = builder.amount;
        this.destinationAccount = builder.destinationAccount;
        this.ledgerPrefix = builder.ledgerPrefix;
        this.interledgerPaymentPacket = builder.interledgerPaymentPacket;
        this.executionCondition = builder.executionCondition;
        this.cancellationCondition = builder.cancellationCondition;
        this.expireAt = builder.expireAt;
    }


    public TransferId getTransferId() {
        return transferId;
    }

    public InterledgerAddress getSourceAccount() {
        return sourceAccount;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public InterledgerAddress getDestinationAccount() {
        return destinationAccount;
    }

    public InterledgerAddress getLedgerPrefix() {
        return ledgerPrefix;
    }

    public InterledgerPayment getInterledgerPaymentPacket() {
        return interledgerPaymentPacket;
    }

    public Condition getExecutionCondition() {
        return executionCondition;
    }

    public Condition getCancellationCondition() {
        return cancellationCondition;
    }

    public Instant getExpireAt() {
        return expireAt;
    }
}
