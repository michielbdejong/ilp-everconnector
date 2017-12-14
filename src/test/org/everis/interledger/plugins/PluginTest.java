package org.everis.interledger.plugins;

import org.everis.interledger.org.everis.interledger.common.ILPTransfer;
import org.everis.interledger.tools.mockLedger.LocalAccount;
import org.everis.interledger.tools.mockLedger.MockInMemoryLedger;
import org.interledger.InterledgerAddress;
import org.interledger.cryptoconditions.Fulfillment;
import org.interledger.cryptoconditions.PrefixSha256Fulfillment;
import org.interledger.cryptoconditions.PreimageSha256Fulfillment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.interledger.ilp.InterledgerPayment;


import javax.money.Monetary;

import java.math.BigInteger;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class PluginTest {

    final static String ID_CONNECTOR_EVERIS = "connectorEveris";
    final static InterledgerAddress CONNECTOR_ADDRESS = InterledgerAddress.of("g.everis.connector1");
    final static String LEDGER1_PREFIX = "test1.pound.";
    final static String LEDGER2_PREFIX = "test1.rupias.";

    MockInMemoryLedger bankOfIngland, bankOfLepe;

    Plugin plugin1;
    Plugin plugin2;

    @BeforeEach
    void setUp() {
        // Create two new banks.
        bankOfIngland = new MockInMemoryLedger(
            InterledgerAddress.of(LEDGER1_PREFIX), Monetary.getCurrency("GBP"));

        bankOfLepe = new MockInMemoryLedger(
            InterledgerAddress.of(LEDGER2_PREFIX), Monetary.getCurrency("EUR"));

        // Register connector accounts in both ledgers
        bankOfIngland.addAccount(new LocalAccount(ID_CONNECTOR_EVERIS , ID_CONNECTOR_EVERIS , 1000));
        bankOfLepe   .addAccount(new LocalAccount(ID_CONNECTOR_EVERIS , ID_CONNECTOR_EVERIS , 1000));

        // Fill some test users in each bank
        bankOfIngland.addAccount(new LocalAccount("user1_1" , "user1_1" , 10000));
        bankOfIngland.addAccount(new LocalAccount("user1_2" , "user1_2" , 10000));

        bankOfLepe   .addAccount(new LocalAccount("user2_1" , "user2_1" , 10000));
        bankOfLepe   .addAccount(new LocalAccount("user2_2" , "user2_2" , 10000));

        // Finally instantiate two plugins. Indicate an existing bank/ledger and the needed info to connect.
        plugin1 = new Plugin(bankOfIngland, new Plugin.LedgerConnection(ID_CONNECTOR_EVERIS, ID_CONNECTOR_EVERIS, CONNECTOR_ADDRESS));
        plugin2 = new Plugin(bankOfLepe   , new Plugin.LedgerConnection(ID_CONNECTOR_EVERIS, ID_CONNECTOR_EVERIS, CONNECTOR_ADDRESS));

    }

    @Test
    void connectDisconnect() {
        assertFalse(plugin1.isConnected());
        plugin1.connect();
        assertTrue(plugin1.isConnected());
        plugin1.disconnect();
        assertFalse(plugin1.isConnected());
    }

    @Test
    void wrongCredentialsCanNotConnect() {
        try {
            Plugin pluginWrongCredentials = new Plugin(
                    bankOfIngland,
                    new Plugin.LedgerConnection(
                            ID_CONNECTOR_EVERIS, "hacker_from_Lepe_Trying_To_Connect_With_this_Wrong_Password", CONNECTOR_ADDRESS));
            pluginWrongCredentials.connect();
            assertTrue(false, "This code must never be reached. Wrong credentials must not be able to connect");
        } catch (Exception e){
            assertTrue(true);
        }
    }

    @Test
    void sendTransfer() {
        // TODO:(0) DOUBT: The plugin <-> ledger must handler ILPTransfer objects or just localTransfers + InterledgerPayments
        InterledgerAddress connector1Addr = InterledgerAddress.of(LEDGER1_PREFIX+ID_CONNECTOR_EVERIS);
        InterledgerAddress srcAccountAddr = InterledgerAddress.of(LEDGER1_PREFIX+"user1_2");
        InterledgerAddress dstAccountAddr = InterledgerAddress.of(LEDGER1_PREFIX+"user1_2");
        byte[] preimage = "LEPE_IS_COOL".getBytes();
        Fulfillment fulfillment = new PreimageSha256Fulfillment(preimage);
        int amount = 100;
        String UUID0 = "UUID0";
        ILPTransfer ilpTransfer = new ILPTransfer
                (UUID0,
                 srcAccountAddr,
                 dstAccountAddr,
                 amount,
                 InterledgerPayment.builder()
                         .destinationAccount(dstAccountAddr)
                         .destinationAmount(new BigInteger(""+amount))
                         .data(new byte[]{})
                         .build(),
                 fulfillment.getCondition()
                 );
        int initial_holdBalance      = bankOfIngland.getHoldAccount().getBalance();
        int initial_connectorBalance = bankOfIngland.getAccountByILPAddress(connector1Addr).getBalance();

        plugin1.sendTransfer(ilpTransfer);
        int        holdBalance_prepared = bankOfIngland.getHoldAccount().getBalance();
        int   connectorBalance_prepared = bankOfIngland.getAccountByILPAddress(connector1Addr).getBalance();
        assertEquals(initial_holdBalance + amount, holdBalance_prepared);
        assertEquals(initial_connectorBalance, connectorBalance_prepared);

        plugin1.fulfillCondition(UUID0, fulfillment);
        int        holdBalance_fulfilled = bankOfIngland.getHoldAccount().getBalance();
        int   connectorBalance_fulfilled = bankOfIngland.getAccountByILPAddress(connector1Addr).getBalance();
        assertEquals(initial_holdBalance, holdBalance_fulfilled);
        assertEquals(initial_connectorBalance + amount, connectorBalance_fulfilled);
    }

    @Test
    void fulfillCondition() {
    }

    @Test
    void rejectTransfer() {
    }

}