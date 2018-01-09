package org.everis.interledger.connector;

public class ConnectorTest {
    /*final static String ID_CONNECTOR_EVERIS = "connectorEveris";
    final static InterledgerAddress CONNECTOR_ADDRESS = InterledgerAddress.of("test1.connector");
    final static String LEDGER_POUND_PREFIX = "test1.pound.";
    final static String LEDGER_EURO_PREFIX = "test1.euro.";

    LocalLedgerILPAdaptor bankOfEngland, bankOfFrance;
    Plugin pluginBankEngland, pluginBankFrance;
    SimpleConnector connector;
    InterledgerAddress srcAccountAddr, dstAccountAddr;


    @BeforeEach
    public void setUp() {
        //Create a connector
        connector = new SimpleConnector();

        // Create two new banks.
        bankOfEngland = new LocalLedgerILPAdaptor(InterledgerAddress.of(LEDGER_POUND_PREFIX),
                Monetary.getCurrency("GBP"), connector);
        bankOfFrance = new LocalLedgerILPAdaptor(InterledgerAddress.of(LEDGER_EURO_PREFIX), Monetary.getCurrency("EUR"), connector);

        // Finally instantiate two plugins. Indicate an existing bank/ledger and the needed info to connect.
        pluginBankEngland = new Plugin(bankOfEngland, new LedgerConnection(ID_CONNECTOR_EVERIS, "password", CONNECTOR_ADDRESS));
        pluginBankFrance = new Plugin(bankOfFrance   , new LedgerConnection(ID_CONNECTOR_EVERIS, "password", CONNECTOR_ADDRESS));

        //associate the plugind to the connector
        connector.setPluginSourceLedger(pluginBankEngland);
        connector.setPluginDestinationLedger(pluginBankFrance);

        // Register connector accounts in both ledgers
        bankOfEngland.addAccount(new LocalAccount(ID_CONNECTOR_EVERIS , "password" , 1000));
        bankOfFrance.addAccount(new LocalAccount(ID_CONNECTOR_EVERIS , "password" , 1000));

        // Fill some test users in each bank
        bankOfEngland.addAccount(new LocalAccount("user_england" , "password" , 10000));
        bankOfFrance.addAccount(new LocalAccount("user_france" , "password" , 10000));
        srcAccountAddr = InterledgerAddress.of(LEDGER_POUND_PREFIX + "user_england");
        dstAccountAddr = InterledgerAddress.of(LEDGER_EURO_PREFIX + "user_france");
    }

    @Test
    public void sendTransferThroughConnector() {
        System.out.println("---------------------Transfer Fulfilled---------------------");
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
    public void rejectTransferThroughConnector() {
        System.out.println("---------------------Transfer Rejected---------------------");
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
        System.out.println("---------------------Reject Transfer---------------------");
        System.out.println("srcBalance: " + srcBalance);
        System.out.println("holdAccBankEngland: " + holdAccBankEngland);
        System.out.println("srcConnector: " + srcConnector);
        System.out.println("dstConnector: " + dstConnector);
        System.out.println("holdAccBankFrance: " + holdAccBankFrance);
        System.out.println("destBalance: " + destBalance + "\n");
    }*/
}
