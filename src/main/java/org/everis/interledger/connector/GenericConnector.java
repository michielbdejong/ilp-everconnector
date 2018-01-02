package org.everis.interledger.connector;

import org.everis.interledger.org.everis.interledger.common.ILPTransfer;
import org.everis.interledger.plugins.Plugin;
import org.interledger.InterledgerAddress;
import org.interledger.cryptoconditions.Fulfillment;

import java.util.HashMap;
import java.util.Map;

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

    public void notifyPreparePayment(ILPTransfer newTransfer) {
        InterledgerAddress destinationLedgerPrefix = newTransfer.getDestinationAccount().getPrefix();
        if(!this.connectorPLugins.containsValue(destinationLedgerPrefix)) {
            //TODO: throw exception plugin not associated with connector
        }

        ILPTransfer forwardTransfer = new ILPTransfer(newTransfer.getUUID(), newTransfer.getSourceAccount(),
                newTransfer.getDestinationAccount(), newTransfer.getAmount(), newTransfer.getPayment(),
                newTransfer.getCondition());

        Plugin destinationPlugin = this.connectorPLugins.get(destinationLedgerPrefix);
//        destinationPlugin.prepareTransfer(newTransfer);
        destinationPlugin.prepareTransfer(forwardTransfer);
    }

    public void notifyFulfillment(ILPTransfer transferFulfilled, Fulfillment fulfillment) {
        InterledgerAddress sourceLedgerPrefix = transferFulfilled.getSourceAccount().getPrefix();
        if(!this.connectorPLugins.containsValue(sourceLedgerPrefix)) {
            //TODO: throw exception plugin not associated with connector
        }

        Plugin sourcePlugin = this.connectorPLugins.get(sourceLedgerPrefix);
        sourcePlugin.fulfillCondition(transferFulfilled.getUUID(), fulfillment);
    }

    public void notifyReject(ILPTransfer transferRejected) {
        InterledgerAddress sourceLedgerPrefix = transferRejected.getSourceAccount().getPrefix();
        if(!this.connectorPLugins.containsValue(sourceLedgerPrefix)) {
            //TODO: throw exception plugin not associated with connector
        }

        Plugin sourcePlugin = this.connectorPLugins.get(sourceLedgerPrefix);
        sourcePlugin.rejectTransfer(transferRejected.getUUID());
    }
}
