package org.everis.interledger.connector;

import org.interledger.InterledgerAddress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouteTable {

    static class RouteTableBuilder {
        final RouteTable routeTable = new RouteTable();

        public void addRoute(Route route) {
            routeTable.addRoute(route);
        }

        public void setDefaultRoute(InterledgerAddress prefix) {
            routeTable.setDefaultRoute(prefix);
        }

        public RouteTable build(){
            if (routeTable.defaultRoute == null) {
                throw new RuntimeException("you must call setDefaultRoute on RouteTable builder first");
            }
            return routeTable;
        }

    }

    // For now (2018-01-17) The routes are hardcoded at config.
    private Map<InterledgerAddress, Route> prefixToRoute = new HashMap<InterledgerAddress, Route>();
    private Route defaultRoute = null;

    private RouteTable(){ }

    public static RouteTableBuilder builder(){
        return new RouteTableBuilder();
    }

    public void setDefaultRoute(InterledgerAddress prefix){
        Route aux = prefixToRoute.get(prefix);
        if (aux == null) {
            String sError = "No plugin has a matching prefix for default route\n"
                + "default routes are used to indicate the gateway for unkown routes"
                + "Hint: If default_route.prefix=peer.connector2. @ connector1.prop and\n"
                + "connector.peersConfigFiles=connector1_to_connector2_plugin.prop,....\n "
                + " then some of the peersConfigFiles must have a  plugin.ledgerPrefix= matching "
                +   "this default route:\n"
                + "plugin.ledgerPrefix=peer.connector2. \n"
                + "Then the connector will now what plugin to use to forward default route payments";
            throw new RuntimeException(sError);
        }
        this.defaultRoute = aux;
    }

    public void addRoute(final Route route) {
        this.prefixToRoute.put(route.addressPrefix, route);
    }

    public Route findRouteByAddress(InterledgerAddress address) {
        /*
         * If an address is similar to g.part1.part2.part3.part4
         * let's try to find a matching route in order:
         *  g.part1.part2.part3.part4
         *  g.part1.part2.part3.
         *  g.part1.part2.
         *  g.part1.
         *  g.
         */
        String[] part_list = address.getValue().split(".");
        for (int numParts=part_list.length; numParts>0; numParts--){
            StringBuffer prefix = new StringBuffer(part_list[0]);
            for (int idx=1; idx < numParts-1; idx++) {
                prefix.append(part_list[idx]);
            }
            prefix.append('.');

            Route route = this.prefixToRoute.get(prefix);
            if (route != null) {
                return  route;
            }
        }
        return defaultRoute;

    }

}
