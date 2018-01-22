package org.everis.interledger.tools.mockILPNetwork;

import io.vertx.core.VertxOptions;
import org.everis.interledger.config.VertXConfigSupport;
import org.everis.interledger.connector.ConnectorConfig;
import org.everis.interledger.org.everis.interledger.common.ILPTransfer;
import org.everis.interledger.plugins.BasePlugin;
import org.everis.interledger.plugins.ILPOverHTTPPlugin;
import org.interledger.InterledgerAddress;
import org.interledger.InterledgerPacketType;
import org.interledger.InterledgerProtocolException;
import org.interledger.cryptoconditions.Condition;
import org.interledger.cryptoconditions.PreimageSha256Fulfillment;
import org.interledger.ilp.InterledgerProtocolError;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class MockWebShop {
    final static InterledgerAddress WEBSHOP_ILPADDRESS = InterledgerAddress.of("g.usd.com.amazon.webshop.");

    static class RequestHandlerWebShop implements BasePlugin.IRequestHandler {
        final MockWebShop REMOTE_WEBSHOP;

        RequestHandlerWebShop() {
             REMOTE_WEBSHOP = new MockWebShop();
        }

        /**
         *  handles incomming "data" from the plugin
         * @param ilpTransfer
         * @return
         */
        public CompletableFuture<ILPResponse> onRequestReceived(ILPTransfer ilpTransfer ) {
                CompletableFuture<ILPResponse> result = new CompletableFuture<ILPResponse>();
                if (!ilpTransfer.destinationAccount.startsWith(WEBSHOP_ILPADDRESS)) {
                     InterledgerProtocolException finalExp =
                        new InterledgerProtocolException(
                            InterledgerProtocolError.builder()
                                .errorCode(InterledgerProtocolError.ErrorCode.F02_UNREACHABLE)
                                .triggeredAt(Instant.now())
                                .data(("only payments for "+WEBSHOP_ILPADDRESS.getValue()+" are allowed").getBytes())
                             .build());
                    result.completeExceptionally(finalExp);
                }
                try {
                    String sFulfillment = REMOTE_WEBSHOP.getFulfillmentOrThrow(ilpTransfer.condition);
                    result.complete(new ILPResponse(
                            InterledgerPacketType.ILP_PAYMENT_TYPE,
                            Optional.empty(),
                            Optional.of(sFulfillment)));
                } catch (Exception e){
                    InterledgerProtocolException finalExp =
                        new InterledgerProtocolException(
                            InterledgerProtocolError.builder()
                                .errorCode(InterledgerProtocolError.ErrorCode.F05_WRONG_CONDITION)
                                .triggeredAt(Instant.now())
                                .data(e.toString().getBytes())
                             .build());
                    result.completeExceptionally(finalExp);
                }
                return result;
        }
    }
    HashMap<Condition, String> pendingCC2FFQueue = new HashMap<>();
    HashMap<Condition, String>    paidCC2FFQueue = new HashMap<>();

    /**
     * Simulates previous payment setups between client payers and the webshop seller
     * A an arbitrary list of pending payments in the pedingCC2FFQueue
     * In a real app the WebShop must have some sort of SPSP end-point to communicate/agree
     * new payments with clients
     */
    private void __simulateSPSP_Pending_Payments(){
        for (int idx=1; idx<10; idx++){
            String preimage = "preimage"+idx;
            pendingCC2FFQueue.put(new PreimageSha256Fulfillment(preimage.getBytes()).getCondition(), preimage);
        }
        for (Condition key : pendingCC2FFQueue.keySet()){

            // Base64.getUrlEncoder().withoutPadding().encode(headers.get("ilp-condition").getBytes());
            System.out.println("preimage (fulfillment):"+pendingCC2FFQueue.get(key) +" -> condition: "+key.getFingerprintBase64Url());
        }
    }

    public MockWebShop() {
        __simulateSPSP_Pending_Payments();
    }

    public String getFulfillmentOrThrow(Condition condition) {
        // Simulate network/load delay
        try { Thread.sleep(1000); } catch (InterruptedException e) { }
        String result = pendingCC2FFQueue.get(condition);
        if (result == null) {
            throw new RuntimeException("No conditions matches any pending payment ");
        }
        if (true /* ACID operation in DDBB */){
            paidCC2FFQueue.put(condition, result);
            pendingCC2FFQueue.remove(condition);
        }
        return result;
    }

    private static final String pathToConfig = "ILP-Plugin/config/dev_network/two_connectors";
    public static void main(String[] args) {
        VertXConfigSupport.build(new VertxOptions()); // Init Vertx
        // Pay through connector1:

        // Reuse config. from connector2 (connector connecting to connector1) for the client plugin:
        ConnectorConfig config2 = new ConnectorConfig(pathToConfig+"/connector1.prop", Optional.empty());

        ILPOverHTTPPlugin client_plugin = (ILPOverHTTPPlugin)config2.plugins.get(0);

        ILPTransfer ilpTransfer = new ILPTransfer(
            "",
            InterledgerAddress.of(WEBSHOP_ILPADDRESS.getValue()+"account1"),
            /*String amount*/ "10000",
            Instant.now().plus(Duration.ofSeconds(20)),
            new PreimageSha256Fulfillment(("preimage"+0).getBytes()).getCondition(),
            new byte[] {}
            );
        client_plugin.sendData(ilpTransfer);
    }
}
