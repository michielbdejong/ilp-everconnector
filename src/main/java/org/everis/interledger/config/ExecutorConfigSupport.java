package org.everis.interledger.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorConfigSupport {
    public  static final ExecutorService executor =
        Executors.newFixedThreadPool(8 /* TODO:(1) Arbitrary max. numbers of parallel trheads*/);


}
