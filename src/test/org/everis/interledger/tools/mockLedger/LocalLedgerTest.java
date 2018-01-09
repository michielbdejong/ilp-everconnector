package org.everis.interledger.tools.mockLedger;

import org.interledger.InterledgerAddress;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.money.Monetary;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class LocalLedgerTest {

    final String LEDGER_PREFIX = "test1.pound.";
    LocalLedgerILPAdaptor ledger;
    final LocalAccount connectorAct1 = new LocalAccount("connector1", "connector1", 1000);
    final LocalAccount creditor1 = new LocalAccount("creditor1", "creditor1", 1000);
    InterledgerAddress creditor1_ilp_address =  InterledgerAddress.of(LEDGER_PREFIX + creditor1.id);
    final LocalAccount debitor1  = new LocalAccount("debitor1" , "debitor1" , 1000);
    InterledgerAddress debitor1_ilp_address =  InterledgerAddress.of(LEDGER_PREFIX + debitor1.id);

/// @BeforeEach
/// void setUp() {
///     ledger = new LocalLedgerILPAdaptor(InterledgerAddress.of(LEDGER_PREFIX), Monetary.getCurrency(Locale.UK));
///     // By Default the HOLD_ACCOUNT must be present
///     assertEquals(ledger.debugTotalAccounts(), 1);
///     ledger.internalLedger.addAccount(connectorAct1);
///     ledger.internalLedger.addAccount(creditor1);
///     ledger.internalLedger.addAccount(debitor1);
///     assertEquals(ledger.debugTotalAccounts(), 4);
/// }

/// @AfterEach
/// void tearDown() {
/// }

/// @Test
/// void executeLocalTransfer() {
///     int creditor1_initialBalance = ledger.getAccountByILPAddress(creditor1_ilp_address).getBalance();
///     int tx_amount = 100;
///     LocalTransfer tx = new LocalTransfer( creditor1, debitor1, tx_amount );
///     ledger.internalLedger.executeTransfer(tx);
///     int creditor1_finalBalance = ledger.getAccountByILPAddress(creditor1_ilp_address).getBalance();
///     assertEquals( creditor1_finalBalance, creditor1_initialBalance - tx_amount);
/// }

}