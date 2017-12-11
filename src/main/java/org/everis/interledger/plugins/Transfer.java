package org.everis.interledger.plugins;

import java.security.Timestamp;
import org.interledger.InterledgerAddress;
import org.interledger.cryptoconditions.Condition;
import org.interledger.cryptoconditions.Fulfillment;
import org.interledger.ilp.InterledgerPayment;

public class Transfer {

  public enum TransferStatus {
    PREPARED,
    EXECUTED,
    REJECTED
  }

  private static int generator = 0;
  private int id;

  private InterledgerAddress destinationAccount;
  private InterledgerAddress sourceAccount;
  private Integer amount;

  private Timestamp expiration;
  private TransferStatus status;

  private InterledgerPayment payment;

  private Condition condition;
  private Fulfillment fulfillment;

  public Transfer(
      InterledgerAddress destinationAccount,
      InterledgerAddress sourceAccount,
      Integer amountValue,
      InterledgerPayment payment) {
    this.id = generator;
    generator = generator + 1;

    this.destinationAccount = destinationAccount;
    this.sourceAccount = sourceAccount;

    this.status = TransferStatus.PREPARED;
    this.amount = amountValue;
    this.payment = payment;
  }

  public Integer getId() {
      return id;
  }

  public InterledgerAddress getDestinationAccount() {
    return destinationAccount;
  }

  public InterledgerAddress getSourceAccount() {
    return sourceAccount;
  }

  public InterledgerPayment getPayment() {
    return payment;
  }

  public void setPayment(InterledgerPayment paymentValue) {
    this.payment = paymentValue;
  }

    public TransferStatus getStatus() {
        return status;
    }

    public void setStatus(TransferStatus status) {
        this.status = status;
    }

    public Integer getAmount() {
        return amount;
    }

    public Condition getCondition() {
    return condition;
  }

  public void setCondition(Condition conditionValue) {
    this.condition = conditionValue;
  }

  public Fulfillment getFulfillment() {
    return fulfillment;
  }

  public void setFulfillment(Fulfillment fulfillmentValue) {
    this.fulfillment = fulfillmentValue;
  }
}
