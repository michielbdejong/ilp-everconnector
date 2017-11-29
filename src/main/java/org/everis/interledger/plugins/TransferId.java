package org.everis.interledger.plugins;

import java.util.UUID;

public class TransferId {
    private final UUID value;


    public static class Builder {
        private UUID value;


        public Builder value(UUID value) {
            this.value = value;
            return this;
        }

        public TransferId build() {
            return new TransferId(this);
        }
    }


    public TransferId(Builder builder) {
        this.value = builder.value;
    }


    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransferId)) return false;

        TransferId that = (TransferId) o;

        return getValue().equals(that.getValue());
    }

    @Override
    public int hashCode() {
        return getValue().hashCode();
    }
}
