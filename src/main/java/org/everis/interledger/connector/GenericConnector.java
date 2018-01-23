package org.everis.interledger.connector;

import org.everis.interledger.org.everis.interledger.common.ILPTransfer;
import org.everis.interledger.plugins.BasePlugin;
import org.interledger.InterledgerProtocolException;
import org.interledger.cryptoconditions.Condition;
import org.interledger.cryptoconditions.Fulfillment;
import org.interledger.ilp.InterledgerProtocolError;


import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// TODO:(0) Rename generic to Simple? Generic has many interpretations (base class, ...)

/* TODO:(0) Add handleTransfer (transfer, paymentPacket) with next logic:
 *   handleTransfer (transfer, paymentPacket) {
 *     if (this.knownFulfillments[transfer.executionCondition.toString('hex')]) {
 *       return Promise.resolve(this.knownFulfillments[transfer.executionCondition.toString('hex')])
 *     }
 *     return Promise.resolve(this.forwarder.forward(transfer, paymentPacket))
 *   },
 */

/**
 * generic connector for processing transfer between x ledgers connected to the connector.
 */
public class GenericConnector {

    public  final ConnectorConfig config;
    private final List<BasePlugin> plugin_list;
    private final Forwarder forwarder;
    private final Map<Condition, Fulfillment> knownFulfillments = new HashMap<Condition, Fulfillment>();

    private GenericConnector(ConnectorConfig config) {
        this.config = config;
        plugin_list = config.plugins;
        this.forwarder = new Forwarder(this, config.initialRoutingTable, new Object() /*quoter*/);
    }

    // private CompletableFuture<BasePlugin.DataResponse> handleTransfer(ILPTransfer ilpTransfer) {
    //     Fulfillment ff = this.knownFulfillments.get(ilpTransfer.condition);
    //     final CompletableFuture<BasePlugin.DataResponse> result;
    //     if (ff != null) {
    //         /* TODO:(RFC) Arbitrarelly incomming endToEndData from input puglin is returned.
    //          *            Must it be empty?
    //          */
    //         result = new CompletableFuture<BasePlugin.DataResponse>();
    //         result.complete(new BasePlugin.DataResponse(ff, ilpTransfer.endToEndData));
    //     } else {
    //         result = forwarder.forwardPayment(ilpTransfer);
    //     }
    //     return result;
    // }

    public static GenericConnector build(ConnectorConfig config) {
        GenericConnector result = new GenericConnector(config);
        for (BasePlugin plugin : result.plugin_list){
            plugin.setParentConnector(result);
        }
        return result;
    }

    public CompletableFuture<Void> run(){

        CompletableFuture<Void>[] connect_list = new CompletableFuture[plugin_list.size()];
        int idx=0;
        System.out.println("Connecting plugins ...");
        for (BasePlugin plugin : plugin_list) {
            System.out.println("Connecting plugin ...");
            connect_list[idx] = plugin.connect();
            idx++;
        }
        return CompletableFuture.allOf(connect_list);
    }

    /*
     * Note: There is a redundancy at this moment. First, plugins are registered and attached to a handler,
     * then the plugin invoques this handler (than most probably will always be the same for the same connector)
     * Since there is a single handler by connector attaching the handler directly to the connector makes more sense.
     * Could it be that in a future different plugins could use different handlers?
     */

    /**
     * Connector will try to find a response through the registered handler.
     * If the handlers can not respond (return a fulfillment) then connector will
     * forward the data to other connector using the forwarder ("RoutingTable") for it.
     * @param registeredHandler
     * @param ilpTransfer
     * @return
     */
    public void handleRequestOrForward(
            Optional<BasePlugin.IRequestHandler> registeredHandler,
            ILPTransfer ilpTransfer, CompletableFuture<BasePlugin.DataResponse> result) {
        new Thread(() -> {
            if(registeredHandler.isPresent()) {
                try {
                    CompletableFuture<BasePlugin.DataResponse> resultFromHandler = new CompletableFuture<>();
                    registeredHandler.get().onRequestReceived(ilpTransfer, resultFromHandler);
                    final BasePlugin.DataResponse response = resultFromHandler.get();
                    if (response.packetType != -1 /*continue*/) {
                        result.complete(response);
                        return;
                    } else {
                        // Continue, forward the request.
                    }
                } catch (InterruptedException e) {
                    // TODO:(0) Retry
                    InterledgerProtocolException finalExp =
                        new InterledgerProtocolException(
                            InterledgerProtocolError.builder()
                                .errorCode(InterledgerProtocolError.ErrorCode.F99_APPLICATION_ERROR)
                                .triggeredAt(Instant.now())
                                .triggeredByAddress(config.ilpAddress)
                                .data(e.toString().getBytes())
                                .build());
                    result.completeExceptionally(finalExp);
                    return;
                } catch (ExecutionException e) {
                    result.completeExceptionally(e.getCause());
                    return;
                }
            }
            forwarder.forwardPayment(ilpTransfer, result);
        }).start();
    }
}
