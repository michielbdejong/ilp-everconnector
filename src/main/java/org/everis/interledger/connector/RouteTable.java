package org.everis.interledger.connector;

import org.interledger.InterledgerAddress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouteTable {
    // For now (2018-01-17) The routes are hardcoded at config.
    private final Map<InterledgerAddress, Route> prefixToRoute;
    private final Route defaultRoute;

    public RouteTable(Map<InterledgerAddress, Route> prefixToRouteFromConfig, Route defaultRoute){
        this.prefixToRoute = prefixToRouteFromConfig;
        this.defaultRoute = defaultRoute;
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
