package org.everis.interledger.connector;

import org.everis.interledger.config.plugin.BasePluginConfig;
import org.everis.interledger.config.PropertiesConfig;
import org.everis.interledger.plugins.BasePlugin;
import org.interledger.InterledgerAddress;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * List of peers connectors.
 */
public class ConnectorConfig {
    public final InterledgerAddress ilpAddress;
    private final PropertiesConfig propConfig;
    public final List<BasePlugin> plugins;
    public final RouteTable initialRoutingTable;
    public final String tls_key_path;
    public final String tls_crt_path;

    public ConnectorConfig(final String configFile, BasePlugin.IRequestHandler requestHandler ){

       propConfig = new PropertiesConfig(configFile);
       ilpAddress = InterledgerAddress.of(propConfig.getCleanString("connector.ilpAddress"));
       String peerConfigFiles = propConfig.getCleanString("connector.peersConfigFiles");
       String[] peerConfigList = peerConfigFiles.split(";");
       tls_key_path = propConfig.getCleanString("connector.tls_key_path");
       tls_crt_path = propConfig.getCleanString("connector.tls_crt_path");
       plugins = new ArrayList<>();
       RouteTable.RouteTableBuilder rtBuilder = RouteTable.builder();

       int lastIdxOfDir = configFile.lastIndexOf('/');
       String basePath = configFile.substring(0,lastIdxOfDir);
       for (String peerConfigFile : peerConfigList) {
           String aux = basePath+"/"+peerConfigFile;
           System.out.println("loading peer Config File: "+aux+"'");
           BasePluginConfig pluginConfig = BasePluginConfig.build(aux);
           try {
               BasePlugin plugin = (BasePlugin)pluginConfig.pluginClass
                .getConstructor(pluginConfig.configClass, BasePlugin.IRequestHandler.class)
                .newInstance(pluginConfig, requestHandler);
               plugins.add(plugin);
               rtBuilder.addRoute(new Route(pluginConfig.ledgerPrefix, plugin, pluginConfig.liquidityCurve));
           }catch(Exception e){
               StringWriter writer = new StringWriter();
               PrintWriter printWriter = new PrintWriter( writer );
               e.printStackTrace( printWriter );
               printWriter.flush();
               String stackTrace = writer.toString();
               String sError = "While reading plugin basePluginConfig file '"+peerConfigFile+"' next exception was raised:\n"
                     + "Could not configure connector due to:"+e.toString() +"\n"
                     + stackTrace ;

               throw new RuntimeException(sError);
           }
      }
      rtBuilder.setDefaultRoute(InterledgerAddress.of(propConfig.getString("default_route.prefix")));
      this.initialRoutingTable = rtBuilder.build();

   }
}
