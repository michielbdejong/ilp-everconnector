package org.everis.interledger.tools.mockLedger;

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

class MockInMemoryLedgerTest {

    final String LEDGER_PREFIX = "test1.pound.";
    MockInMemoryLedger ledger;
    final LocalAccount connectorAct1 = new LocalAccount("connector1", "connector1", 1000);
    final LocalAccount creditor1 = new LocalAccount("creditor1", "creditor1", 1000);
    InterledgerAddress creditor1_ilp_address =  InterledgerAddress.of(LEDGER_PREFIX + creditor1.id);
    final LocalAccount debitor1  = new LocalAccount("debitor1" , "debitor1" , 1000);
    InterledgerAddress debitor1_ilp_address =  InterledgerAddress.of(LEDGER_PREFIX + debitor1.id);

    @BeforeEach
    void setUp() {
        ledger = new MockInMemoryLedger(InterledgerAddress.of(LEDGER_PREFIX), Monetary.getCurrency(Locale.UK));
        // By Default the HOLD_ACCOUNT must be present
        assertEquals(ledger.debugTotalAccounts(), 1);
        ledger.internalLedger.addAccount(connectorAct1);
        ledger.internalLedger.addAccount(creditor1);
        ledger.internalLedger.addAccount(debitor1);
        assertEquals(ledger.debugTotalAccounts(), 4);
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
    void executeLocalTransfer() {
        int creditor1_initialBalance = ledger.getAccountByILPAddress(creditor1_ilp_address).getBalance();
        int tx_amount = 100;
        LocalTransfer tx = new LocalTransfer( creditor1, debitor1, tx_amount );
        ledger.internalLedger.executeTransfer(tx);
        int creditor1_finalBalance = ledger.getAccountByILPAddress(creditor1_ilp_address).getBalance();
        assertEquals( creditor1_finalBalance, creditor1_initialBalance - tx_amount);
    }


        /**
     * Test of a simple transaction. We only handle the simple way, without taking care about
     * conditions or fulfillments.
     */
///////    private static void testSimpleTransaction() {

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
///////         sourcePlugin.prepareTransfer(testOne);

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