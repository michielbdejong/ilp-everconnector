package org.everis.interledger.connector;

import org.everis.interledger.org.everis.interledger.common.ILPTransfer;
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
    private static final Duration MIN_MESSAGE_WINDOW = Duration.ofSeconds(10);
    // don't bother the next node with requests that have less than one second left on the clock
    private static final Duration FORWARD_TIMEOUT = MIN_MESSAGE_WINDOW.plus(Duration.ofSeconds(1));

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


    public CompletableFuture<Fulfillment> forwardPayment(ILPTransfer transfer, Object paymentPacket) {
        // final Object payment = IlpPacket.deserializeIlpPayment(paymentPacket)
        CompletableFuture<Fulfillment> result = new CompletableFuture<Fulfillment>();

        if (transfer.getExpiresAt().isAfter(Instant.now().plus(FORWARD_TIMEOUT))) {
            // return Promise.reject(ERROR_LACK_TIME)
            final InterledgerProtocolError ilpError = InterledgerProtocolError.builder()
                .errorCode(InterledgerProtocolError.ErrorCode.R00_TRANSFER_TIMED_OUT)
                .triggeredByAddress(connector.config.ilpAddress)
                .triggeredAt(Instant.now())
                .build();
                result.completeExceptionally(new InterledgerProtocolException(ilpError));
        }

/////   const { onwardAmount, onwardPeer } = this.quoter.findHop(payment.account, parseInt(payment.amount));
/////   // console.log('quote', onwardAmount, onwardPeer)
/////   if (!onwardPeer || !this.peers[onwardPeer]) {
/////     return Promise.reject(ERROR_NO_ROUTE)
/////   }
/////   if (onwardAmount > transfer.amount) {
/////     // console.log('lack source amount', onwardAmount / 1000, transfer.amount / 1000)
/////     return Promise.reject(ERROR_LACK_SOURCE_AMOUNT)
/////   }
/////   // console.log('calling interledgerPayment')
/////   return this.peers[onwardPeer].interledgerPayment({
/////     amount: onwardAmount,
/////     expiresAt: new Date(transfer.expiresAt.getTime() - MIN_MESSAGE_WINDOW),
/////     executionCondition: transfer.executionCondition
/////   }, paymentPacket).then(result => {
/////     // console.log('interledgerPayment result', result)
/////     return result
/////   }, err => {
/////     console.error('interledgerPayment err', err)
/////     throw err
/////   })
        return result;
    }

////forwardRoute (route) {
////  for (let name in this.peers) {
////    if (name.startsWith('peer_')) { // only forward over BTP peers, not virtual peers (ledger plugins)
////      this.peers[name].announceRoute(route)
////    }
////  }
////}
}
