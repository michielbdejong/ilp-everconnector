package org.everis.interledger.plugins;

import org.interledger.InterledgerAddress;
import org.interledger.ilp.InterledgerPayment;


public class Message {
    private final MessageId messageId;
    private final InterledgerAddress fromAddress;
    private final InterledgerAddress ledgerPrefix;
    private final InterledgerPayment interledgerPaymentPacket;
    //private final List<SubprotocolData> subprotocolData;


    public static class Builder {
        private MessageId messageId;
        private InterledgerAddress fromAddress;
        private InterledgerAddress ledgerPrefix;
        private InterledgerPayment interledgerPaymentPacket;


        public Builder messageId(MessageId messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder fromAddress(InterledgerAddress fromAddress) {
            this.fromAddress = fromAddress;
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

        public Message build() {
            return new Message(this);
        }
    }


    public Message (Builder builder) {
        this.messageId = builder.messageId;
        this.fromAddress = builder.fromAddress;
        this.ledgerPrefix = builder.ledgerPrefix;
        this.interledgerPaymentPacket = builder.interledgerPaymentPacket;
    }


    public MessageId getMessageId() {
        return messageId;
    }

    public InterledgerAddress getFromAddress() {
        return fromAddress;
    }

    public InterledgerAddress getLedgerPrefix() {
        return ledgerPrefix;
    }

    public InterledgerPayment getInterledgerPaymentPacket() {
        return interledgerPaymentPacket;
    }
}
