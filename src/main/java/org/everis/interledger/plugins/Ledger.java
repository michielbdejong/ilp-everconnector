package org.everis.interledger.plugins;

import java.util.Locale;
import javax.money.Monetary;
import org.interledger.InterledgerAddress;
import org.interledger.cryptoconditions.Fulfillment;

import javax.money.CurrencyUnit;
import java.util.HashMap;
import java.util.Map;

public class Ledger {

    private Map<InterledgerAddress, Account> ledgerAccounts;
    private Map<InterledgerAddress, PluginConnection> pluginsConnected;
    private Map<Integer, Transfer> ledgerTransfers;
    private final LedgerInfo info;
    private static Account HOLD_ACCOUNT;

    /**
     * Construction of a default Ledger.
     */
    public Ledger() {
        this("test1.everis.",  Monetary.getCurrency(Locale.UK));
    }

    public Ledger(String ledgerPrefix, CurrencyUnit ledgerCurrency) {
        this.ledgerAccounts = new HashMap<InterledgerAddress, Account>();
        this.pluginsConnected = new HashMap<InterledgerAddress, PluginConnection>();
        this.ledgerTransfers = new HashMap<Integer, Transfer>();
        this.info = new LedgerInfo(InterledgerAddress.of(ledgerPrefix), ledgerCurrency);

        InterledgerAddress addressHoldAccount = InterledgerAddress.of(ledgerPrefix + "holdAccount");
        this.HOLD_ACCOUNT = new Account(addressHoldAccount, "password", 0);
    }

    public LedgerInfo getInfo() {
        return info;
    }

    public void addAccount(Account newAccount) {
        InterledgerAddress newAccountAddress = newAccount.getAccountAddress();
        if (!this.ledgerAccounts.containsKey(newAccountAddress)) {
            this.ledgerAccounts.put(newAccountAddress, newAccount);
        } else {
            throw new RuntimeException("the account " + newAccountAddress + " already exist.");
        }
    }

    public LedgerInfo connect(PluginConnection pluginConnection) {
        InterledgerAddress accountAddress = pluginConnection.getAccount();
        if(this.pluginsConnected.containsKey(accountAddress) ) {
            throw new RuntimeException("plugin already connected");
        } else if(this.ledgerAccounts.containsKey(accountAddress)) {
            String accountPassword = this.ledgerAccounts.get(accountAddress).getPassword();
            if(pluginConnection.getPassword().equals(accountPassword)) {
                this.pluginsConnected.put(accountAddress, pluginConnection);
                return this.info;
            } else {
                throw new RuntimeException("wrong password");
            }
        } else {
            throw new RuntimeException("account not exist in the ledger");
        }
    }

    public void disconnect(InterledgerAddress pluginAccountAddress) {
        if(this.pluginsConnected.containsKey(pluginAccountAddress)) {
            this.pluginsConnected.remove(pluginAccountAddress);
        } else {
            throw new RuntimeException("plugin not connected");
        }
    }

    public boolean isPluginConnected(InterledgerAddress pluginAccountAddress) {
        return this.pluginsConnected.containsKey(pluginAccountAddress);
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

        sourceAccount.debitAccount(newTransfer.getAmount());
        HOLD_ACCOUNT.creditAccount(newTransfer.getAmount());
        newTransfer.setPreparedStatus();

        storeTranfer(newTransfer);
    }

    public void fulfillCondition(Transfer transfer, Fulfillment fulfillment) {
        Account destinationAccount = this.getAccountByAddress(transfer.getDestinationAccount());

        transfer.setFulfillment(fulfillment);
        HOLD_ACCOUNT.debitAccount(transfer.getAmount());
        destinationAccount.creditAccount(transfer.getAmount());
        transfer.setExecutedStatus();
    }

    public void rejectTransfer(Transfer rejectedTransfer) {
        Account sourceAccount = this.getAccountByAddress(rejectedTransfer.getSourceAccount());

        HOLD_ACCOUNT.debitAccount(rejectedTransfer.getAmount());
        sourceAccount.creditAccount(rejectedTransfer.getAmount());
        rejectedTransfer.setRejectedStatus();
    }

    private void storeTranfer(Transfer newTransfer) {
        int idTransfer = newTransfer.getId();
        if (!this.ledgerTransfers.containsKey(idTransfer)) {
            this.ledgerTransfers.put(idTransfer, newTransfer);
        } else {
            throw new RuntimeException("The transfer " + idTransfer + " is already tracked");
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(this.info.toString());
        str.append("\n" + this.printAccounts());
        str.append("\n" + this.printPluginConnections());
        str.append("\n" + this.printTransactions() + "\n");
        return str.toString();
    }

    public String printAccounts() {
        StringBuilder str = new StringBuilder();
        str.append("-LEDGER-ACCOUNTS---------");
        str.append("\n" + HOLD_ACCOUNT);
        str.append("\n-------------------------");
        if (!this.ledgerAccounts.isEmpty()) {
            for (InterledgerAddress addressAccount : this.ledgerAccounts.keySet()) {
                str.append("\n" + this.ledgerAccounts.get(addressAccount));
            }
        } else {
            str.append("\n-NO-ACCOUNTS-------------");
        }
        str.append("\n-------------------------");
        return str.toString();
    }

    public String printPluginConnections() {
        StringBuilder str = new StringBuilder();
        str.append("-PLUGIN-CONNECTIONS------");
        if (!this.pluginsConnected.isEmpty()) {
            for (InterledgerAddress pluginConnected : this.ledgerAccounts.keySet()) {
                str.append("\n" + this.pluginsConnected.get(pluginConnected));
            }
        } else {
            str.append("\n-NO-CONNECTIONS----------");
        }
        str.append("\n-------------------------");
        return str.toString();
    }

    public String printTransactions() {
        StringBuilder str = new StringBuilder();
        str.append("-LEDGER-TRANSACTIONS-----");
        if (!this.ledgerTransfers.isEmpty()) {
            for (int idTransaction : this.ledgerTransfers.keySet()) {
                str.append("\n" + this.ledgerTransfers.get(idTransaction));
            }
        } else {
            str.append("\n-NO-TRANSACTIONS---------");
        }
        str.append("\n-------------------------");
        return str.toString();
    }
}