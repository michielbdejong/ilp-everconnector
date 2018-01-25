package org.everis.interledger.connector;

import org.everis.interledger.config.ExecutorConfigSupport;
import org.everis.interledger.org.everis.interledger.common.ILPTransfer;
import org.everis.interledger.plugins.BasePlugin;
import org.interledger.InterledgerProtocolException;
import org.interledger.ilp.InterledgerProtocolError;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;

/**
 * Class in charge of forwarding transfers not managed by the local connectors
 */
public class Forwarder {
    // TODO:(1) Make those parameters configurable?
    // Never set this to less than 1 second, especially not during a leap second
    private static final Duration MIN_MESSAGE_WINDOW = Duration.ofSeconds(1);
    // make sure you always have 10 seconds to fulfill after your peer fulfilled.
    private static final Duration FORWARD_TIMEOUT = Duration.ofSeconds(10);

    private static final int ERROR_LACK_SOURCE_AMOUNT = 2;
    private static final int ERROR_NO_ROUTE = 3;

    final Object quoter;
    final SimpleConnector connector;
    final RouteTable routeTable;


    public Forwarder(final SimpleConnector connector, RouteTable routeTable, final Object quoter) {
        this.connector = connector;
        this.routeTable = routeTable;
        this.quoter = quoter;
    }

    /*
    105   findHop (address, amount) {
    106     const curve = this.findCurve(address)
    107     return {
    108       onwardAmount: destToSource(amount, curve.buf),
    109       onwardPeer: curve.peer
    110     }
    111   },
     */
    public void forwardPayment(ILPTransfer ilpTransfer, CompletableFuture<BasePlugin.DataResponse> result) {

        Route route = routeTable.findRouteByAddress(ilpTransfer.destinationAccount);
        if (route == RouteTable.SELF) {
           final InterledgerProtocolError ilpError = InterledgerProtocolError.builder()
                .errorCode(InterledgerProtocolError.ErrorCode.T01_LEDGER_UNREACHABLE)
                .triggeredByAddress(connector.config.ilpAddress)
                .triggeredAt(Instant.now())
                .build();
                result.completeExceptionally(new InterledgerProtocolException(ilpError));
        }
        /* TODO:(0):FIXME: onwardAmount: apply input/output currency conversion and connector comission/toll
         *  Use info in Route instance (liquidity curve, minimumAllowedAmmount, ...) to calculate it.
         *  NOTE: input amount will never be equal to onwardAmount unless currency is the same and toll is ZERO
         *  */
        final String onwardAmount = ilpTransfer.amount;

        // TODO:(0): CHECK 2:  Check that (onwardAmount > transfer.amount) or cancel

        // Forward payment with new amount
        ILPTransfer newILPTransfer = new ILPTransfer(
        "",
        ilpTransfer.destinationAccount,
        onwardAmount,
        ilpTransfer.expiresAt.minus(MIN_MESSAGE_WINDOW) /* decrease timeout */,
        ilpTransfer.condition,
        ilpTransfer.endToEndData );
        System.out.println("forwarding "+newILPTransfer.amount + "to "+ newILPTransfer.destinationAccount + " through "+route.plugin.getConfigFile());
        route.plugin.sendData(newILPTransfer, result);
        ExecutorConfigSupport.executor.submit(() -> {
            try {
                BasePlugin.DataResponse response = result.get(FORWARD_TIMEOUT.getSeconds(), TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // TODO:(0.5) Retry
                System.out.println("InterruptedException:"+e.toString());
            } catch (ExecutionException e) {
                result.completeExceptionally(e.getCause());
            } catch (TimeoutException e) {
                result.completeExceptionally(
                    new RuntimeException("Timeout waiting for peer response to '"+route.addressPrefix+"' route"));
            }
        });

    }
}
