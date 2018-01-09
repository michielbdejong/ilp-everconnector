package org.everis.interledger.tools.mockLedger;

import org.interledger.InterledgerAddress;

import javax.money.CurrencyUnit;

// TODO:(?) Use configurable sleep... to simulate long TX times (Bitcoin, ...a

/**
 * This adaptor maps ILP request to internal ledger transactions.
 */
public class LocalLedgerILPAdaptor {
    // TODO:(0) Use real Interledger Errors instead of RuntimeException<.

    final int SECS_SIMULATED_LEDGER_LOAD_DELAY = 0 /*secs*/;
    public final MockSettlementLedger internalLedger;

    /**
     * Constructor's ledger with a prefix and a currency unit.
     *
     * @param ledgerCurrency
     */
    public LocalLedgerILPAdaptor(CurrencyUnit ledgerCurrency) {
        internalLedger = new MockSettlementLedger(ledgerCurrency, SECS_SIMULATED_LEDGER_LOAD_DELAY);
    }

    /**
     * connect request from external plugin
     *
     * @param connectorAddress ILP ConnectorAddress
     * @param accountId        Connector account in ledger
     * @param password         Credentials used for "AAA"
     */
    public void onILPConnectRequest(
            InterledgerAddress connectorAddress, String accountId, String password) {
        LocalAccount connectorLocalAccount = this.internalLedger.getLocalAccount(accountId);
        String accountPassword = connectorLocalAccount.getPassword();
        if (!password.equals(accountPassword)) {
            throw new RuntimeException("wrong password");
        }
    }

    /**
     * disconnect a plugin from the ledger.
     */
    public void disconnect() {
    }

    public void addAccount(LocalAccount account) {
        this.internalLedger.addAccount(account);
    }

   // Debug
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("\n" + this.printAccounts());
        return str.toString();
    }

    private StringBuffer TXLogToStringBuffer(MockSettlementLedger.TXLog log) {
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
        for (MockSettlementLedger.TXLog log : internalLedger.logOfLocalTransfers) {
            result.append(TXLogToStringBuffer(log));
        }
        return result;
    }

    public String printAccounts() {
        StringBuilder str = new StringBuilder();
        str.append("-LEDGER-ACCOUNTS---------");
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

    public StringBuffer debugDumpLastLocalTransfers() {
        int last_idx = internalLedger.logOfLocalTransfers.size() - 1;
        MockSettlementLedger.TXLog log = internalLedger.logOfLocalTransfers.get(last_idx);
        return TXLogToStringBuffer(log);
    }

    public int debugTotalAccounts() {
        return this.internalLedger.ledgerAccounts.keySet().size();
    }
}