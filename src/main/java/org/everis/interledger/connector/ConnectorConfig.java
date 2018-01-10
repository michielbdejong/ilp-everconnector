package org.everis.interledger.connector;

import org.everis.interledger.config.plugin.BasePluginConfig;
import org.everis.interledger.config.PropertiesConfig;
import org.everis.interledger.plugins.BasePlugin;
import org.interledger.InterledgerAddress;

import java.util.ArrayList;
import java.util.List;

/**
 * List of peers connectors.
 */
public class ConnectorConfig {
   public final InterledgerAddress ilpAddress;
   public final PropertiesConfig propConfig;
   public final List<BasePlugin> plugins;

   public final RouteTable initialRoutingTable;

   public ConnectorConfig(final String configFile){
      propConfig = new PropertiesConfig(configFile);
      ilpAddress = InterledgerAddress.of(propConfig.getCleanString("connector.ilpAddress"));
      String peerConfigFiles = propConfig.getCleanString("connector.peersConfigFiles");
      String[] peerConfigList = peerConfigFiles.split(";");
      plugins = new ArrayList<BasePlugin>();
      initialRoutingTable = new RouteTable();
      int lastIdxOfDir = configFile.lastIndexOf('/');
      String basePath = configFile.substring(0,lastIdxOfDir);
      for (String peerConfigFile : peerConfigList) {
         BasePluginConfig pluginConfig = BasePluginConfig.build(basePath+"/"+peerConfigFile);
         try {
             BasePlugin plugin = (BasePlugin)pluginConfig.pluginClass
              .getConstructor(pluginConfig.configClass)

              .newInstance(pluginConfig);
             plugins.add(plugin);
             initialRoutingTable.addRoute(new Route(pluginConfig.ledgerPrefix, plugin));
         }catch(Exception e){
             String sError = "While reading plugin config file '"+peerConfigFile+"' next exception was raised:\n"
                   + "Could not configure connector due to:"+e.toString();
             throw new RuntimeException(sError);
         }
      }

   }
}
