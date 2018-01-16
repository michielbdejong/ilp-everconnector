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

    private static String hostPort2Key(String host, String port) {
        return host+":"+port;

    }

    public static void registerPlugin(final String host, final String port, final BasePlugin plugin) {
        final String key = hostPort2Key(host,port);
        if (host2Plugin.containsKey(key)) {
            throw new RuntimeException(key +"already registered");
        }
        host2Plugin.put(key, plugin);
    }

 // public static void registerConnectorPlugins(List<BasePlugin> plugin_list) {
 //     for (BasePlugin plugin : plugin_list) {
 //         registerPlugin(
 //             plugin.basePluginConfig.listeningHostOrIP,
 //             plugin.basePluginConfig.listeningPort,
 //             plugin);
 //     }
 // }

    public static BasePlugin getInstance(final String host, final String port) {
        final String key = hostPort2Key(host,port);
        if (!host2Plugin.containsKey(key)) {
            throw new RuntimeException("No plugin instance listenint at "+key);
        }
        return host2Plugin.get(key);
    }
}
