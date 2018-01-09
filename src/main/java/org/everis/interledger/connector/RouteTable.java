package org.everis.interledger.connector;

import java.util.ArrayList;
import java.util.List;

public class RouteTable {
    public final List<Route> table;

    public RouteTable(){
        this.table = new ArrayList<Route>();
    }

    public void addRoute(final Route route) {
        this.table.add(route);
    }

}
