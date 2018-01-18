package org.everis.interledger.tools.mockILPNetwork;

import org.everis.interledger.connector.ConnectorConfig;
import org.everis.interledger.connector.GenericConnector;
import org.everis.interledger.plugins.BasePlugin;
import org.interledger.InterledgerAddress;
import org.interledger.InterledgerPacketType;
import org.interledger.InterledgerProtocolException;

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
    private static final String pathToConfig = "ILP-Plugin/basePluginConfig/dev_network/two_connectors";

    public static void main(String[] args) {
       /*
           public interface IRequestHandler {

        class ILPResponse {
            // TODO:(Quilt) There is no concept of type in Quilt org.interledger.InterledgerPacket
            public final int packetType;
            public final Optional<InterledgerProtocolException> optILPException;
            public final Optional<String> optBase64Fulfillment;

            ILPResponse(
                int packetType,
                Optional<InterledgerProtocolException> optILPException,
                Optional<String> optBase64Fulfillment )
            {
                if (packetType == InterledgerPacketType.INTERLEDGER_PROTOCOL_ERROR &&
                    ! optILPException.isPresent()) {
                    throw new RuntimeException("packetType equals ILP error but optILPException is not present");
                }
                if (packetType == InterledgerPacketType.ILP_PAYMENT_TYPE &&
                    !optBase64Fulfillment.isPresent() ) {
                }
                this.packetType = packetType;
                this.optILPException = optILPException;
                this.optBase64Fulfillment = optBase64Fulfillment;
            }
        }
	}
        */
        class RequestHandler implements BasePlugin.IRequestHandler {
             public CompletableFuture<ILPResponse> onRequestReceived(
    	        InterledgerAddress destination,
                String Base64ExecCondition,
                Instant expiresAt,
                String amount,
                Buffer endToEndData
                ) {
                  CompletableFuture<ILPResponse> result = new CompletableFuture<ILPResponse>();
                  result.complete(new ILPResponse(
                      InterledgerPacketType.ILP_PAYMENT_TYPE,
                      Optional.empty(),
                      Optional.of("TODO:(0)optBase64Fulfillment")  ));

                  return result;
             }
        }
        RequestHandler myRequestHandler = new RequestHandler();
        ConnectorConfig config1 = new ConnectorConfig(pathToConfig+"/connector1.prop", myRequestHandler);
        ConnectorConfig config2 = new ConnectorConfig(pathToConfig+"/connector2.prop", myRequestHandler);
        GenericConnector connector1 = GenericConnector.build(config1);
        GenericConnector connector2 = GenericConnector.build(config2);

        connector2.run();
        connector1.run();
    }
}
