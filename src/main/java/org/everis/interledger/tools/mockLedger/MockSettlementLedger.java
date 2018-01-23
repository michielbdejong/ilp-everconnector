package org.everis.interledger.tools.mockLedger;

import javax.money.CurrencyUnit;
import java.math.BigInteger;
import java.util.*;

/**
 *
 *  In memory ledger with simple handling of accounts and balances.
 *  It must NOT contain any reference to ILP structures. In practice this
 *  class simulates a main-frame, a blockchain, ...
 */
class MockSettlementLedger {
    public final Map<String /* id */, LocalAccount> ledgerAccounts;
    public final CurrencyUnit ledgerCurrency;

    Random rnd = new Random();
    public final int          simulatedLoadDelay;

    /**
     *  TXLog : utility class used to store initial and final balances associated to a TX
     *         for debugging / tracing purposes.
     */
    static public class TXLog {
        final public BigInteger initialSrcAmount;
        final public BigInteger initialDstAmount;
        final public LocalTransfer tx;
        final public BigInteger finalSrcAmount;
        final public BigInteger finalDstAmount;

        TXLog(BigInteger initialSrcAmount, BigInteger initialDstAmount, LocalTransfer tx, BigInteger finalSrcAmount, BigInteger finalDstAmount) {
            if (initialSrcAmount.add(initialDstAmount).compareTo(finalSrcAmount.add(finalDstAmount)) != 0) {
                throw new RuntimeException("Initial and final balances do NOT match");
            }
            this.initialSrcAmount = initialSrcAmount;
            this.initialDstAmount = initialDstAmount;
            this.tx               = tx;
            this.finalSrcAmount   = finalSrcAmount;
            this.finalDstAmount   = finalDstAmount;
        }
    }

    public final List<TXLog> logOfLocalTransfers = new ArrayList<TXLog>();

    MockSettlementLedger(CurrencyUnit ledgerCurrency, int simulatedLoadDelay) {
        this.ledgerAccounts = new HashMap<String /*id */, LocalAccount>();
        this.ledgerCurrency = ledgerCurrency;
        this.simulatedLoadDelay = simulatedLoadDelay;
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
        if (this.simulatedLoadDelay>0) {
            try {
                Thread.sleep(rnd.nextInt(this.simulatedLoadDelay * 1000));
            } catch (InterruptedException e) {
                // Nothing intelligent ot fix the exception
            }
        }
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
        if (from.getBalance().compareTo(internalTransfer.ammount) < 0  ) {
            throw new RuntimeException("not enough funds");
        }
        // "Move money from account to account". Overwrite OLD registries
        LocalAccount newFrom = new LocalAccount(from, from.getBalance().subtract(internalTransfer.ammount));
        LocalAccount newTo   = new LocalAccount(to  , to  .getBalance().add     (internalTransfer.ammount));

        TXLog txLog = new TXLog(from.getBalance(), to.getBalance(), internalTransfer, newFrom.getBalance(), newTo.getBalance() );

        // Next code must be executed atomically in the DDBB
        // (all updates success or the DDBB status is rollback to initial state)
        {
            this.ledgerAccounts.put(keyFrom, newFrom);
            this.ledgerAccounts.put(keyTo, newTo);
        }
        logOfLocalTransfers.add(txLog);
    }

}
