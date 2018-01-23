package org.everis.interledger.tools.mockILPNetwork;

import io.vertx.core.VertxOptions;
import org.everis.interledger.config.VertXConfigSupport;
import org.everis.interledger.connector.ConnectorConfig;
import org.everis.interledger.connector.SimpleConnector;

import java.util.Optional;

public class NetworkWithTwoConnectors {
    // TODO:(0) Instantiate 2 connectors with 2 settlement ledgers and 1 Payment channel
    /*
     *  Settlement ledger 1 <-> Connector 1 <- Payment Channel -> Connector 2 <-> Settlement Ledger 2
     *
     */
    private static final String pathToConfig = "config/dev_network/two_connectors";



    public static void main(String[] args) {
        VertXConfigSupport.build(new VertxOptions()); // Init Vertx
        // First connector just "forwards" payments. No custom handler attached
        ConnectorConfig config1 = new ConnectorConfig(pathToConfig+"/connector1.prop", Optional.empty());

        // The second connector is "connected" with the webshop
        ConnectorConfig config2 = new ConnectorConfig(pathToConfig+"/connector2.prop", Optional.of(new MockWebShop.RequestHandlerWebShop()));
        SimpleConnector connector1 = SimpleConnector.build(config1);
        SimpleConnector connector2 = SimpleConnector.build(config2);

        connector2.run();
        connector1.run();

    }
}
