package org.everis.interledger.connector;

import org.everis.interledger.plugins.BasePlugin;
import org.interledger.InterledgerAddress;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

// TODO:(0) Rename generic to Simple? Generic has many interpretations (base class, ...)
/**
 * generic connector for processing transfer between x ledgers connected to the connector.
 */
public class GenericConnector {
    public final ConnectorConfig config;
    private final List<BasePlugin> plugin_list;
    private RouteTable routeTable;

    private GenericConnector(ConnectorConfig config) {
        plugin_list = config.plugins;
        /* For now (2018-01-08) the inital routing table
         * from basePluginConfig is the final one */
        routeTable = config.initialRoutingTable;
        this.config = config;
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
        for (BasePlugin plugin : plugin_list) {
            connect_list[idx] = plugin.connect();
            idx++;
        }
        return CompletableFuture.allOf(connect_list);
    }

/// /**
///  * notify the destination ledger for a new ilp transfer prepared.
///  * @param newTransfer
///  */
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
