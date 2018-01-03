package org.everis.interledger.plugins;

import org.interledger.InterledgerAddress;

/**
 * Keeps credentials and any other usefull info to help connect to the ledger
 * This info will vary for each different ledger
 * This class is used as input to the connect phase.
 * It represents data needed to find the ledger on the network, authenticate to the ledger,...
 *
 * Once connected info about the remote ledger can be fetched throught LedgerInfo (connected_)ledgder.getInfo();
 */
public class LedgerConnection {
    private final String host; // not used, just left as example data
    private final String port; // not used, just left as example data
    private final String accound_id;
    private final String pass;
    private final InterledgerAddress connectorAddress;

    public LedgerConnection(String account_id, String pass, InterledgerAddress connectorAddress){
       this.accound_id = account_id;
       this.pass = pass;
       this.host = "mockHost";
       this.port = "mockPort";
       this.connectorAddress = connectorAddress;
    }

    public String getAccound_id() {
        return accound_id;
    }

    public String getPass() {
        return pass;
    }

    public InterledgerAddress getConnectorAddress() {
        return connectorAddress;
    }
}