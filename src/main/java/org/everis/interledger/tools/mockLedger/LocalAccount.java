package org.everis.interledger.tools.mockLedger;

import org.interledger.InterledgerAddress;

/**
 * entity representing an account with an ILP address, a password and a balance.
 */
public class LocalAccount {
    final public String id;
    final public String password;
    private int /* TODO:(0) Move to BigInteger*/ balance;


    /**
     * Constructor for an account with an ILP address, a password and a balance.
     * @param id
     * @param password
     * @param balance
     */
    public LocalAccount(String id, String password, int balance) {
        this.id = id;
        this.password = password;
        this.balance = balance;
    }

    /**
     * deposit the amount on the account.
     * @param amount
     */
    public void creditAccount(int amount) { this.balance += amount;}

    /**
     * withdraw the amount on the account.
     * @param amount
     */
    public void debitAccount(int amount) {
        this.balance -= amount;
    }


    public String getPassword() {
        return password;
    }

    public int getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(this.id).append(" | ").append(balance);
        return str.toString();
    }
}
