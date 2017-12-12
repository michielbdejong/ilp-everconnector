package org.everis.interledger.plugins;

import org.interledger.InterledgerAddress;

public class PluginConnection {

    private InterledgerAddress account;
    private String password;

    public PluginConnection(InterledgerAddress account, String password) {
        this.account = account;
        this.password = password;
    }

    public PluginConnection(Account linkedAccount) {
        this.account = linkedAccount.getAccountAddress();
        this.password = linkedAccount.getPassword();
    }

    public InterledgerAddress getAccount() {
        return account;
    }

    public void setAccount(InterledgerAddress accountValue) {
        this.account = accountValue;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String passwordValue) {
        this.password = passwordValue;
    }

    @Override
    public String toString() {
        return "Plugin connector of " + account;
    }
}
