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

    /**
     *  InternalLedger represents the ledger *before* adding ILP support.
     *  It must not contain any reference to ILP structures. In practice this
     *  class could represent a main-frame , a blockchain, ...
     */
    class InternalLedger {
        private final Map<String /* id */, LocalAccount> ledgerAccounts;
        private final CurrencyUnit ledgerCurrency;
        // Keeps track of local ledger Transfers
        private final List<LedgerTransfer> logOfLocalTransfers = new ArrayList<LedgerTransfer>();

        InternalLedger(CurrencyUnit ledgerCurrency) {
            this.ledgerAccounts = new HashMap<String /*id */, LocalAccount>();
            this.ledgerCurrency = ledgerCurrency;
        }
        /**
         * add an account to the ledger.
         * @param newAccount
         */
        public void addAccount(LocalAccount newAccount) {
            if (this.ledgerAccounts.containsKey(newAccount.id)) {
                throw new RuntimeException("the account " + newAccount.id + " already exist.");
            }
            this.ledgerAccounts.put(newAccount.id, newAccount);
        }

        public LocalAccount getLocalAccount(String accountId){
            if(!this.ledgerAccounts.containsKey(accountId)) {
                throw new RuntimeException("account not exist in the ledger");
            }
            return this.ledgerAccounts.get(accountId);
        }

        public void executeTransfer(LedgerTransfer internalTransfer) {
            if(!this.ledgerAccounts.containsKey(internalTransfer.from.id)) {
                throw new RuntimeException("internal transfer creditor (from) '"+internalTransfer.from.id+"' not found" );
            }
            if(!this.ledgerAccounts.containsKey(internalTransfer.to.id)) {
                throw new RuntimeException("internal transfer debitor (to) '"+internalTransfer.from.id+"' not found" );
            }
            if (internalTransfer.from.getBalance() < internalTransfer.ammount ) {
                throw new RuntimeException("not enough funds");
            }
            internalTransfer.from.debitAccount(internalTransfer.ammount);
            internalTransfer.to.creditAccount(internalTransfer.ammount);
            logOfLocalTransfers.add(internalTransfer);
        }

    }

    public int debugTotalAccounts(){
       return this.internalLedger.ledgerAccounts.keySet().size();
    }

    public void addAccount(LocalAccount account) {
        this.internalLedger.addAccount(account);
    }

    public final InternalLedger internalLedger;

    // ILP related. This data is needed to support ILP.
    // Notice an ILPTransfer maps to different LocalTransfers
    private final InterledgerAddress ledgerPrefix;
    private final Map<InterledgerAddress, ConnectedPlugin> pluginsConnected;
    // Keeps track of out-standing ILP Transfers (prepared but not yet executed or rejected)
    private final Map<String, ILPTransfer> ilpPendingTransfers;
    private final LocalAccount ILP_HOLD_ACCOUNT;

    public LedgerInfo getInfo(){
        return new LedgerInfo(this.ledgerPrefix, this.internalLedger.ledgerCurrency);
    }


    /**
     * Constructor's ledger with a prefix and a currency unit.
     * @param ledgerPrefix
     * @param ledgerCurrency
     */
    public MockInMemoryLedger(InterledgerAddress ledgerPrefix, CurrencyUnit ledgerCurrency) {
        if (! ledgerPrefix.getValue().endsWith(".")) {
            throw new RuntimeException("ledgerPrefix must end with a dot '.' ");
        }
        internalLedger = new InternalLedger(ledgerCurrency);

        this.ILP_HOLD_ACCOUNT = new LocalAccount("@ILP_HOLD_ACCOUNT@", "password", 0);
        internalLedger.addAccount(this.ILP_HOLD_ACCOUNT);

        this.pluginsConnected = new HashMap<InterledgerAddress, ConnectedPlugin>();
        this.ilpPendingTransfers = new HashMap<String, ILPTransfer>();
        this.ledgerPrefix = ledgerPrefix;
        //create a hold account specific for this ledger
    }



    /**
     * connect request from external plugin
     * @param connectorAddress  ILP ConnectorAddress
     * @param accountId         Connector account in ledger
     * @param password          Credentials used for "AAA"
     */
    public void onILPConnectRequest(InterledgerAddress connectorAddress, String accountId, String password) {
        if(this.pluginsConnected.containsKey(connectorAddress)) {
            throw new RuntimeException("plugin already connected");
        }
        LocalAccount connectorLocalAccount = this.internalLedger.getLocalAccount(accountId);
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
     * get an account from its ilp address
     * @param accountAddress
     * @return Account
     */
    public LocalAccount getAccountByILPAddress(InterledgerAddress accountAddress) {
        // The local account id is just the ilp ledger account  without the ledger prefix
        String local_account_id = accountAddress.getValue().replace(this.ledgerPrefix.getValue(),"");
        return this.internalLedger.getLocalAccount(local_account_id);
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
        LocalAccount sourceAccount = getAccountByILPAddress(newTransfer.getSourceAccount());

        // Atomic {
        this.ilpPendingTransfers.put(newTransfer.getUUID(), newTransfer);
        sourceAccount.debitAccount(newTransfer.getAmount());
        ILP_HOLD_ACCOUNT.creditAccount(newTransfer.getAmount());
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
        LocalAccount destinationAccount = this.getAccountByILPAddress(transfer.getDestinationAccount());
        // NOTE: Next operations must be atomic or rollback
        {
            ILP_HOLD_ACCOUNT.debitAccount(transfer.getAmount());
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
            LocalAccount sourceAccount = this.getAccountByILPAddress(rejectedTransfer.getSourceAccount());

            rejectedTransfer.setRejectedStatus();
            ILP_HOLD_ACCOUNT.debitAccount(rejectedTransfer.getAmount());
            sourceAccount.creditAccount(rejectedTransfer.getAmount());
        } else {
            throw new RuntimeException("transfer not exist");
        }
    }

    public LocalAccount getHoldAccount() {
        return ILP_HOLD_ACCOUNT;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(new LedgerInfo(this.ledgerPrefix, this.internalLedger.ledgerCurrency));
        str.append("\n" + this.printAccounts());
        str.append("\n" + this.printPluginConnections());
        str.append("\n" + this.printTransactions() + "\n");
        return str.toString();
    }
    
    public String printAccounts() {
        StringBuilder str = new StringBuilder();
        str.append("-LEDGER-ACCOUNTS---------");
        str.append("\n" + ILP_HOLD_ACCOUNT);
        str.append("\n-------------------------");
        if (this.debugTotalAccounts() != 0) {
            for (String addressAccount : this.internalLedger.ledgerAccounts.keySet()) {
                str.append("\n" + this.internalLedger.ledgerAccounts.get(addressAccount));
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