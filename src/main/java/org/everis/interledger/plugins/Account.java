package org.everis.interledger.plugins;

import org.interledger.InterledgerAddress;

/**
 * entity representing an account with an ILP address, a password and a balance.
 */
public class Account {
    private InterledgerAddress accountAddress;
    private String password;
    private int balance;


    /**
     * Constructor for an account with an ILP address, a password and a balance.
     * @param accountAddress
     * @param password
     * @param balance
     */
    public Account(InterledgerAddress accountAddress, String password, int balance) {
        this.accountAddress = accountAddress;
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

    //getters ans setters
    public InterledgerAddress getAccountAddress() {
        return accountAddress;
    }

    public void setAccountAddress(InterledgerAddress accountAddress) {
        this.accountAddress = accountAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(this.accountAddress + " | " + balance);
        return str.toString();
    }
}
