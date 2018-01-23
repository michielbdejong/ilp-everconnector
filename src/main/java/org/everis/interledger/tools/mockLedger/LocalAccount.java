package org.everis.interledger.tools.mockLedger;

import java.math.BigInteger;

/**
 * entity representing an account with an ILP ilpAddress, a password and a balance.
 *
 * credit and debit are removed to force balance movements just in the LocalLedger.execute(LocalTransfer tx)
 * That forces to always move money in the context of local transactions.
 * This will force extra testings like getting sure that the sum of initial and final balances are always
 * equals (double-entry accountancy principles)
 */
public class LocalAccount {
    final public String id;
    final public String password;
    private BigInteger /* TODO:(0) Move to BigInteger*/ balance;


    /**
     * Constructor for an account with an ILP ilpAddress, a password and a balance.
     * @param id
     * @param password
     * @param balance
     */
    public LocalAccount(String id, String password, BigInteger balance) {
        this.id = id;
        this.password = password;
        this.balance = balance;
    }
    public LocalAccount(LocalAccount other, BigInteger newBalance) {
        this(other.id, other.password, newBalance);
    }


    public String getPassword() {
        return password;
    }

    public BigInteger getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(this.id).append(" | ").append(balance);
        return str.toString();
    }

    @Override
    public boolean equals(Object other) {
        boolean result =  false;
        if (other == this) return true;
        if (other == null) return false;
        if (! (other instanceof  LocalAccount)) return false;
        LocalAccount lOther = (LocalAccount)other;
        return
            lOther.id.equals(id) &&
            lOther.password.equals(password) &&
            lOther.balance == balance;
    }
}
