package org.everis.interledger.plugins;

import org.interledger.InterledgerAddress;

public class Account {
    private InterledgerAddress accountAddress;
    private String password;
    private int balance;


    public Account(InterledgerAddress accountAddress, String password, int balance) {
        this.accountAddress = accountAddress;
        this.password = password;
        this.balance = balance;
    }

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

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public void creditAccount(int amount) { this.balance += amount;}

    public void debitAccount(int amount) { this.balance -= amount;}
}
