package org.everis.interledger.tools.mockLedger;

import org.everis.interledger.connector.GenericConnector;
import org.everis.interledger.org.everis.interledger.common.LedgerInfo;
import org.everis.interledger.org.everis.interledger.common.ILPTransfer;
import org.interledger.InterledgerAddress;
import org.interledger.cryptoconditions.Fulfillment;

import javax.money.CurrencyUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic simulation entity of a ledger.
 */

// TODO:(?) Use configurable sleep... to simulate long TX times (Bitcoin, ...a

/**
 * This adaptor maps ILP request to internal ledger transactions.
 *
 */
public class LocalLedgerILPAdaptor {

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


    final int SECS_SIMULATED_LEDGER_LOAD_DELAY = 0 /*secs*/;
    public final LocalLedger internalLedger;
    // ILP related. This data is needed to support ILP.
    // Notice an ILPTransfer maps to different LocalTransfers
    private final InterledgerAddress ledgerPrefix;
    private final Map<InterledgerAddress, ConnectedPlugin> pluginsConnected;
    // Keeps track of out-standing ILP Transfers (prepared but not yet executed or rejected)
    private final Map<String, ILPTransfer> ilpPendingTransfers;
    private final String ID_HOLD_ACCOUNT = "@ILP_HOLD_ACCOUNT@";
    //final SimpleConnector connectorLinked;
    private GenericConnector connectorLinked;



    /**
     * Constructor's ledger with a prefix, a currency unit and a null connector. use for testing local transfers.
     *
     * @param ledgerPrefix
     * @param ledgerCurrency
     */
    public LocalLedgerILPAdaptor(InterledgerAddress ledgerPrefix, CurrencyUnit ledgerCurrency) {
        this.connectorLinked = null;
        if (!ledgerPrefix.getValue().endsWith(".")) {
            throw new RuntimeException("ledgerPrefix must end with a dot '.' ");
        }
        internalLedger = new LocalLedger(ledgerCurrency, SECS_SIMULATED_LEDGER_LOAD_DELAY);

        LocalAccount ILP_HOLD_ACCOUNT = new LocalAccount(ID_HOLD_ACCOUNT, "password", 0);
        internalLedger.addAccount(ILP_HOLD_ACCOUNT);

        this.pluginsConnected = new HashMap<InterledgerAddress, ConnectedPlugin>();
        this.ilpPendingTransfers = new HashMap<String, ILPTransfer>();
        this.ledgerPrefix = ledgerPrefix;
        //create a hold account specific for this ledger
    }

    /**
     * Constructor's ledger with a prefix and a currency unit.
     *
     * @param ledgerPrefix
     * @param ledgerCurrency
     * @param connectorLinked
     */
    public LocalLedgerILPAdaptor(InterledgerAddress ledgerPrefix, CurrencyUnit ledgerCurrency, GenericConnector connectorLinked) {
        this.connectorLinked = connectorLinked;
        if (!ledgerPrefix.getValue().endsWith(".")) {
            throw new RuntimeException("ledgerPrefix must end with a dot '.' ");
        }
        internalLedger = new LocalLedger(ledgerCurrency, SECS_SIMULATED_LEDGER_LOAD_DELAY);

        LocalAccount ILP_HOLD_ACCOUNT = new LocalAccount(ID_HOLD_ACCOUNT, "password", 0);
        internalLedger.addAccount(ILP_HOLD_ACCOUNT);

        this.pluginsConnected = new HashMap<InterledgerAddress, ConnectedPlugin>();
        this.ilpPendingTransfers = new HashMap<String, ILPTransfer>();
        this.ledgerPrefix = ledgerPrefix;
        //create a hold account specific for this ledger
    }


    /**
     * connect request from external plugin
     *
     * @param connectorAddress ILP ConnectorAddress
     * @param accountId        Connector account in ledger
     * @param password         Credentials used for "AAA"
     */
    public void onILPConnectRequest(InterledgerAddress connectorAddress, String accountId, String password) {
        if (this.pluginsConnected.containsKey(connectorAddress)) {
            throw new RuntimeException("plugin already connected");
        }
        LocalAccount connectorLocalAccount = this.internalLedger.getLocalAccount(accountId);
        String accountPassword = connectorLocalAccount.getPassword();
        if (!password.equals(accountPassword)) {
            throw new RuntimeException("wrong password");
        }
        this.pluginsConnected.put(connectorAddress, new ConnectedPlugin(connectorAddress, connectorLocalAccount));
    }

