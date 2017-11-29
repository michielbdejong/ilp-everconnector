package org.everis.interledger.plugins;

import java.util.UUID;

public class MessageId {
    private final UUID value;


    public static class Builder {
        private UUID value;


        public Builder value(UUID value) {
            this.value = value;
            return this;
        }

        public MessageId build() {
            return new MessageId(this);
        }
    }


    public MessageId(Builder builder) {
        this.value = builder.value;
    }


    public UUID getValue() {
        return value;
    }
}
