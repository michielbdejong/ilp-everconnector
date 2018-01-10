package org.everis.interledger.plugins;

// REF: https://github.com/interledgerjs/ilp-plugin-btp
import org.everis.interledger.config.plugin.PaymentChannelConfig;
import org.everis.interledger.org.everis.interledger.common.LedgerInfo;
import org.interledger.InterledgerAddress;


import java.math.BigInteger;
import java.nio.Buffer;
import java.util.concurrent.CompletableFuture;

/**
 * Entity of Plugin to connect any sender, receiver or connector with a remoteLedgerAdaptor.
 */
// TODO:(0) Rename as BaseBTPPlugin
public class PaymentChannelPlugin extends BasePlugin {
    final PaymentChannelConfig pluginConfig;
    private PaymentChannelPlugin peerPaymentChannelPlugin = null;

    /*
     * TODO:(1) In a production plugin re-read current balance
     *         after reload/reboot
     */
    private BigInteger balanceWithPeer = BigInteger.ZERO;
    final private BigInteger maxIOYAmmount ;
    final private BigInteger maxYOMAmmount ;

    private Status status = Status.DISCONNECTED;

    /**
     * Connector with a PluginConnection object.
     * @param pluginConfig   : Config for initial setup
     */
    public PaymentChannelPlugin(
            PaymentChannelConfig pluginConfig) {
     /*
      *     InterledgerAddress ledgerPrefix,
      *     LocalLedgerILPAdaptor ledger,
      *     LedgerConnectionConfig ledgerConnection) {
      */
        super(pluginConfig);
        this.pluginConfig = pluginConfig;
        maxIOYAmmount = this.pluginConfig.maxIOYAmmount;
        maxYOMAmmount = this.pluginConfig.maxIOYAmmount;


    }

    /**
     * connect the plugin with the remoteLedgerAdaptor in parameter.
     */
    @Override
    public CompletableFuture<Void> connect() {
        // TODO:(0) IMPLEMENT
        this.status = Status.CONNECTED;
        CompletableFuture<Void> result = new CompletableFuture<Void>();
        return result;
    }

    @Override
    public CompletableFuture<Void> disconnect() {
        // TODO:(0) IMPLEMENT
        this.status = Status.DISCONNECTED;
        CompletableFuture<Void> result = new CompletableFuture<Void>();
        return result;
    }

    @Override
    public boolean isConnected() {
        return this.status == Status.CONNECTED;
    }

    @Override
    public LedgerInfo getInfo(){
        if (!isConnected()) {
            // TODO:(0) Use correct ILP Exception
            throw new RuntimeException("plugin not connected");
        }
        // TODO:(0) Implement
        // TODO:(0) Check currency from ledger info, not from initial pluginConfig
        LedgerInfo result = new LedgerInfo(InterledgerAddress.of("peer.TODO:(0)"), pluginConfig.currency);
        return result;
    }

    @Override
	public String getAccount() {
        return this.pluginConfig.account;
    }

    @Override
	public void registerRequestHandler(IRequestHandler requestHandler ) {
        // TODO:(0) Implement

    }

    @Override
	public void deregisterRequestHandler ( ) {
        // TODO:(0) Implement
    }

    /**
     * first step of the ILP flow to prepare a transfer by transferring the funds on the hold account and put
     * the transfer status as "PREPARED".
     *
     * @param data
     */
    @Override
    public CompletableFuture<Buffer> sendData(Buffer data) {
        if (!isConnected()) {
            // TODO:(0) Use correct ILP Exception
            throw new RuntimeException("plugin not connected");
        }
        // TODO:(0) Implement
        CompletableFuture<Buffer> result = new CompletableFuture<Buffer>();
        return result;
    }

    /**
     * Send Fulfillment or any data needed for the payment
     *
     * @param amount
     */
    @Override
    public CompletableFuture<Void> sendMoney(String amount) {
        if (!this.isConnected()) {
            // TODO:(0) Use correct ILP Exception
            throw new RuntimeException("Plugin not connected");
        }

        // ILPTransfer transfer = this.ilpPendingTransfers.get(transferId);
        // TODO:(0) Implement
        CompletableFuture<Void> result = new CompletableFuture<Void>();
        return result;

    }

}