    /**
     * disconnect a plugin from the ledger.
     *
     * @param pluginAccountAddress
     */
    public void disconnect(InterledgerAddress pluginAccountAddress) {
        if (!this.pluginsConnected.containsKey(pluginAccountAddress)) {
            throw new RuntimeException("plugin not connected");
        }
        this.pluginsConnected.remove(pluginAccountAddress);
    }

    /**
     * get an account from its ilp address
     *
     * @param accountAddress
     * @return Account
     */
    public LocalAccount getAccountByILPAddress(InterledgerAddress accountAddress) {
        if (!accountAddress.getValue().startsWith(this.ledgerPrefix.getValue())) {
            throw new RuntimeException(
                    "Interledger Address does not correspond to a local ledger address. '"
                            + accountAddress.getValue()
                            + "' does not match ledger prefix " + this.ledgerPrefix.getValue()

            );
        }
        // The local account id is just the ilp ledger account  without the ledger prefix
        String local_account_id = accountAddress.getValue().replace(this.ledgerPrefix.getValue(), "");
        return this.internalLedger.getLocalAccount(local_account_id);
    }

    /**
     * first step of the ILP flow to prepare a transfer by transferring the funds on the hold account and put
     * the transfer status as "PREPARED".
     *
     * @param newTransfer
     */
    public void prepareTransaction(ILPTransfer newTransfer) {
        if (this.ilpPendingTransfers.containsKey(newTransfer.getUUID())) {
            //TODO: throw exception transfer already exist
        }

        InterledgerAddress sourceAddress = newTransfer.getSourceAccount();
        LocalAccount sourceAccount;
        //check if the source ilp address is local if not use local connector account
        if(sourceAddress.getPrefix().getValue().equals(this.ledgerPrefix.getValue())) {
            sourceAccount = getAccountByILPAddress(sourceAddress);
        } else {
            sourceAccount = getAccountByILPAddress(InterledgerAddress.of(this.ledgerPrefix.getValue() +
                    this.connectorLinked.getIdConnector()));
        }

        //set up local transfer, change status ilp transfer and save the ilp transfer
        LocalTransfer localTransfer = new LocalTransfer(sourceAccount, this.getHoldAccount(), newTransfer.getAmount());
        //TODO: Next operations must be atomic or rollback
        this.internalLedger.executeTransfer(localTransfer);
        newTransfer.setPreparedStatus();
        this.ilpPendingTransfers.put(newTransfer.getUUID(), newTransfer);

        //if the destination is not local then notify the connector with the ilp transfer
        InterledgerAddress destinationAddress = newTransfer.getDestinationAccount();
        if(!destinationAddress.getPrefix().getValue().equals(this.ledgerPrefix.getValue())) {
            this.connectorLinked.notifyPreparePayment(newTransfer);
        }
    }

    /**
     * Fulfill the condition of the transfer by receiver when he agree with the conditions of the transfer.
     *
     * @param transferId
     * @param fulfillment
     */
    public void fulfillCondition(String transferId, Fulfillment fulfillment) {
        if (!this.ilpPendingTransfers.containsKey(transferId)) {
            //TODO: throw exception transfer not exist
        }
        ILPTransfer transfer = this.ilpPendingTransfers.get(transferId);

        if (transfer.getStatus() != ILPTransfer.TransferStatus.PREPARED) {
            //TODO: throw exception transfer status not prepared
        }

        //check if the destination ilp address is local if not use local connector account
        InterledgerAddress destinationAddress = transfer.getDestinationAccount();
        LocalAccount destinationAccount;
        if(destinationAddress.getPrefix().getValue().equals(this.ledgerPrefix.getValue())) {
            destinationAccount = this.getAccountByILPAddress(destinationAddress);
        } else {
            destinationAccount = getAccountByILPAddress(InterledgerAddress.of(this.ledgerPrefix.getValue() +
                    this.connectorLinked.getIdConnector()));
        }

        //TODO: Next operations must be atomic or rollback
        LocalTransfer tx = new LocalTransfer(this.getHoldAccount(), destinationAccount, transfer.getAmount());
        this.internalLedger.executeTransfer(tx);
        ILPTransfer transferFulfilled = new ILPTransfer(transfer, fulfillment);
        transfer.setExecutedStatus();
        this.ilpPendingTransfers.replace(transferId, transferFulfilled);

        //if the source's transfer is not local then notify the connector with the ilp transfer
        InterledgerAddress sourceAddress = transfer.getSourceAccount();
        if(!sourceAddress.getPrefix().getValue().equals(this.ledgerPrefix.getValue())) {
            this.connectorLinked.notifyFulfillment(transfer, fulfillment);
        }
    }

