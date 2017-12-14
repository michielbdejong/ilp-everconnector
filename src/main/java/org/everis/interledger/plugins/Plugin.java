package org.everis.interledger.plugins;

import org.everis.interledger.org.everis.interledger.common.LedgerInfo;
import org.everis.interledger.tools.mockLedger.LocalAccount;
import org.everis.interledger.tools.mockLedger.MockInMemoryLedger;
import org.interledger.InterledgerAddress;
import org.interledger.cryptoconditions.Fulfillment;

import javax.money.Monetary;
import java.util.Locale;

/**
 * Entity of Plugin to connect any sender, receiver or connector with a ledger.
 */
public class Plugin {

    private MockInMemoryLedger ledger;
    private LedgerInfo ledgerInfo;

    private LedgerConnection ledgerConnection;

    /**
     * Keeps credentials and any other usefull info to help connect to the ledger
     * This info will vary for each different ledger
     * This class is used as input to the connect phase.
     * It represents data needed to find the ledger on the network, authenticate to the ledger,...
     *
     * Once connected info about the remote ledger can be fetched throught LedgerInfo (connected_)ledgder.getInfo();
     */
    class LedgerConnection {
        final String host; // not used, just left as example data
        final String port; // not used, just left as example data
        final String accound_id;
        final String pass;
        final InterledgerAddress connectorAddress;

        public LedgerConnection(String account_id, String pass, InterledgerAddress connectorAddress){
           this.accound_id = account_id;
           this.pass = pass;
           this.host = "mockHost";
           this.port = "mockPort";
           this.connectorAddress = connectorAddress;
        }
    }

    /**
     * Connector with a PluginConnection object.
     * @param ledgerConnection
     */
    public Plugin(LedgerConnection ledgerConnection) {
        this.ledgerConnection = ledgerConnection;
    }


    /**
     * This static class simulates a network connection to the ledger
     *
     * In this case we just instantiate a new in-memory-ledger
     * @param host
     * @param port
     * @param connector_account_on_ledger
     * @param connector_password_on_ledger
     * @return
     */
    public static MockInMemoryLedger mockNetworkConnect(String host, String port, InterledgerAddress nodeAddress, String connector_account_on_ledger, String connector_password_on_ledger) {
        MockInMemoryLedger mockLedger = new MockInMemoryLedger(InterledgerAddress.of("test1.pound"), Monetary.getCurrency(Locale.UK));
        if (true)
        {
            // More mock code. Let's create a new local account with the plugin provided credentials
            // =>  That means connection will always success. In a real scenario the connector account must have
            // been added previously to the ledger, and the connection can success or fail if the plugin provided credentials
            // are OK or KO.
            LocalAccount newConnectorAccount = new LocalAccount(connector_account_on_ledger, connector_password_on_ledger, 1000000);
            mockLedger.addAccount(newConnectorAccount);
        }
        // Next line will fail if credentials do not match with registered credentials in "remote" ledger
        mockLedger.onConnectRequest(nodeAddress, connector_account_on_ledger, connector_password_on_ledger);
        return  mockLedger;
    }

    /**
     * connect the plugin with the ledger in parameter.
     */
    public void connect() {
        this.ledger = mockNetworkConnect(
                ledgerConnection.host,
                ledgerConnection.host,
                ledgerConnection.connectorAddress,
                ledgerConnection.accound_id,
                ledgerConnection.pass);

        this.ledgerInfo = ledger.getInfo();
    }

    /**
     * disconnect the plugin from any ledger connected.
     */
    public void disconnect() {
        this.ledger.disconnect(this.ledgerConnection.connectorAddress);
        this.ledger = null;
        this.ledgerInfo = null;
    }

    /**
     * verify that the plugin is right connected to a ledger.
     * @return boolean
     */
    public boolean isConnected() {
        return this.ledger !=null && this.ledger.isPluginConnected(this.ledgerConnection.connectorAddress);
    }

    /**
     * call the prepare transfer method on the ledger.
     * @param newTransfer
     */
    public void sendTransfer(ILPTransfer newTransfer) {
        this.ledger.prepareTransaction(newTransfer);
    }

    /**
     * call the fulfill condition method on the ledger.
     * @param transferId
     * @param fulfillment
     */
    public void fulfillCondition(int transferId, Fulfillment fulfillment) {
        this.ledger.fulfillCondition(transferId, fulfillment);
    }

    /**
     * call the reject transfer method on the ledger.
     * @param transferId
     */
    public void rejectTransfer(int transferId) {
        this.ledger.rejectTransfer(transferId);
    }
    
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("-PLUGIN-ACCOUNTS---------");
        str.append("\n-------------------------");
        str.append("\n" +  this.ledger == null ? "-NO-LEDGER------------" : this.ledgerInfo);
        str.append("\nLinked Account " + this.ledgerConnection.connectorAddress);
        str.append("\n-------------------------");
        return str.toString();
    }
}
