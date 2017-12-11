package org.everis.interledger.plugins;

import org.interledger.InterledgerAddress;

import javax.money.CurrencyUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ledger {
    private Map<InterledgerAddress, Account> ledgerAccounts;
    private List<InterledgerAddress> pluginsConnected;
    private Map<InterledgerAddress, Transfer> ledgerTransfers;
    private final InterledgerAddress ledgerPrefix;
    private final CurrencyUnit ledgerCurrency;
    private static Account HOLD_ACCOUNT;


    public Ledger(String ledgerPrefix, CurrencyUnit ledgerCurrency) {
        this.ledgerAccounts = new HashMap<InterledgerAddress, Account>();
        this.pluginsConnected = new ArrayList<InterledgerAddress>();
        this.ledgerTransfers = new HashMap<InterledgerAddress, Transfer>();
        this.ledgerPrefix = InterledgerAddress.of(ledgerPrefix);
        this.ledgerCurrency = ledgerCurrency;
        this.HOLD_ACCOUNT = new Account(InterledgerAddress.of("holdAccount"), "password", 0);

    }


    public void addAccount(Account newAccount) {
        this.ledgerAccounts.put(newAccount.getAccountAddress(), newAccount);
    }

    public LedgerInfo connect(InterledgerAddress pluginAccountAddress, String password) {
        if(this.pluginsConnected.contains(pluginAccountAddress)) {
            throw new RuntimeException("plugin already connected");
        } else {
            if(this.ledgerAccounts.containsKey(pluginAccountAddress)) {
                String accountPassword = this.ledgerAccounts.get(pluginAccountAddress).getPassword();

                if(password.equals(accountPassword)) {
                    this.pluginsConnected.add(pluginAccountAddress);

                    return new LedgerInfo(this.ledgerPrefix.getPrefix(), this.ledgerCurrency);
                } else {
                    throw new RuntimeException("wrong password");
                }
            } else {
                throw new RuntimeException("account not exist in the ledger");
            }
        }
    }

    public void disconnect(InterledgerAddress pluginAccountAddress) {
        if(this.pluginsConnected.contains(pluginAccountAddress)) {
            this.pluginsConnected.remove(pluginAccountAddress);
        } else {
            throw new RuntimeException("plugin not connected");
        }
    }

    public boolean isPluginConnected(InterledgerAddress pluginAccountAddress) {
        if(this.pluginsConnected.contains(pluginAccountAddress)) {
            return true;
        } else {
            return false;
        }
    }

    public void prepareTransaction() {

    }

    public void fulfillCondition() {

    }
}
