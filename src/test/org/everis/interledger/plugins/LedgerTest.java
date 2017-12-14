package org.everis.interledger.plugins;

import org.everis.interledger.tools.mockLedger.LocalAccount;
import org.everis.interledger.tools.mockLedger.MockInMemoryLedger;
import org.interledger.InterledgerAddress;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.money.Monetary;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;


// import java.util.Locale;
// import javax.money.Monetary;
// import org.interledger.InterledgerAddress;
// import org.interledger.cryptoconditions.Condition;
// import org.interledger.cryptoconditions.PreimageSha256Condition;
// import org.interledger.ilp.InterledgerPayment;

class LedgerTest {

    MockInMemoryLedger ledger;
    final LocalAccount connectorAct1 = new LocalAccount("connector1", "0000", 1000);

    @BeforeEach
    void setUp() {
        ledger = new MockInMemoryLedger(InterledgerAddress.of("test1.pound"), Monetary.getCurrency(Locale.UK));
        // By Default the HOLD_ACCOUNT must be present
        assertEquals(ledger.getLedgerAccounts().keySet().size(), 1);
        ledger.addAccount(connectorAct1);
        assertEquals(ledger.getLedgerAccounts().keySet().size(), 2);
    }

    @AfterEach
    void tearDown() {
    }

//        PluginConnection jhonPluginConnection =
//            new PluginConnection(jhonDoe.getAccountAddress(), jhonDoe.getPassword());
//
//        PluginConnection destinationPluginConnection =
//            new PluginConnection(janeDoe.getAccountAddress(), janeDoe.getPassword());
//
//        Plugin jhonDoePlugin = new Plugin(jhonPluginConnection);
//        Plugin janeDoePlugin = new Plugin(destinationPluginConnection);
//
//        jhonDoePlugin.connect(ledgerEveris);
//        janeDoePlugin.connect(ledgerEveris);
//
//        System.out.println(ledgerEveris);
//
//        jhonDoePlugin.disconnect();
//        janeDoePlugin.disconnect();
//
//        System.out.println(ledgerEveris);

    @Test
    void connect() {
    }

    @Test
    void disconnect() {
    }

    @Test
    void isPluginConnected() {
    }

    @Test
    void getAccountByAddress() {
    }

    @Test
    void prepareTransaction() {
    }

    @Test
    void fulfillCondition() {
    }

    @Test
    void rejectTransfer() {
    }

    @Test
    void getHoldAccount() {
    }

        /**
     * Test of a simple transaction. We only handle the simple way, without taking care about
     * conditions or fulfillments.
     */
///////    private static void testSimpleTransaction() {
///////         System.out.println("Simple Transaction Test ---------------------------------------------");
///////         Ledger ledgerEveris = new Ledger("test1.pound", Monetary.getCurrency(Locale.UK));
///////
///////         Account sourceAccount = new Account(InterledgerAddress.of("test1.everis.jhon"),
///////             "0000", 1000);
///////         Account destinationAccount = new Account(InterledgerAddress.of("test1.everis.jane"),
///////             "1111", 0);
///////
///////         ledgerEveris.addAccount(sourceAccount);
///////         ledgerEveris.addAccount(destinationAccount);
///////
///////         PluginConnection sourcePluginConnection =
///////             new PluginConnection(sourceAccount.getAccountAddress(), sourceAccount.getPassword());
///////
///////         PluginConnection destinationPluginConnection =
///////             new PluginConnection(destinationAccount.getAccountAddress(), destinationAccount.getPassword());
///////
///////         Plugin sourcePlugin = new Plugin(sourcePluginConnection);
///////         Plugin destinationPlugin = new Plugin(destinationPluginConnection);
///////
///////         System.out.println(ledgerEveris);
///////
///////         Condition mockCondition = new PreimageSha256Condition(0, new byte[]{});
///////         // Begin the transaction jhon to jane
///////
///////         InterledgerPayment mockPayment = InterledgerPayment.builder().
///////
///////
///////         // static Builder builder(final InterledgerAddress destinationAccount, final BigInteger destinationAmount, final byte[] data) {
///////         //     return new Builder(destinationAccount, destinationAmount, data);
///////         // }
///////
///////         Transfer testOne = new Transfer(
///////             sourceAccount.getAccountAddress(),
///////             destinationAccount.getAccountAddress(),
///////             150, mockCondition, null);
///////
///////         sourcePlugin.connect(ledgerEveris);
///////         sourcePlugin.sendTransfer(testOne);

/////////        System.out.println("null here" + "\n" + ledgerEveris);

////////        destinationPlugin.connect(ledgerEveris);
////////        System.out.println("null here" + "\n" + ledgerEveris);
////////        destinationPlugin.fulfillCondition(testOne.getId(), null);
////////
////////        sourcePlugin.disconnect();
////////        destinationPlugin.disconnect();

////////        System.out.println(ledgerEveris);
////////    }

}