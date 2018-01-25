package org.everis.interledger.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorConfigSupport {
    public  static final ExecutorService executor = Executors.newCachedThreadPool();
}
