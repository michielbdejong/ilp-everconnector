package org.everis.interledger.connector;

import org.everis.interledger.org.everis.interledger.common.ILPTransfer;
import org.everis.interledger.plugins.LedgerConnection;
import org.everis.interledger.plugins.Plugin;
import org.everis.interledger.tools.mockLedger.LocalAccount;
import org.everis.interledger.tools.mockLedger.LocalLedgerILPAdaptor;
import org.interledger.InterledgerAddress;
import org.interledger.cryptoconditions.Fulfillment;
import org.interledger.cryptoconditions.PreimageSha256Fulfillment;
import org.interledger.ilp.InterledgerPayment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.money.Monetary;
import java.math.BigInteger;

public class GenericConnectorTest {
    final static String ID_CONNECTOR_EVERIS = "connectorEveris";
    final static InterledgerAddress CONNECTOR_ADDRESS = InterledgerAddress.of("test1.connector");
    final static String LEDGER_POUND_PREFIX = "test1.pound.";
    final static String LEDGER_EURO_PREFIX = "test1.euro.";

    GenericConnector connector;
    LocalLedgerILPAdaptor bankOfEngland, bankOfFrance;
    Plugin pluginBankEngland, pluginBankFrance;
    InterledgerAddress srcAccountAddr, dstAccountAddr;


    @BeforeEach
    void setUp() {
        //instance of the connector with the id connector use for build the ilp address for the connector accounts
        connector = new GenericConnector(ID_CONNECTOR_EVERIS);

        bankOfEngland = new LocalLedgerILPAdaptor(InterledgerAddress.of(LEDGER_POUND_PREFIX),
                Monetary.getCurrency("GBP"), connector);
        bankOfFrance = new LocalLedgerILPAdaptor(InterledgerAddress.of(LEDGER_EURO_PREFIX),
                Monetary.getCurrency("EUR"), connector);

        pluginBankEngland = new Plugin(bankOfEngland, new LedgerConnection(ID_CONNECTOR_EVERIS, "password", CONNECTOR_ADDRESS));
        pluginBankFrance = new Plugin(bankOfFrance   , new LedgerConnection(ID_CONNECTOR_EVERIS, "password", CONNECTOR_ADDRESS));


        //associate the plugins to the connector
        connector.addPlugin(InterledgerAddress.of(LEDGER_POUND_PREFIX), pluginBankEngland);
        connector.addPlugin(InterledgerAddress.of(LEDGER_EURO_PREFIX), pluginBankFrance);

        // Register connector accounts and test users in both ledgers
        bankOfEngland.addAccount(new LocalAccount(ID_CONNECTOR_EVERIS , "password" , 10000));
        bankOfFrance.addAccount(new LocalAccount(ID_CONNECTOR_EVERIS , "password" , 10000));
        bankOfEngland.addAccount(new LocalAccount("user_england" , "password" , 1000));
        bankOfFrance.addAccount(new LocalAccount("user_france" , "password" , 1000));
        srcAccountAddr = InterledgerAddress.of(LEDGER_POUND_PREFIX + "user_england");
        dstAccountAddr = InterledgerAddress.of(LEDGER_EURO_PREFIX + "user_france");
    }

