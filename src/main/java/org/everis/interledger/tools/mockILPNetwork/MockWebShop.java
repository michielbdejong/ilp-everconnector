package org.everis.interledger.tools.mockILPNetwork;

import io.vertx.core.VertxOptions;
import org.everis.interledger.config.ExecutorConfigSupport;
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
import java.util.Base64;
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
        @Override
        public void onRequestReceived(ILPTransfer ilpTransfer, CompletableFuture<BasePlugin.DataResponse>  result ) {
System.out.println("deleteme MockWebShop onRequestReceived 1");
            // ExecutorConfigSupport.executor.submit(() -> {
            new Thread(() -> {
                if (!ilpTransfer.destinationAccount.startsWith(WEBSHOP_ILPADDRESS)) {
System.out.println("deleteme MockWebShop onRequestReceived 2");
                    InterledgerProtocolException finalExp =
                            new InterledgerProtocolException(
                                    InterledgerProtocolError.builder()
                                            .errorCode(InterledgerProtocolError.ErrorCode.F02_UNREACHABLE)
                                            .triggeredAt(Instant.now())
                                            .data(("only payments for " + WEBSHOP_ILPADDRESS.getValue() + " are allowed").getBytes())
                                            .build());
                    result.completeExceptionally(finalExp);
                }
                try {
System.out.println("deleteme MockWebShop onRequestReceived 3");
                    result.complete(new BasePlugin.DataResponse(
                            InterledgerPacketType.ILP_PAYMENT_TYPE,
                            Optional.of(REMOTE_WEBSHOP.getFulfillmentOrThrow(ilpTransfer.condition)),
                            new byte[] {},
                            Optional.empty()));
                } catch (Exception e) {
System.out.println("deleteme MockWebShop onRequestReceived 4.1"+ e.toString());
                    InterledgerProtocolException finalExp =
                        new InterledgerProtocolException(
                            InterledgerProtocolError.builder()
                                .errorCode(InterledgerProtocolError.ErrorCode.F05_WRONG_CONDITION)
                                .triggeredAt(Instant.now())
                                .triggeredByAddress(WEBSHOP_ILPADDRESS)
                                .data(e.toString().getBytes())
                                .build());
System.out.println("deleteme MockWebShop onRequestReceived 4.2");
                    result.completeExceptionally(finalExp);
                }
            // });
             }).start();
        }
    }
    HashMap<String /*Base64URLencoded condition*/, String/*Base64URLencoded ff*/> pendingCC2FFQueue = new HashMap<>();
    HashMap<String /*Base64URLencoded condition*/, String/*Base64URLencoded ff*/>    paidCC2FFQueue = new HashMap<>();

    /**
     * Simulates previous payment setups between client payers and the webshop seller
     * A an arbitrary list of pending payments in the pedingCC2condition.getFingerprintBase64Url()FFQueue
     * In a real app the WebShop must have some sort of SPSP end-point to communicate/agree
     * new payments with clients
     */
    private void __simulateSPSP_Pending_Payments(){
        for (int idx=1; idx<10; idx++){
            String preimage = "preimage0000000000000000000000"+idx;
            pendingCC2FFQueue.put(
                    new PreimageSha256Fulfillment(Base64.getUrlDecoder().decode(preimage)).getCondition().getFingerprintBase64Url(), preimage);
        }
        for (String key : pendingCC2FFQueue.keySet()){
            // Base64.getUrlEncoder().withoutPadding().encode(headers.get("ilp-condition").getBytes());
            System.out.println("preimage (fulfillment):"+pendingCC2FFQueue.get(key) +" -> condition: "+key);
        }
    }

    public MockWebShop() {
        __simulateSPSP_Pending_Payments();
    }

    public PreimageSha256Fulfillment getFulfillmentOrThrow(Condition condition) {
        String base64URLCC = condition.getFingerprintBase64Url();
        // Simulate network/load delay
        try { Thread.sleep(1000); } catch (InterruptedException e) { }
        String result = pendingCC2FFQueue.get(base64URLCC);
        if (result == null) {
            throw new RuntimeException("No condition "+base64URLCC+" matches any pending payment ");
        }
        if (true /* ACID operation in DDBB */){
            paidCC2FFQueue.put(base64URLCC, result);
            pendingCC2FFQueue.remove(base64URLCC);
        }
        return new PreimageSha256Fulfillment(Base64.getUrlDecoder().decode(result));
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
        CompletableFuture<BasePlugin.DataResponse> result = new CompletableFuture<BasePlugin.DataResponse>();
        client_plugin.sendData(ilpTransfer, result);
    }
}
