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
        // TXLog used to store initial and final balances associated to a TX
        private class TXLog {
            final public int initialSrcAmount;
            final public int initialDstAmount;
            final public LocalTransfer tx;
            final public int finalSrcAmount;
            final public int finalDstAmount;

            TXLog(int initialSrcAmount, int initialDstAmount, LocalTransfer tx, int finalSrcAmount, int finalDstAmount) {
                if (initialSrcAmount + initialDstAmount != finalSrcAmount + finalDstAmount) {
                    throw new RuntimeException("Initial and final balances do NOT match");
                }
                this.initialSrcAmount = initialSrcAmount;
                this.initialDstAmount = initialDstAmount;
                this.tx               = tx;
                this.finalSrcAmount   = finalSrcAmount;
                this.finalDstAmount   = finalDstAmount;
            }
        }
        private final List<TXLog> logOfLocalTransfers = new ArrayList<TXLog>();

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

        public void executeTransfer(LocalTransfer internalTransfer) {
            String keyFrom = internalTransfer.from.id;
            String keyTo   = internalTransfer.to  .id;
            if(! this.ledgerAccounts.containsKey(keyFrom)) {
                throw new RuntimeException("internal transfer creditor (from) '"+internalTransfer.from.id+"' not found" );
            }
            if(!this.ledgerAccounts.containsKey(keyTo)) {
                throw new RuntimeException("internal transfer debitor (to) '"+internalTransfer.from.id+"' not found" );
            }
            LocalAccount from =  this.ledgerAccounts.get(keyFrom);
            LocalAccount to   =  this.ledgerAccounts.get(keyTo  );
            if (from.getBalance() < internalTransfer.ammount ) {
                throw new RuntimeException("not enough funds");
            }
            // "Move money from account to account". Overwrite OLD registries
            LocalAccount newFrom = new LocalAccount(from, from.getBalance() - internalTransfer.ammount);
            LocalAccount newTo   = new LocalAccount(to  , to  .getBalance() + internalTransfer.ammount);

            TXLog txLog = new TXLog(from.getBalance(), to.getBalance(), internalTransfer, newFrom.getBalance(), newTo.getBalance() );
            this.ledgerAccounts.put(keyFrom, newFrom);
            this.ledgerAccounts.put(keyTo  , newTo  );
            logOfLocalTransfers.add(txLog);
        }

    }

    private StringBuffer TXLogToStringBuffer(InternalLedger.TXLog log) {
        StringBuffer result = new StringBuffer();
        result.append("{\n")
              .append("Initial src/dst balance: ")
              .append(log.initialSrcAmount).append("/").append(log.initialDstAmount).append("\n")
              .append(log.tx.from).append(" -> ").append(log.tx.ammount).append(" -> ").append(log.tx.to).append("\n")
              .append("Final   src/dst balance: ")
              .append(log.finalSrcAmount)  .append("/").append(log.finalDstAmount).append("\n")
              .append("}") ;
        return result;
    }

    public StringBuffer debugDumpOrderedListOfLocalTransfers() {
        StringBuffer result = new StringBuffer();
        for (InternalLedger.TXLog log : internalLedger.logOfLocalTransfers) {
            result.append(TXLogToStringBuffer(log)) ;
        }
        return result;
    }
    public StringBuffer debugDumpLastLocalTransfers() {
        int last_idx = internalLedger.logOfLocalTransfers.size()-1;
        InternalLedger.TXLog log = internalLedger.logOfLocalTransfers.get(last_idx);
        return TXLogToStringBuffer(log) ;
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
    private final String ID_HOLD_ACCOUNT =  "@ILP_HOLD_ACCOUNT@";

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

        LocalAccount ILP_HOLD_ACCOUNT = new LocalAccount(ID_HOLD_ACCOUNT, "password", 0);
        internalLedger.addAccount(ILP_HOLD_ACCOUNT);

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
        if (! accountAddress.getValue().startsWith(this.ledgerPrefix.getValue())) {
            throw new RuntimeException(
                "Interledger Address does not correspond to a local ledger address. '"
              + accountAddress.getValue()
              + "' does not match ledger prefix "+this.ledgerPrefix.getValue()

              );
        }
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

        boolean ilpSourceIsLocal = (newTransfer.getSourceAccount().
                getValue().startsWith(this.ledgerPrefix.getValue()));

        if ( !ilpSourceIsLocal) {
            // This could be caused by a wrong routing, a programming error or some cracker
            throw new RuntimeException("The ILP source account doesn't match a local account in this ledger");
        }

        boolean ilpDestinationIsLocal = (newTransfer.getDestinationAccount().
                getValue().startsWith(this.ledgerPrefix.getValue()));
        if (!ilpDestinationIsLocal) {
            /* TODO:(0) Implement forwarding. That means:
             *  - Searching a connected plugin that can forward the payment.
             *  - Notifying the plugin to "forward the payment" to next ledger / trust-line
             */
            throw new RuntimeException("ILP Destination is NOT local but forwarding NOT yet implemented");
        }

        // Atomic {
        newTransfer.setPreparedStatus();
        this.ilpPendingTransfers.put(newTransfer.getUUID(), newTransfer);
        LocalTransfer tx = new LocalTransfer(sourceAccount, this.getHoldAccount(), newTransfer.getAmount());
        this.internalLedger.executeTransfer(tx);
        // }
    }

    /**
     * Fulfill the condition of the transfer by receiver when he agree with the conditions of the transfer.
     * @param transferId
     * @param fulfillment
     */
    public void fulfillCondition(String transferId, Fulfillment fulfillment) {
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
            LocalTransfer tx = new LocalTransfer(this.getHoldAccount(), destinationAccount, transfer.getAmount());
            this.internalLedger.executeTransfer(tx);
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
            LocalTransfer tx = new LocalTransfer(this.getHoldAccount(), sourceAccount, rejectedTransfer.getAmount());
            this.internalLedger.executeTransfer(tx);
        } else {
            throw new RuntimeException("transfer not exist");
        }
    }

    public LocalAccount getHoldAccount() {
        return this.internalLedger.getLocalAccount(ID_HOLD_ACCOUNT);
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
        str.append("\n" + this.getHoldAccount());
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