package org.everis.interledger.tools.mockLedger;

import org.everis.interledger.org.everis.interledger.common.LedgerInfo;
import org.everis.interledger.org.everis.interledger.common.ILPTransfer;
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
// TODO:(0) Move Ledger outside plugin. Maybe to org.everis.interledger.devtools.mockledger
//      since it's no more than an temporal utility class to help during ledger the development.
// TODO:(?) Use configurable sleep... to simulate long TX times (Bitcoin, ...)
public class MockInMemoryLedger {

    private class ConnectedPlugin {
        public final InterledgerAddress connectorILPAddress;
        public final LocalAccount connectorAccount;

        /**
         *  Helper class to keep a trace of connectors connected
         * @param connectorILPAddress
         * @param connectorAccount
         */
        public ConnectedPlugin(InterledgerAddress connectorILPAddress, LocalAccount connectorAccount) {
            this.connectorILPAddress = connectorILPAddress;
            this.connectorAccount = connectorAccount;
        }

}
    // TODO:(0) Use real Interledger Errors instead of RuntimeException<.

    // Non ILP Related. All this data exists on the ledger
    // even before adding any ILP related support
    private final Map<String /* id */, LocalAccount> ledgerAccounts;
    private final InterledgerAddress ledgerPrefix;
    private final CurrencyUnit ledgerCurrency;
    // Keeps track of local ledger Transfers
    private final List<LedgerTransfer> logOfLocalTransfers = new ArrayList<LedgerTransfer>();


    // ILP related. This data is needed to support ILP.
    // Notice an ILPTransfer maps to different LocalTransfers
    private final Map<InterledgerAddress, ConnectedPlugin> pluginsConnected;
    // Keeps track of out-standing ILP Transfers (prepared but not yet executed or rejected)
    private final Map<String, ILPTransfer> ilpPendingTransfers;
    private static LocalAccount HOLD_ACCOUNT;


    public LedgerInfo getInfo(){
        return new LedgerInfo(this.ledgerPrefix, this.ledgerCurrency);
    }

    public Map<String, LocalAccount> getLedgerAccounts(){
        return new HashMap<String, LocalAccount>(this.ledgerAccounts);
    }

    /**
     * Constructor's ledger with a prefix and a currency unit.
     * @param ledgerPrefix
     * @param ledgerCurrency
     */
    public MockInMemoryLedger(InterledgerAddress ledgerPrefix, CurrencyUnit ledgerCurrency) {
        this.ledgerAccounts = new HashMap<String /*id */, LocalAccount>();
        this.pluginsConnected = new HashMap<InterledgerAddress, ConnectedPlugin>();
        this.ilpPendingTransfers = new HashMap<String, ILPTransfer>();
        this.ledgerPrefix = ledgerPrefix;
        this.ledgerCurrency = ledgerCurrency;
        //create a hold account specific for this ledger
        this.HOLD_ACCOUNT = new LocalAccount("@HOLD_ACCOUNT@", "password", 0);
        addAccount(this.HOLD_ACCOUNT);
    }

    /**
     * add an account to the ledger.
     * @param newAccount
     */
    public void addAccount(LocalAccount newAccount) {
        if (!this.ledgerAccounts.containsKey(newAccount.id)) {
            throw new RuntimeException("the account " + newAccount.id + " already exist.");
        }
        this.ledgerAccounts.put(newAccount.id, newAccount);
    }

    /**
     * connect request from external plugin
     * @param connectorAddress  ILP ConnectorAddress
     * @param accountId         Connector account in ledger
     * @param password          Credentials used for "AAA"
     */
    public void onConnectRequest(InterledgerAddress connectorAddress, String accountId, String password) {
        if(this.pluginsConnected.containsKey(accountId)) {
            throw new RuntimeException("plugin already connected");
        }
        if(!this.ledgerAccounts.containsKey(accountId)) {
            throw new RuntimeException("account not exist in the ledger");
        }
        LocalAccount connectorLocalAccount = this.ledgerAccounts.get(accountId);
        String accountPassword = connectorLocalAccount.getPassword();
        if(!password.equals(accountPassword)) {
            throw new RuntimeException("wrong password");
        }
        this.pluginsConnected.put(connectorAddress, new ConnectedPlugin(connectorAddress, connectorLocalAccount));
    }

