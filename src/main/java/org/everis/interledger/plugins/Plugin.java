package org.everis.interledger.plugins;

import org.interledger.InterledgerAddress;
import org.interledger.cryptoconditions.Fulfillment;
import org.interledger.ilp.InterledgerProtocolError;

public class Plugin {
    private PluginConnection linkedAccount;
    private Ledger ledger;

    public Plugin(Account linkedAccount) {
        this.ledger = null;
        this.linkedAccount = new PluginConnection(linkedAccount);
    }

    public LedgerInfo getLedgerInfo() {
        return ledger.getInfo();
    }

    public InterledgerAddress getConnectorAccount() {
        throw new RuntimeException("Not Implemented");
    }

//    BigInteger getConnectorBalance();

    public void connect(Ledger aimedLedger) {
        this.ledger = aimedLedger;
        this.ledger.connect(this.linkedAccount);
    }

    public void disconnect() {
        this.ledger.disconnect(this.linkedAccount.getAccount());
        this.ledger = null;
    }

    public boolean isConnected() {
        return this.ledger != null && this.ledger.isPluginConnected(this.linkedAccount.getAccount());
    }

//    Optional<Fulfillment> getFulfillment(TransferId transferId);

    public void sendTransfer(Transfer transfer) {
        if (this.isConnected()) {
            this.ledger.prepareTransaction(transfer);
        } else {
            throw new RuntimeException("Can reach the connection");
        }
    }

//    void sendMessage(Message message);

    public void fulfillCondition(Transfer transfer, Fulfillment fulfillment) {
        if (this.isConnected()) {
            this.ledger.fulfillCondition(transfer, fulfillment);
        } else {
            throw new RuntimeException("Can reach the connection");
        }
    }

    public void rejectIncomingTransfer(Transfer transfer, InterledgerProtocolError rejectionReason) {

    }

//    UUID addLedgerPluginEventHandler(LedgerPluginEventHandler eventHandler);
//    void removeLedgerPluginEventHandler(UUID eventHandlerId);
//    LedgerPluginEventEmitter getLedgerPluginEventEmitter();

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("-PLUGIN-ACCOUNTS---------");
        str.append("\n-------------------------");
        str.append("\n" +  this.ledger == null ? "-NO-LEDGER------------" : this.getLedgerInfo());
        str.append("\nLinked Account " + this.linkedAccount.getAccount());
        str.append("\n-------------------------");
        return str.toString();
    }
}
