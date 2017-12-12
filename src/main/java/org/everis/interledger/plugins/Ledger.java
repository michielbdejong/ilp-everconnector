package org.everis.interledger.plugins;

import org.interledger.InterledgerAddress;
import org.interledger.cryptoconditions.Fulfillment;

import javax.money.CurrencyUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic simulation entity of a ledger.
 */
public class Ledger {
    private Map<InterledgerAddress, Account> ledgerAccounts;
    private List<InterledgerAddress> pluginsConnected;
    private Map<Integer, Transfer> ledgerTransfers;
    private final InterledgerAddress ledgerPrefix;
    private final CurrencyUnit ledgerCurrency;
    private static Account HOLD_ACCOUNT;


    /**
     * Constructor's ledger with a prefix and a currency unit.
     * @param ledgerPrefix
     * @param ledgerCurrency
     */
    public Ledger(String ledgerPrefix, CurrencyUnit ledgerCurrency) {
        this.ledgerAccounts = new HashMap<InterledgerAddress, Account>();
        this.pluginsConnected = new ArrayList<InterledgerAddress>();
        this.ledgerTransfers = new HashMap<Integer, Transfer>();
        this.ledgerPrefix = InterledgerAddress.of(ledgerPrefix);
        this.ledgerCurrency = ledgerCurrency;
        //create a hold account specific for this ledger
        this.HOLD_ACCOUNT = new Account(InterledgerAddress.of("test1.holdAccount"), "password", 0);
    }


    /**
     * add an account to the ledger.
     * @param newAccount
     */
    public void addAccount(Account newAccount) {
        this.ledgerAccounts.put(newAccount.getAccountAddress(), newAccount);
    }

    /**
     * connect a plugin with the ledger via the plugin's account on the ledger.
     * @param pluginConnection
     */
    public void connect(PluginConnection pluginConnection) {
        InterledgerAddress pluginAccountAddress = pluginConnection.getPluginAccountAddress();
        String password = pluginConnection.getPluginAccountPassword();

        if(this.pluginsConnected.contains(pluginAccountAddress)) {
            throw new RuntimeException("plugin already connected");
        } else if(this.ledgerAccounts.containsKey(pluginAccountAddress)) {
            String accountPassword = this.ledgerAccounts.get(pluginAccountAddress).getPassword();

            if(password.equals(accountPassword)) {
                this.pluginsConnected.add(pluginAccountAddress);
            } else {
                throw new RuntimeException("wrong password");
            }
        }
        else {
            throw new RuntimeException("account not exist in the ledger");
        }
    }

    /**
     * disconnect a plugin from the ledger.
     * @param pluginAccountAddress
     */
    public void disconnect(InterledgerAddress pluginAccountAddress) {
        if(this.pluginsConnected.contains(pluginAccountAddress)) {
            this.pluginsConnected.remove(pluginAccountAddress);
        } else {
            throw new RuntimeException("plugin not connected");
        }
    }

    /**
     * check if a plugin is right connected with a ledger.
     * @param pluginAccountAddress
     * @return boolean
     */
    public boolean isPluginConnected(InterledgerAddress pluginAccountAddress) {
        if(this.pluginsConnected.contains(pluginAccountAddress)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * get an account from its ilp address
     * @param accountAddress
     * @return Account
     */
    public Account getAccountByAddress(InterledgerAddress accountAddress) {
        if(this.ledgerAccounts.containsKey(accountAddress)) {
            return this.ledgerAccounts.get(accountAddress);
        } else {
            throw new RuntimeException("account not exist");
        }
    }

    /**
     * first step of the ILP flow to prepare a transfer by transferring the funds on the hold account and put
     * the transfer status as "PREPARED".
     * @param newTransfer
     */
    public void prepareTransaction(Transfer newTransfer) {
        if(!this.ledgerTransfers.containsKey(newTransfer.getId())) {
            Account sourceAccount = getAccountByAddress(newTransfer.getSourceAccount());

            if(!this.ledgerTransfers.containsKey(newTransfer.getId())) {
                this.ledgerTransfers.put(newTransfer.getId(), newTransfer);
                sourceAccount.debitAccount(newTransfer.getAmount());
                HOLD_ACCOUNT.creditAccount(newTransfer.getAmount());
                newTransfer.prepareTransaction();
            } else {
                throw new RuntimeException("transfer already exist");
            }
        } else {
            throw new RuntimeException("transfer already processed");
        }
    }

    /**
     * Fulfill the condition of the transfer by receiver when he agree with the conditions of the transfer.
     * @param transferId
     * @param fulfillment
     */
    public void fulfillCondition(int transferId, Fulfillment fulfillment) {
        if(this.ledgerTransfers.containsKey(transferId)) {
            Transfer transfer = this.ledgerTransfers.get(transferId);
            Account destinationAccount = this.getAccountByAddress(transfer.getDestinationAccount());

            transfer.setFulfillment(fulfillment);
            HOLD_ACCOUNT.debitAccount(transfer.getAmount());
            destinationAccount.creditAccount(transfer.getAmount());
            transfer.executeTransaction();
        } else {
            throw new RuntimeException("transfer not exist");
        }
    }

    /**
     * reject the transfer by the receiver when the transfer'd conditions are not respected or the timeout is passed.
     * @param transferId
     */
    public void rejectTransfer(int transferId) {
        if(this.ledgerTransfers.containsKey(transferId)) {
            Transfer rejectedTransfer = this.ledgerTransfers.get(transferId);
            Account sourceAccount = this.getAccountByAddress(rejectedTransfer.getSourceAccount());

            rejectedTransfer.rejectTransaction();
            HOLD_ACCOUNT.debitAccount(rejectedTransfer.getAmount());
            sourceAccount.creditAccount(rejectedTransfer.getAmount());
        } else {
            throw new RuntimeException("transfer not exist");
        }
    }

    //getters for testing
    public Map<InterledgerAddress, Account> getLedgerAccounts() {
        return ledgerAccounts;
    }

    public List<InterledgerAddress> getPluginsConnected() {
        return pluginsConnected;
    }

    public Map<Integer, Transfer> getLedgerTransfers() {
        return ledgerTransfers;
    }

    public InterledgerAddress getLedgerPrefix() {
        return ledgerPrefix;
    }

    public CurrencyUnit getLedgerCurrency() {
        return ledgerCurrency;
    }

    public Account getHoldAccount() {
        return HOLD_ACCOUNT;
    }
}