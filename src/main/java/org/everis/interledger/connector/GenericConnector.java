package org.everis.interledger.connector;

import org.everis.interledger.org.everis.interledger.common.ILPTransfer;
import org.everis.interledger.plugins.Plugin;
import org.interledger.InterledgerAddress;
import org.interledger.cryptoconditions.Fulfillment;

import java.util.HashMap;
import java.util.Map;

/**
 * generic connector for processing transfer between x ledgers connected to the connector.
 */
public class GenericConnector {
    private final String idConnector;
    private Map<InterledgerAddress, Plugin> connectorPLugins;


    public GenericConnector(String idConnector) {
        this.idConnector = idConnector;
        this.connectorPLugins = new HashMap<InterledgerAddress, Plugin>();
    }


    public String getIdConnector() {
        return idConnector;
    }

    public void addPlugin(InterledgerAddress ledgerPrefix, Plugin newPlugin) {
        if(this.connectorPLugins.containsKey(ledgerPrefix)) {
            //TODO: throw error plugin already associated with the connector
        }

        connectorPLugins.put(ledgerPrefix, newPlugin);
    }

    public void removePlugin(Plugin removePlugin) {
        if(this.connectorPLugins.containsValue(removePlugin)) {
            //TODO: throw error plugin not associated with the connector
        }

        this.connectorPLugins.remove(removePlugin);
    }

    /**
     * notify the destination ledger for a new ilp transfer prepared.
     * @param newTransfer
     */
    public void notifyPreparePayment(ILPTransfer newTransfer) {
        InterledgerAddress destinationLedgerPrefix = newTransfer.getDestinationAccount().getPrefix();
        if(!this.connectorPLugins.containsKey(destinationLedgerPrefix)) {
            //TODO: throw exception plugin not associated with connector
        }

        //new ilp transfer forward without the status
        ILPTransfer forwardTransfer = new ILPTransfer(newTransfer.getUUID(), newTransfer.getSourceAccount(),
                newTransfer.getDestinationAccount(), newTransfer.getAmount(), newTransfer.getPayment(),
                newTransfer.getCondition());

        //reach the destination plugin
        Plugin destinationPlugin = this.connectorPLugins.get(destinationLedgerPrefix);
        destinationPlugin.prepareTransfer(forwardTransfer);
    }

    /**
     * notify the source ledger for a ilp transfer fulfilled.
     * @param transferFulfilled
     * @param fulfillment
     */
    public void notifyFulfillment(ILPTransfer transferFulfilled, Fulfillment fulfillment) {
        InterledgerAddress sourceLedgerPrefix = transferFulfilled.getSourceAccount().getPrefix();
        if(!this.connectorPLugins.containsKey(sourceLedgerPrefix)) {
            //TODO: throw exception plugin not associated with connector
        }

        Plugin sourcePlugin = this.connectorPLugins.get(sourceLedgerPrefix);
        sourcePlugin.fulfillCondition(transferFulfilled.getUUID(), fulfillment);
    }


    /**
     * notidy the source ledger for a ilp transfer rejected.
     * @param transferRejected
     */
    public void notifyReject(ILPTransfer transferRejected) {
        InterledgerAddress sourceLedgerPrefix = transferRejected.getSourceAccount().getPrefix();
        if(!this.connectorPLugins.containsKey(sourceLedgerPrefix)) {
            //TODO: throw exception plugin not associated with connector
        }

        Plugin sourcePlugin = this.connectorPLugins.get(sourceLedgerPrefix);
        sourcePlugin.rejectTransfer(transferRejected.getUUID());
    }
}
