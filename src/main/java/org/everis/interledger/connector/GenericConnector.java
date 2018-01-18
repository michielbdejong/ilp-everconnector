package org.everis.interledger.connector;

import org.everis.interledger.org.everis.interledger.common.ILPTransfer;
import org.everis.interledger.plugins.BasePlugin;
import org.interledger.cryptoconditions.Condition;
import org.interledger.cryptoconditions.Fulfillment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

// TODO:(0) Rename generic to Simple? Generic has many interpretations (base class, ...)

// TODO:(0) Add default route
// TODO:(0) Add list of known fulfillments
/* TODO:(0) Add handleTransfer (transfer, paymentPacket) with next logic:
 *   handleTransfer (transfer, paymentPacket) {
 *     if (this.knownFulfillments[transfer.executionCondition.toString('hex')]) {
 *       return Promise.resolve(this.knownFulfillments[transfer.executionCondition.toString('hex')])
 *     }
 *     return Promise.resolve(this.forwarder.forward(transfer, paymentPacket))
 *   },
 */
// TODO:(0) Add forwarder entity?

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

    private CompletableFuture<Fulfillment> handleTransfer(ILPTransfer transfer) {
        CompletableFuture<Fulfillment> result = new CompletableFuture<Fulfillment>();
        Fulfillment ff = this.knownFulfillments.get(transfer.getCondition());
        if (this.knownFulfillments.get(transfer.getCondition()) != null) {
            result.complete(ff);
        } else {
            return forwarder.forwardPayment(transfer, new Object() /*TODO:(0) paymentPacket*/);
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
}
