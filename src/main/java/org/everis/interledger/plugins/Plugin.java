package org.everis.interledger.plugins;

import org.interledger.InterledgerAddress;
import org.interledger.cryptoconditions.Fulfillment;

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
            System.out.println("Can reach the connection");
        }
    }

//    void sendMessage(Message message);

    public void fulfillCondition(Transfer transfer, Fulfillment fulfillment) {
        if (this.isConnected()) {
            this.ledger.fulfillCondition(transfer, fulfillment);
        } else {
            System.out.println("Can reach the connection");
        }
    }

//    void rejectIncomingTransfer(TransferId transferId, InterledgerProtocolError rejectionReason);
//    UUID addLedgerPluginEventHandler(LedgerPluginEventHandler eventHandler);
//    void removeLedgerPluginEventHandler(UUID eventHandlerId);
//    LedgerPluginEventEmitter getLedgerPluginEventEmitter();

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("-PLUGIN-ACCOUNTS---------");
        str.append("\n-------------------------");
        if (this.ledger != null) {
            str.append("\n" + this.getLedgerInfo());
        } else {
            str.append("\n-NO-LEDGER------------");
        }
        str.append("\nLinked Account " + this.linkedAccount.getAccount());
        str.append("\n-------------------------");
        return str.toString();
    }
}
