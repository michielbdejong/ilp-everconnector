package org.everis.interledger.plugins;

import org.interledger.InterledgerAddress;
import org.interledger.cryptoconditions.Condition;
import org.interledger.cryptoconditions.CryptoConditionType;
import org.interledger.cryptoconditions.Fulfillment;

public class Test {

    public static void main(String [] args) {
//        testCreateLedgerAccountConnections();
        testSimpleTransaction();
    }

    private static void testSimpleTransaction() {
        Ledger ledgerEveris = new Ledger();

        Account sourceAccount = new Account(InterledgerAddress.of("test1.everis.jhon"), "0000", 1000);
        Account destinationAccount = new Account(InterledgerAddress.of("test1.everis.jane"), "1111", 0);

        ledgerEveris.addAccount(sourceAccount);
        ledgerEveris.addAccount(destinationAccount);

        Plugin sourcePlugin = new Plugin(sourceAccount);
        Plugin destinationPlugin = new Plugin(destinationAccount);

        System.out.println(ledgerEveris);

        // Begin the transaction jhon to jane
        Transfer testOne = new Transfer(
            sourceAccount.getAccountAddress(),
            destinationAccount.getAccountAddress(),
            150);

        sourcePlugin.connect(ledgerEveris);
        sourcePlugin.sendTransfer(testOne);

        System.out.println(ledgerEveris);

        destinationPlugin.connect(ledgerEveris);
        destinationPlugin.fulfillCondition(testOne, null);

        sourcePlugin.disconnect();
        destinationPlugin.disconnect();

        System.out.println(ledgerEveris);
    }

    private static void testCreateLedgerAccountConnections() {
        Ledger ledgerEveris = new Ledger();
        System.out.println(ledgerEveris);

        Account jhonDoe = new Account(InterledgerAddress.of("test1.everis.jhon"),
                                    "0000", 1000);
        Account janeDoe = new Account(InterledgerAddress.of("test1.everis.jane"),
                                    "1111", 0);

        ledgerEveris.addAccount(janeDoe);
        ledgerEveris.addAccount(jhonDoe);
        System.out.println(ledgerEveris);

        Plugin jhonPlugin = new Plugin(jhonDoe);
        Plugin janePlugin = new Plugin(janeDoe);

        jhonPlugin.connect(ledgerEveris);
        janePlugin.connect(ledgerEveris);
        System.out.println(ledgerEveris);

        jhonPlugin.disconnect();
        janePlugin.disconnect();
        System.out.println(ledgerEveris);
    }
}
