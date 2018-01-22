package org.everis.interledger.config;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

public class VertXConfigSupport {
    public static VertxOptions defOptions =  new VertxOptions();
    public static Vertx vertx = null;

    public static Vertx build(VertxOptions options) {
        if (vertx == null) {
            vertx = Vertx.vertx(options);
        }
        return vertx;
    }
    public static Vertx getVertx() {
        if (vertx == null) throw new
            RuntimeException("VertX not yet initialized. Use getVertx(VertxOptions options) first ");
        return vertx;
    }
}
