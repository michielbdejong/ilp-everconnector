package org.everis.interledger.connector;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.everis.interledger.org.everis.interledger.common.ILPTransfer;
import org.everis.interledger.plugins.BasePlugin;
import org.interledger.InterledgerAddress;
import org.interledger.cryptoconditions.Condition;
import org.interledger.cryptoconditions.Fulfillment;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    // vertx: Framework used for async handling
    public final Vertx vertx;
    ExecutorService executor = Executors.newFixedThreadPool(8 /* TODO:(1) Arbitrary max. numbers of parallel trheads*/);

    private GenericConnector(ConnectorConfig config) {
        this.config = config;
        plugin_list = config.plugins;
        this.forwarder = new Forwarder(this, config.initialRoutingTable, new Object() /*quoter*/);
        VertxOptions options = new VertxOptions();
        this.vertx = Vertx.vertx(options);
    }

    private CompletableFuture<BasePlugin.DataResponse> handleTransfer(ILPTransfer ilpTransfer) {
        Fulfillment ff = this.knownFulfillments.get(ilpTransfer.condition);
        final CompletableFuture<BasePlugin.DataResponse> result;
        if (ff != null) {
            /* TODO:(RFC) Arbitrarelly incomming endToEndData from input puglin is returned.
             *            Must it be empty?
             */
            result = new CompletableFuture<BasePlugin.DataResponse>();
            result.complete(new BasePlugin.DataResponse(ff, ilpTransfer.endToEndData));
        } else {
            result = forwarder.forwardPayment(ilpTransfer);
        }
        return result;
    }

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
     *
     * @param registeredHandler
     * @param ilpTransfer
     * @return
     */
    public CompletableFuture<BasePlugin.IRequestHandler.ILPResponse> handleRequestOrForward(
            BasePlugin.IRequestHandler registeredHandler,
            ILPTransfer ilpTransfer)
        {

        CompletableFuture<BasePlugin.IRequestHandler.ILPResponse> result =
                new CompletableFuture<BasePlugin.IRequestHandler.ILPResponse> ();

        executor.submit(() -> {
            BasePlugin.IRequestHandler.ILPResponse response = null;
            try {
                response = registeredHandler.onRequestReceived(ilpTransfer).get();
            } catch (InterruptedException e) {
                // TODO:(0) Retry
            } catch (ExecutionException e) {
                // TODO:(0) propagate exception
                forwarder.forwardPayment(ilpTransfer);
            }
            if (response.optBase64Fulfillment.isPresent()) {
                result.complete(response);
            } else {
                // TODO:(0) Forward to next opt
            }

        });
        return result;
    }
}