    /**
     * reject the transfer by the receiver when the transfer'd conditions are not respected or the timeout is passed.
     *
     * @param //transferId
     */
    //TODO: need to be implemented
    public void rejectTransfer(String transferId) {
        if (!this.ilpPendingTransfers.containsKey(transferId)) {
            //TODO: throw exception transfer not exist
        }
        ILPTransfer transfer = this.ilpPendingTransfers.get(transferId);

        if (transfer.getStatus() != ILPTransfer.TransferStatus.PREPARED) {
            //TODO: throw exception transfer status not prepared
        }

        //check if the source ilp address is local if not use local connector account
        InterledgerAddress sourceAddress = transfer.getSourceAccount();
        LocalAccount sourceAccount;
        if(sourceAddress.getPrefix().getValue().equals(this.ledgerPrefix.getValue())) {
            sourceAccount = this.getAccountByILPAddress(sourceAddress);
        } else {
            sourceAccount = getAccountByILPAddress(InterledgerAddress.of(this.ledgerPrefix.getValue() +
                    this.connectorLinked.getIdConnector()));
        }

        //TODO: Next operations must be atomic or rollback
        LocalTransfer localTransfer = new LocalTransfer(this.getHoldAccount(), sourceAccount, transfer.getAmount());
        this.internalLedger.executeTransfer(localTransfer);
        transfer.setRejectedStatus();
        this.ilpPendingTransfers.replace(transferId, transfer);

        //if the source's transfer is not local then notify the connector with the ilp transfer
        if(!sourceAddress.getPrefix().getValue().equals(this.ledgerPrefix.getValue())) {
            this.connectorLinked.notifyReject(transfer);
        }
    }

    public void addAccount(LocalAccount account) {
        this.internalLedger.addAccount(account);
    }

    public LedgerInfo getInfo() {
        return new LedgerInfo(this.ledgerPrefix, this.internalLedger.ledgerCurrency);
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

    private StringBuffer TXLogToStringBuffer(LocalLedger.TXLog log) {
        StringBuffer result = new StringBuffer();
        result.append("{\n")
                .append("Initial src/dst balance: ")
                .append(log.initialSrcAmount).append("/").append(log.initialDstAmount).append("\n")
                .append(log.tx.from).append(" -> ").append(log.tx.ammount).append(" -> ").append(log.tx.to).append("\n")
                .append("Final   src/dst balance: ")
                .append(log.finalSrcAmount).append("/").append(log.finalDstAmount).append("\n")
                .append("}");
        return result;
    }

    public StringBuffer debugDumpOrderedListOfLocalTransfers() {
        StringBuffer result = new StringBuffer();
        for (LocalLedger.TXLog log : internalLedger.logOfLocalTransfers) {
            result.append(TXLogToStringBuffer(log));
        }
        return result;
    }

    public StringBuffer debugDumpLastLocalTransfers() {
        int last_idx = internalLedger.logOfLocalTransfers.size() - 1;
        LocalLedger.TXLog log = internalLedger.logOfLocalTransfers.get(last_idx);
        return TXLogToStringBuffer(log);
    }

    public int debugTotalAccounts() {
        return this.internalLedger.ledgerAccounts.keySet().size();
    }

}