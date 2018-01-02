package org.everis.interledger.connector;

import org.everis.interledger.org.everis.interledger.common.ILPTransfer;
import org.everis.interledger.plugins.Plugin;
import org.interledger.InterledgerAddress;
import org.interledger.cryptoconditions.Fulfillment;


public class SimpleConnector {
    /*private Plugin pluginSourceLedger;
    private Plugin pluginDestinationLedger;


    public SimpleConnector() {
    }

    public void setPluginSourceLedger(Plugin pluginSourceLedger) {
        this.pluginSourceLedger = pluginSourceLedger;
    }

    public void setPluginDestinationLedger(Plugin pluginDestinationLedger) {
        this.pluginDestinationLedger = pluginDestinationLedger;
    }

    public void notifyPreparePayment(ILPTransfer ilpTransfer) {
        InterledgerAddress destinationPrefix = ilpTransfer.getDestinationAccount().getPrefix();
        ILPTransfer ilpConnectorFrwdTsf = new ILPTransfer(ilpTransfer.getUUID(),
                InterledgerAddress.of(destinationPrefix + "connectorEveris"),
                ilpTransfer.getDestinationAccount(), ilpTransfer.getAmount(), ilpTransfer.getPayment(),
                ilpTransfer.getCondition());

        this.pluginDestinationLedger.prepareTransfer(ilpConnectorFrwdTsf);
    }

    public void notifyFulfillment(ILPTransfer transfer, Fulfillment fulfillment) {
        this.pluginSourceLedger.fulfillCondition(transfer.getUUID(), fulfillment);
    }

    public void notifyReject(ILPTransfer transfer) {
        this.pluginSourceLedger.rejectTransfer(transfer.getUUID());
    }*/
}
