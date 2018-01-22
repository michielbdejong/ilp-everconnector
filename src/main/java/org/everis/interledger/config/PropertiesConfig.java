package org.everis.interledger.config;

import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Properties;

/**
 * KISS Config. The idea is to create a class with read-only method that gets
 * configured just upon start up (at Config class loading in the Main method).
 * If something is wrong it just fails to start.
 *
 */
public class PropertiesConfig {
    private final Properties EFFECTIVE_PROPS;
    public final String CONFIG_FILE;

    public PropertiesConfig(String CONFIG_FILE){

        this.CONFIG_FILE = CONFIG_FILE;
        this.EFFECTIVE_PROPS = new Properties();
            try {
        FileInputStream inputStream = new FileInputStream(CONFIG_FILE);
        EFFECTIVE_PROPS.load(inputStream);
        Enumeration<Object> keysEnum = EFFECTIVE_PROPS.keys();
        while (keysEnum.hasMoreElements()) {
            String key = (String) keysEnum.nextElement();
            EFFECTIVE_PROPS.setProperty(key, EFFECTIVE_PROPS.getProperty(key).trim());
        }
            } catch (Exception e) {

        throw new RuntimeException(
                "Can not set-up basePluginConfig for file '" + CONFIG_FILE + "' "
                + "due to " + e +"\n"
                + "Working dir: " +Paths.get(".").toAbsolutePath().
                        normalize().toString() );
            }

    }

    // start utility private internal methods. Notice next methods will fail-fast
    // (https://en.wikipedia.org/wiki/Fail-fast) to avoid un-initialized parameters at runtime.
    public String getString(final String key) {
        final String result = EFFECTIVE_PROPS.getProperty(key);
        if (StringUtils.isEmpty(result)) {
            throw new RuntimeException(key + " was not found in '" + CONFIG_FILE);
        }
        return result;
    }

    public String getCleanString(final String key) {
        final String result = getString(key);
        char[] forbiddenChars = new char[]{'"',' ','\''};
        for (int idx=0; idx<forbiddenChars.length; idx++){
            char forbidden = forbiddenChars[idx];
            if (result.indexOf(forbidden)>=0) {
                throw new RuntimeException(key + " value can not contain '"+forbidden+"' characters.\n" +
                    "The current value of key is:>>"+result+"<<");
            }
        }
        return result;
    }

    public String getEnum(final String key, final String[] allowedEnums){
        final String result = getString(key);
        for (String allowed : allowedEnums) {
            if (allowed.equals(result)) return result;
        }
        String sError = "value for '"+key+"' ("+result+") @"+CONFIG_FILE+" is not amongst list of allowed values :\n";
        for (String allowed : allowedEnums) {
            sError += allowed + ",";
        }
        throw new RuntimeException(sError);
    }

    public boolean getBoolean(final String key) {
        final String auxi = getString(key).toLowerCase();
        if (auxi.equals("false") || auxi.equals("0")) {
            return false;
        }
        if (auxi.equals("true") || auxi.equals("1")) {
            return true;
        }
        throw new RuntimeException(key + " defined in " + CONFIG_FILE
                + " can not be parsed as boolean. Use true|false or 0|1");
    }

    public int getInteger(final String key) {
        final String auxi = getString(key).toLowerCase();
        try {
            return Integer.parseInt(auxi);
        } catch (Exception e) {
            throw new RuntimeException(key + " defined in " + CONFIG_FILE + " can not be parsed as integer");
        }
    }

    public float getFloat(final String key) {
        final String auxi = getString(key).toLowerCase();
        try {
            return Float.parseFloat(auxi);
        } catch (Exception e) {
            throw new RuntimeException(key + " defined in " + CONFIG_FILE + " can not be parsed as float");
        }
    }

}
