package org.everis.interledger.plugins;

public class MockSettlementLedgerPlugin {
////public class MockSettlementLedgerPlugin extends BasePlugin {
////    private final GenericConnector parentConnector;
////    private final MockSettlementConfig pluginConfig;
////    private Status status = Status.DISCONNECTED;
////    private final LocalLedgerILPAdaptor remoteLedgerAdaptor;
////
////    MockSettlementLedgerPlugin(GenericConnector parentConnector, MockSettlementConfig pluginConfig) {
////        super(pluginConfig);
////        this.parentConnector = parentConnector;
////        this.pluginConfig = pluginConfig;
////        this.remoteLedgerAdaptor = new LocalLedgerILPAdaptor(this.pluginConfig.currency);
////    }
////
////    /**
////     * connect the plugin with the remoteLedgerAdaptor in parameter.
////     */
////    @Override
////    public void connect() {
////        // Simulate remote connection
////        this.remoteLedgerAdaptor.onILPConnectRequest(parentConnector.getIlpAddress(),
////                pluginConfig.account, pluginConfig.account_pass);
////        this.status = Status.CONNECTED;
////    }
////
////    @Override
////    public void disconnect() {
////        this.remoteLedgerAdaptor.disconnect();
////        this.status = Status.DISCONNECTED;
////    }
////
////@Override
////public boolean isConnected() {
////    return this.status == Status.CONNECTED;
////}

}
