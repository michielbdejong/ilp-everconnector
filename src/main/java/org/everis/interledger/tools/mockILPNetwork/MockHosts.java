package org.everis.interledger.tools.mockILPNetwork;

import org.everis.interledger.plugins.BasePlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class simulating connectors running on the network.
 * Each plugin is supposed to listen for incomming messagen on a host and port
 */
public class MockHosts {
    static Map<String /*host:port*/, BasePlugin> host2Plugin = new HashMap<String, BasePlugin>();

    private static void registerPlugin(final String host, final String port, final BasePlugin plugin) {
        final String host_port = host+":"+port;
        if (host2Plugin.containsKey(host_port)) {
            throw new RuntimeException(host_port +"already registered");
        }
    }

    public static void registerConnectorPlugins(List<BasePlugin> plugin_list) {
        for (BasePlugin plugin : plugin_list) {
            registerPlugin(
                plugin.config.listeningHostOrIP,
                plugin.config.listeningPort,
                plugin);
        }
    }

    public static void getInstance(final String host, final String port, final BasePlugin plugin) {

    }
}
