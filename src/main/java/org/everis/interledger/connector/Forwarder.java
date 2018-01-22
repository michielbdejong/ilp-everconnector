package org.everis.interledger.connector;

import org.everis.interledger.org.everis.interledger.common.ILPTransfer;
import org.everis.interledger.plugins.BasePlugin;
import org.interledger.InterledgerProtocolException;
import org.interledger.cryptoconditions.Fulfillment;
import org.interledger.ilp.InterledgerProtocolError;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

/**
 * Class in charge of forwarding transfers not managed by the local connectors
 */
public class Forwarder {
    // make sure you always have 10 seconds to fulfill after your peer fulfilled.
    // Never set this to less than 1 second, especially not during a leap second
    // TODO:(1) Make this configurable
    private static final Duration MIN_MESSAGE_WINDOW = Duration.ofSeconds(1);
    // don't bother the next node with requests that have less than one second left on the clock
    private static final Duration FORWARD_TIMEOUT = Duration.ofSeconds(10);

    private static final int ERROR_LACK_SOURCE_AMOUNT = 2;
    private static final int ERROR_NO_ROUTE = 3;

    final Object quoter;
    final GenericConnector connector;
    final RouteTable routeTable;


    public Forwarder(final GenericConnector connector, RouteTable routeTable, final Object quoter) {
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
    public CompletableFuture<BasePlugin.DataResponse> forwardPayment(ILPTransfer ilpTransfer) {
        if (ilpTransfer.expiresAt.isAfter(Instant.now().plus(FORWARD_TIMEOUT))) {
            CompletableFuture<BasePlugin.DataResponse> result = new CompletableFuture<>();
            // return Promise.reject(ERROR_LACK_TIME)
            final InterledgerProtocolError ilpError = InterledgerProtocolError.builder()
                .errorCode(InterledgerProtocolError.ErrorCode.R00_TRANSFER_TIMED_OUT)
                .triggeredByAddress(connector.config.ilpAddress)
                .triggeredAt(Instant.now())
                .build();
                result.completeExceptionally(new InterledgerProtocolException(ilpError));
                return result;
        } else {
            Route route = routeTable.findRouteByAddress(ilpTransfer.destinationAccount);
            ILPTransfer newILPTransfer = new ILPTransfer(
            "",
            ilpTransfer.destinationAccount,
            ilpTransfer.amount /* TODO:(0) apply currency conversion and commision */,
            ilpTransfer.expiresAt.minus(MIN_MESSAGE_WINDOW) /* decrease timeout */,
            ilpTransfer.condition,
            ilpTransfer.endToEndData );
            // TODO:(0) Apply toll (money kept by our connector).
            return route.plugin.sendData(newILPTransfer);
        }
        // TODO:(0) Check that (onwardAmount > transfer.amount)  or cancel
    }
}
