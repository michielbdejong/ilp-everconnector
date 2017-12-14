package org.everis.interledger.tools.mockLedger;

import static org.testng.Assert.assertEquals;

import org.interledger.InterledgerAddress;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.money.Monetary;
import java.util.Locale;

public class MockInMemoryLedgerTest {
    MockInMemoryLedger ledger;
    final LocalAccount connectorAct1 = new LocalAccount("connector1", "0000", 1000);


    @BeforeMethod
    public void setUp() throws Exception {
        ledger = new MockInMemoryLedger(InterledgerAddress.of("test1.pound"), Monetary.getCurrency(Locale.UK));
        // By Default the HOLD_ACCOUNT must be present
        assertEquals(ledger.getLedgerAccounts().keySet().size(), 1);
        ledger.addAccount(connectorAct1);
        assertEquals(ledger.getLedgerAccounts().keySet().size(), 2);
    }

    @AfterMethod
    public void tearDown() throws Exception {
    }

}