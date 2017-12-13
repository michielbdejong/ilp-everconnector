package org.everis.interledger.plugins;

import java.util.Locale;
import javax.money.Monetary;
import org.interledger.InterledgerAddress;

public class Test {

    public static void main(String [] args) {
        testCreateLedgerAccountConnections();
        testSimpleTransaction();
    }

    /**
     * Test of a simple transaction. We only handle the simple way, without taking care about
     * conditions or fulfillments.
     */
    private static void testSimpleTransaction() {
        System.out.println("Simple Transaction Test ---------------------------------------------");
        Ledger ledgerEveris = new Ledger("test1.pound", Monetary.getCurrency(Locale.UK));

        Account sourceAccount = new Account(InterledgerAddress.of("test1.everis.jhon"),
            "0000", 1000);
        Account destinationAccount = new Account(InterledgerAddress.of("test1.everis.jane"),
            "1111", 0);

        ledgerEveris.addAccount(sourceAccount);
        ledgerEveris.addAccount(destinationAccount);

        PluginConnection sourcePluginConnection =
            new PluginConnection(sourceAccount.getAccountAddress(), sourceAccount.getPassword());

        PluginConnection destinationPluginConnection =
            new PluginConnection(destinationAccount.getAccountAddress(), destinationAccount.getPassword());

        Plugin sourcePlugin = new Plugin(sourcePluginConnection);
        Plugin destinationPlugin = new Plugin(destinationPluginConnection);

        System.out.println(ledgerEveris);

        // Begin the transaction jhon to jane
        Transfer testOne = new Transfer(
            sourceAccount.getAccountAddress(),
            destinationAccount.getAccountAddress(),
            150);

        sourcePlugin.connect(ledgerEveris);
        sourcePlugin.sendTransfer(testOne);

//        System.out.println("null here" + "\n" + ledgerEveris);

        destinationPlugin.connect(ledgerEveris);
        System.out.println("null here" + "\n" + ledgerEveris);
        destinationPlugin.fulfillCondition(testOne.getId(), null);

        sourcePlugin.disconnect();
        destinationPlugin.disconnect();

        System.out.println(ledgerEveris);
    }

    /**
     * Creation of a ledger, accounts and plugins. Also simulate the connection/disconnection  of
     * the plugins.
     */
    private static void testCreateLedgerAccountConnections() {
        System.out.println("Creation of Ledgers, Accounts and Connectios ------------------------");

        Ledger ledgerEveris = new Ledger("test1.pound", Monetary.getCurrency(Locale.UK));
        System.out.println(ledgerEveris);

        Account jhonDoe = new Account(InterledgerAddress.of("test1.everis.jhon"),
                                    "0000", 1000);
        Account janeDoe = new Account(InterledgerAddress.of("test1.everis.jane"),
                                    "1111", 0);

        ledgerEveris.addAccount(janeDoe);
        ledgerEveris.addAccount(jhonDoe);

        System.out.println(ledgerEveris);

        PluginConnection jhonPluginConnection =
            new PluginConnection(jhonDoe.getAccountAddress(), jhonDoe.getPassword());

        PluginConnection destinationPluginConnection =
            new PluginConnection(janeDoe.getAccountAddress(), janeDoe.getPassword());

        Plugin jhonDoePlugin = new Plugin(jhonPluginConnection);
        Plugin janeDoePlugin = new Plugin(destinationPluginConnection);

        jhonDoePlugin.connect(ledgerEveris);
        janeDoePlugin.connect(ledgerEveris);

        System.out.println(ledgerEveris);

        jhonDoePlugin.disconnect();
        janeDoePlugin.disconnect();

        System.out.println(ledgerEveris);
    }
}