    @Test
    void fulfillTransferPoundsToEuro() {
        System.out.println("---------------------Transfer Fulfilled Pounds to Euro---------------------");
        int amount = 100;
        String UUID0 = "UUID0";
        byte[] preimage = "c_comment?".getBytes();
        Fulfillment fulfillment = new PreimageSha256Fulfillment(preimage);
        ILPTransfer ilpTransfer = new ILPTransfer(UUID0,
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

        //connect the plugins to their associated ledger
        pluginBankEngland.connect();
        pluginBankFrance.connect();

        //prepare the transfer
        pluginBankEngland.prepareTransfer(ilpTransfer);
        int srcBalancePrepare = bankOfEngland.getAccountByILPAddress(srcAccountAddr).getBalance();
        int holdAccBankEnglandPrepare = bankOfEngland.getHoldAccount().getBalance();
        int srcConnectorPrepare = bankOfEngland
                .getAccountByILPAddress(InterledgerAddress.of(LEDGER_POUND_PREFIX + ID_CONNECTOR_EVERIS))
                .getBalance();
        int dstConnectorPrepare = bankOfFrance
                .getAccountByILPAddress(InterledgerAddress.of(LEDGER_EURO_PREFIX + ID_CONNECTOR_EVERIS))
                .getBalance();
        int holdAccBankFrancePrepare = bankOfFrance.getHoldAccount().getBalance();
        int destBalancePrepare = bankOfFrance.getAccountByILPAddress(dstAccountAddr).getBalance();
        System.out.println("---------------------Transfer prepare---------------------");
        System.out.println("srcBalance: " + srcBalancePrepare);
        System.out.println("holdAccBankEngland: " + holdAccBankEnglandPrepare);
        System.out.println("srcConnector: " + srcConnectorPrepare);
        System.out.println("dstConnector: " + dstConnectorPrepare);
        System.out.println("holdAccBankFrance: " + holdAccBankFrancePrepare);
        System.out.println("destBalance: " + destBalancePrepare);

        //fulfill the transfer
        pluginBankFrance.fulfillCondition(ilpTransfer.getUUID(), fulfillment);
        int srcBalance = bankOfEngland.getAccountByILPAddress(srcAccountAddr).getBalance();
        int holdAccBankEngland = bankOfEngland.getHoldAccount().getBalance();
        int srcConnector = bankOfEngland
                .getAccountByILPAddress(InterledgerAddress.of(LEDGER_POUND_PREFIX + ID_CONNECTOR_EVERIS))
                .getBalance();
        int dstConnector = bankOfFrance
                .getAccountByILPAddress(InterledgerAddress.of(LEDGER_EURO_PREFIX + ID_CONNECTOR_EVERIS))
                .getBalance();
        int holdAccBankFrance = bankOfFrance.getHoldAccount().getBalance();
        int destBalance = bankOfFrance.getAccountByILPAddress(dstAccountAddr).getBalance();
        System.out.println("---------------------Transfer fulfill---------------------");
        System.out.println("srcBalance: " + srcBalance);
        System.out.println("holdAccBankEngland: " + holdAccBankEngland);
        System.out.println("srcConnector: " + srcConnector);
        System.out.println("dstConnector: " + dstConnector);
        System.out.println("holdAccBankFrance: " + holdAccBankFrance);
        System.out.println("destBalance: " + destBalance + "\n");
    }

    @Test
    public void fulfillTransferEuroToPounds() {
        System.out.println("---------------------Transfer Fulfilled Euro to Pounds---------------------");
        int amount = 100;
        String UUID0 = "UUID0";
        byte[] preimage = "c_comment?".getBytes();
        Fulfillment fulfillment = new PreimageSha256Fulfillment(preimage);
        ILPTransfer ilpTransfer = new ILPTransfer(UUID0,
                dstAccountAddr,
                srcAccountAddr,
                amount,
                InterledgerPayment.builder()
                        .destinationAccount(srcAccountAddr)
                        .destinationAmount(new BigInteger(""+amount))
                        .data(new byte[]{})
                        .build(),
                fulfillment.getCondition()
        );

        //connect the plugins to their associated ledger
        pluginBankEngland.connect();
        pluginBankFrance.connect();

        //prepare the transfer
        pluginBankFrance.prepareTransfer(ilpTransfer);
        int srcBalancePrepare = bankOfFrance.getAccountByILPAddress(dstAccountAddr).getBalance();
        int holdAccBankFrancePrepare = bankOfFrance.getHoldAccount().getBalance();
        int srcConnectorPrepare = bankOfFrance
                .getAccountByILPAddress(InterledgerAddress.of(LEDGER_EURO_PREFIX + ID_CONNECTOR_EVERIS))
                .getBalance();
        int dstConnectorPrepare = bankOfEngland
                .getAccountByILPAddress(InterledgerAddress.of(LEDGER_POUND_PREFIX + ID_CONNECTOR_EVERIS))
                .getBalance();
        int holdAccBankEnglandPrepare = bankOfEngland.getHoldAccount().getBalance();
        int destBalancePrepare = bankOfEngland.getAccountByILPAddress(srcAccountAddr).getBalance();
        System.out.println("---------------------Transfer prepare---------------------");
        System.out.println("srcBalance: " + srcBalancePrepare);
        System.out.println("holdAccBankFrance: " + holdAccBankFrancePrepare);
        System.out.println("srcConnector: " + srcConnectorPrepare);
        System.out.println("dstConnector: " + dstConnectorPrepare);
        System.out.println("holdAccBankEngland: " + holdAccBankEnglandPrepare);
        System.out.println("destBalance: " + destBalancePrepare);

        //fulfill the transfer
        pluginBankEngland.fulfillCondition(ilpTransfer.getUUID(), fulfillment);
        int srcBalance = bankOfFrance.getAccountByILPAddress(dstAccountAddr).getBalance();
        int holdAccBankFrance = bankOfFrance.getHoldAccount().getBalance();
        int srcConnector = bankOfFrance
                .getAccountByILPAddress(InterledgerAddress.of(LEDGER_EURO_PREFIX + ID_CONNECTOR_EVERIS))
                .getBalance();
        int dstConnector = bankOfEngland
                .getAccountByILPAddress(InterledgerAddress.of(LEDGER_POUND_PREFIX + ID_CONNECTOR_EVERIS))
                .getBalance();
        int holdAccBankEngland = bankOfEngland.getHoldAccount().getBalance();
        int destBalance = bankOfEngland.getAccountByILPAddress(srcAccountAddr).getBalance();
        System.out.println("---------------------Transfer fulfill---------------------");
        System.out.println("srcBalance: " + srcBalance);
        System.out.println("holdAccBankFrance: " + holdAccBankFrance);
        System.out.println("srcConnector: " + srcConnector);
        System.out.println("dstConnector: " + dstConnector);
        System.out.println("holdAccBankEngland: " + holdAccBankEngland);
        System.out.println("destBalance: " + destBalance + "\n");
    }

    @Test
    void rejectTransferPoundsToEuro() {
        System.out.println("---------------------Transfer Rejected Pounds to Euro---------------------");
        int amount = 100;
        String UUID0 = "UUID0";
        byte[] preimage = "c_comment?".getBytes();
        Fulfillment fulfillment = new PreimageSha256Fulfillment(preimage);
        ILPTransfer ilpTransfer = new ILPTransfer(UUID0,
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

        //connect the plugins to their associated ledger
        pluginBankEngland.connect();
        pluginBankFrance.connect();

        //prepare the transfer
        pluginBankEngland.prepareTransfer(ilpTransfer);
        int srcBalancePrepare = bankOfEngland.getAccountByILPAddress(srcAccountAddr).getBalance();
        int holdAccBankEnglandPrepare = bankOfEngland.getHoldAccount().getBalance();
        int srcConnectorPrepare = bankOfEngland
                .getAccountByILPAddress(InterledgerAddress.of(LEDGER_POUND_PREFIX + ID_CONNECTOR_EVERIS))
                .getBalance();
        int dstConnectorPrepare = bankOfFrance
                .getAccountByILPAddress(InterledgerAddress.of(LEDGER_EURO_PREFIX + ID_CONNECTOR_EVERIS))
                .getBalance();
        int holdAccBankFrancePrepare = bankOfFrance.getHoldAccount().getBalance();
        int destBalancePrepare = bankOfFrance.getAccountByILPAddress(dstAccountAddr).getBalance();
        System.out.println("---------------------Transfer prepare---------------------");
        System.out.println("srcBalance: " + srcBalancePrepare);
        System.out.println("holdAccBankEngland: " + holdAccBankEnglandPrepare);
        System.out.println("srcConnector: " + srcConnectorPrepare);
        System.out.println("dstConnector: " + dstConnectorPrepare);
        System.out.println("holdAccBankFrance: " + holdAccBankFrancePrepare);
        System.out.println("destBalance: " + destBalancePrepare);

        //fulfill the transfer
        pluginBankFrance.rejectTransfer(ilpTransfer.getUUID());
        int srcBalance = bankOfEngland.getAccountByILPAddress(srcAccountAddr).getBalance();
        int holdAccBankEngland = bankOfEngland.getHoldAccount().getBalance();
        int srcConnector = bankOfEngland
                .getAccountByILPAddress(InterledgerAddress.of(LEDGER_POUND_PREFIX + ID_CONNECTOR_EVERIS))
                .getBalance();
        int dstConnector = bankOfFrance
                .getAccountByILPAddress(InterledgerAddress.of(LEDGER_EURO_PREFIX + ID_CONNECTOR_EVERIS))
                .getBalance();
        int holdAccBankFrance = bankOfFrance.getHoldAccount().getBalance();
        int destBalance = bankOfFrance.getAccountByILPAddress(dstAccountAddr).getBalance();
        System.out.println("---------------------Transfer reject---------------------");
        System.out.println("srcBalance: " + srcBalance);
        System.out.println("holdAccBankEngland: " + holdAccBankEngland);
        System.out.println("srcConnector: " + srcConnector);
        System.out.println("dstConnector: " + dstConnector);
        System.out.println("holdAccBankFrance: " + holdAccBankFrance);
        System.out.println("destBalance: " + destBalance + "\n");
    }
}
