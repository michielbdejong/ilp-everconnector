package org.everis.interledger.tools.mockILPNetwork;

import org.everis.interledger.connector.ConnectorConfig;
import org.everis.interledger.connector.GenericConnector;

public class NetworkWithTwoConnectors {
    // TODO:(0) Instantiate 2 connectors with 2 settlement ledgers and 1 Payment channel
    /*
     *  Settlement ledger 1 <-> Connector 1 <- Payment Channel -> Connector 2 <-> Settlement Ledger 2
     *
     */
    private static final String pathToConfig = "ILP-Plugin/basePluginConfig/dev_network/two_connectors";

    public static void main(String[] args) {
        ConnectorConfig config1 = new ConnectorConfig(pathToConfig+"/connector1.prop");
        ConnectorConfig config2 = new ConnectorConfig(pathToConfig+"/connector2.prop");
        GenericConnector connector1 = GenericConnector.build(config1);
        GenericConnector connector2 = GenericConnector.build(config2);

        connector2.run();
        connector1.run();
    }
}
