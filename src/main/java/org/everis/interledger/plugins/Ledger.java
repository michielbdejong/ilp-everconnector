package org.everis.interledger.plugins;

import org.interledger.InterledgerAddress;
import org.interledger.cryptoconditions.Fulfillment;

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
        } else if(this.ledgerAccounts.containsKey(pluginAccountAddress)) {
                String accountPassword = this.ledgerAccounts.get(pluginAccountAddress).getPassword();

                if(password.equals(accountPassword)) {
                    this.pluginsConnected.add(pluginAccountAddress);

                    return new LedgerInfo(this.ledgerPrefix.getPrefix(), this.ledgerCurrency);
                } else {
                    throw new RuntimeException("wrong password");
                }
            }
        else {
            throw new RuntimeException("account not exist in the ledger");
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

    public Account getAccountByAddress(InterledgerAddress accountAddress) {
        if(this.ledgerAccounts.containsKey(accountAddress)) {
            return this.ledgerAccounts.get(accountAddress);
        } else {
            throw new RuntimeException("account not exist");
        }
    }

    public void prepareTransaction(Transfer newTransfer) {
        Account sourceAccount = this.getAccountByAddress(newTransfer.getSourceAccount());

        newTransfer.prepareTransaction();
        sourceAccount.debitAccount(newTransfer.getAmount());
        HOLD_ACCOUNT.creditAccount(newTransfer.getAmount());
    }

    public void fulfillCondition(Transfer transfer, Fulfillment fulfillment) {
        Account destinationAccount = this.getAccountByAddress(transfer.getDestinationAccount());

        transfer.setFulfillment(fulfillment);
        transfer.executeTransaction();
        HOLD_ACCOUNT.debitAccount(transfer.getAmount());
        destinationAccount.creditAccount(transfer.getAmount());
    }

    public void rejectTransfer(Transfer rejectedTransfer) {
        Account sourceAccount = this.getAccountByAddress(rejectedTransfer.getSourceAccount());

        rejectedTransfer.rejectTransaction();
        HOLD_ACCOUNT.debitAccount(rejectedTransfer.getAmount());
        sourceAccount.creditAccount(rejectedTransfer.getAmount());
    }
}