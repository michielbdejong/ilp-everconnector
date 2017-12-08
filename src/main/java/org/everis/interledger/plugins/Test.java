package org.everis.interledger.plugins;

import org.interledger.InterledgerAddress;

public class Test {
  public static void main(String [] args) {
    try {
      Transfer t1 = new Transfer(InterledgerAddress.of("g.everis"), InterledgerAddress.of("g.everis"));
      Transfer t2 = new Transfer(InterledgerAddress.of("g.everis"), InterledgerAddress.of("g.everis"));
      Transfer t3 = new Transfer(InterledgerAddress.of("g.everis"), InterledgerAddress.of("g.everis"));

      System.out.println(t1.getId() + " " + t2.getId() + " " + t3.getId() + " ");


    } catch (Exception e) {
      System.out.println("Something happens " + e.getMessage());
    }
  }
}