    /**
     * disconnect a plugin from the ledger.
     * @param pluginAccountAddress
     */
    public void disconnect(InterledgerAddress pluginAccountAddress) {
        if(!this.pluginsConnected.containsKey(pluginAccountAddress)) {
            throw new RuntimeException("plugin not connected");
        }
        this.pluginsConnected.remove(pluginAccountAddress);
    }

    /**
     * check if a plugin is right connected with a ledger.
     * @param pluginAccountAddress
     * @return boolean
     */
    public boolean isPluginConnected(InterledgerAddress pluginAccountAddress) {
        return this.pluginsConnected.containsKey(pluginAccountAddress);
    }

    /**
     * get an account from its ilp address
     * @param accountAddress
     * @return Account
     */
    public LocalAccount getAccountByAddress(InterledgerAddress accountAddress) {
        String local_account = accountAddress.getValue().replaceAll(this.ledgerPrefix.getValue(),"");
        if(!this.ledgerAccounts.containsKey(local_account)) {
            throw new RuntimeException("account does NOT exist");
        }
        return this.ledgerAccounts.get(accountAddress);
    }



    /**
     * first step of the ILP flow to prepare a transfer by transferring the funds on the hold account and put
     * the transfer status as "PREPARED".
     * @param newTransfer
     */
    public void prepareTransaction(ILPTransfer newTransfer) {
        if(this.ilpPendingTransfers.containsKey(newTransfer.getUUID())) {
            throw new RuntimeException("duplicated UUID");
        }
        LocalAccount sourceAccount = getAccountByAddress(newTransfer.getSourceAccount());

        // Atomic {
        this.ilpPendingTransfers.put(newTransfer.getUUID(), newTransfer);
        sourceAccount.debitAccount(newTransfer.getAmount());
        HOLD_ACCOUNT.creditAccount(newTransfer.getAmount());
        newTransfer.setPreparedStatus();
        // }
    }

    /**
     * Fulfill the condition of the transfer by receiver when he agree with the conditions of the transfer.
     * @param transferId
     * @param fulfillment
     */
    public void fulfillCondition(int transferId, Fulfillment fulfillment) {
        if(!this.ilpPendingTransfers.containsKey(transferId)) {
            throw new RuntimeException("transfer not exist");
        }
        ILPTransfer transfer = this.ilpPendingTransfers.get(transferId);

        if (transfer.getStatus() != ILPTransfer.TransferStatus.PREPARED) {
            throw new RuntimeException("trying to fulfill, but transfer is NOT in prepared state");
        }

        transfer = new ILPTransfer(transfer, fulfillment);
        LocalAccount destinationAccount = this.getAccountByAddress(transfer.getDestinationAccount());
        // NOTE: Next operations must be atomic or rollback
        {
            HOLD_ACCOUNT.debitAccount(transfer.getAmount());
            destinationAccount.creditAccount(transfer.getAmount());
            transfer.setExecutedStatus();
        }
    }

    /**
     * reject the transfer by the receiver when the transfer'd conditions are not respected or the timeout is passed.
     * @param transferId
     */
    public void rejectTransfer(int transferId) {
        if(this.ilpPendingTransfers.containsKey(transferId)) {
            ILPTransfer rejectedTransfer = this.ilpPendingTransfers.get(transferId);
            LocalAccount sourceAccount = this.getAccountByAddress(rejectedTransfer.getSourceAccount());

            rejectedTransfer.setRejectedStatus();
            HOLD_ACCOUNT.debitAccount(rejectedTransfer.getAmount());
            sourceAccount.creditAccount(rejectedTransfer.getAmount());
        } else {
            throw new RuntimeException("transfer not exist");
        }
    }

    public LocalAccount getHoldAccount() {
        return HOLD_ACCOUNT;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(new LedgerInfo(this.ledgerPrefix, this.ledgerCurrency));
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
            for (String addressAccount : this.ledgerAccounts.keySet()) {
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
            for (InterledgerAddress key : this.pluginsConnected.keySet()) {
                str.append("\n" + this.pluginsConnected.get(key));
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
        if (!this.ilpPendingTransfers.isEmpty()) {
            for (String key : this.ilpPendingTransfers.keySet()) {
                str.append("\n" + this.ilpPendingTransfers.get(key));
            }
        } else {
            str.append("\n-NO-TRANSACTIONS---------");
        }
        str.append("\n-------------------------");
        return str.toString();
    }

}