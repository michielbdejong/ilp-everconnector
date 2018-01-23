package org.everis.interledger.connector;

import org.everis.interledger.config.ExecutorConfigSupport;
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


/// public void notifyPreparePayment(ILPTransfer newTransfer) {
///     InterledgerAddress destinationLedgerPrefix = newTransfer.getDestinationAccount().getPrefix();
///     if(!this.connectorPLugins.containsKey(destinationLedgerPrefix)) {
///         //TODO: throw exception plugin not associated with connector
///     }

///     //new ilp transfer forward without the status
///     ILPTransfer forwardTransfer = new ILPTransfer(newTransfer.getUUID(), newTransfer.getSourceAccount(),
///             newTransfer.getDestinationAccount(), newTransfer.getAmount(), newTransfer.getPayment(),
///             newTransfer.getCondition());

///     //reach the destination plugin
///     PaymentChannelPlugin destinationPlugin = this.connectorPLugins.get(destinationLedgerPrefix);
///     destinationPlugin.prepareTransfer(forwardTransfer);
/// }

/// /**
///  * notify the source ledger for a ilp transfer fulfilled.
///  * @param transferFulfilled
///  * @param fulfillment
///  */
/// public void notifyFulfillment(ILPTransfer transferFulfilled, Fulfillment fulfillment) {
///     InterledgerAddress sourceLedgerPrefix = transferFulfilled.getSourceAccount().getPrefix();
///     if(!this.connectorPLugins.containsKey(sourceLedgerPrefix)) {
///         //TODO: throw exception plugin not associated with connector
///     }

///     PaymentChannelPlugin sourcePlugin = this.connectorPLugins.get(sourceLedgerPrefix);
///     sourcePlugin.fulfillCondition(transferFulfilled.getUUID(), fulfillment);
/// }


/// /**
///  * notidy the source ledger for a ilp transfer rejected.
///  * @param transferRejected
///  */
/// public void notifyReject(ILPTransfer transferRejected) {
///     InterledgerAddress sourceLedgerPrefix = transferRejected.getSourceAccount().getPrefix();
///     if(!this.connectorPLugins.containsKey(sourceLedgerPrefix)) {
///         //TODO: throw exception plugin not associated with connector
///     }

///     PaymentChannelPlugin sourcePlugin = this.connectorPLugins.get(sourceLedgerPrefix);
///     sourcePlugin.rejectTransfer(transferRejected.getUUID());
/// }


    public List<BasePlugin> getRegisteredPlugins() {
        return plugin_list;
    }


    /*
     * TODO:(0) There is a redundancy. First, plugins are registered attached to a handler, then the plugin
     * invoques this handler (than most probably will always be the same for the same connector) so directly
     * attaching the handler to the connector can make more sense unless for some reason the connector could
     * have different handlers for different plugins.
     */

    /**
     * Connector will try to find a response through registered handler/s.
     * If the handlers can not respond (return a fulfillment) then connector will
     * forward the data to other connector using the forwarder ("RoutingTable") for it.
     * @param registeredHandler
     * @param ilpTransfer
     * @return
     */
    public void handleRequestOrForward(
            Optional<BasePlugin.IRequestHandler> registeredHandler,
            ILPTransfer ilpTransfer, CompletableFuture<BasePlugin.DataResponse> result) {
System.out.println("deleteme con.handlerRequestOrForward: 1");
        // ExecutorConfigSupport.executor.submit(() -> {
        new Thread(() -> {
System.out.println("deleteme con.handlerRequestOrForward: 2");
            if(registeredHandler.isPresent()) {
System.out.println("deleteme con.handlerRequestOrForward: 3");
                try {
System.out.println("deleteme con.handlerRequestOrForward: 4");
                    CompletableFuture<BasePlugin.DataResponse> resultFromHandler =
                            new CompletableFuture<BasePlugin.DataResponse>();
                    registeredHandler.get().onRequestReceived(ilpTransfer, resultFromHandler);
                    final BasePlugin.DataResponse response = resultFromHandler.get();
System.out.println("deleteme con.handlerRequestOrForward: 5");
                    if (response.packetType != -1 /*continue*/) {
                        result.complete(response);
                        return;
                    }
                } catch (InterruptedException e) {
System.out.println("deleteme con.handlerRequestOrForward: 4");
                    // TODO:(0) Retry
                    InterledgerProtocolException finalExp =
                        new InterledgerProtocolException(
                            InterledgerProtocolError.builder()
                                .errorCode(InterledgerProtocolError.ErrorCode.F99_APPLICATION_ERROR)
                                .triggeredAt(Instant.now())
                                .triggeredByAddress(config.ilpAddress)
                                .data(e.toString().getBytes())
                                .build());
System.out.println("deleteme MockWebShop onRequestReceived 4.2");
                    result.completeExceptionally(finalExp);
                    return;
                } catch (ExecutionException e) {
System.out.println("deleteme con.handlerRequestOrForward: 5");
                    result.completeExceptionally(e.getCause());
                    return;
                }
            }
System.out.println("deleteme con.handlerRequestOrForward: 6");
            forwarder.forwardPayment(ilpTransfer, result);
        }).start();
    }
}
