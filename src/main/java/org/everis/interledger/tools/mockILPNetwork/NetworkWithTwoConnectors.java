package org.everis.interledger.tools.mockILPNetwork;

import io.vertx.core.VertxOptions;
import org.everis.interledger.config.VertXConfigSupport;
import org.everis.interledger.connector.ConnectorConfig;
import org.everis.interledger.connector.GenericConnector;
import org.everis.interledger.plugins.BasePlugin;
import org.interledger.InterledgerAddress;
import org.interledger.InterledgerPacketType;
import org.interledger.InterledgerProtocolException;
import org.interledger.ilp.InterledgerProtocolError;

import java.nio.Buffer;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class NetworkWithTwoConnectors {
    // TODO:(0) Instantiate 2 connectors with 2 settlement ledgers and 1 Payment channel
    /*
     *  Settlement ledger 1 <-> Connector 1 <- Payment Channel -> Connector 2 <-> Settlement Ledger 2
     *
     */
    private static final String pathToConfig = "ILP-Plugin/config/dev_network/two_connectors";



    public static void main(String[] args) {
        VertXConfigSupport.build(new VertxOptions()); // Init Vertx
        // First connector just "forwards" payments. No custom handler attached
        ConnectorConfig config1 = new ConnectorConfig(pathToConfig+"/connector1.prop", Optional.empty());

        // The second connector is "connected" with the webshop
        ConnectorConfig config2 = new ConnectorConfig(pathToConfig+"/connector2.prop", Optional.of(new MockWebShop.RequestHandlerWebShop()));
        GenericConnector connector1 = GenericConnector.build(config1);
        GenericConnector connector2 = GenericConnector.build(config2);

        connector2.run();
        connector1.run();

    }
}
