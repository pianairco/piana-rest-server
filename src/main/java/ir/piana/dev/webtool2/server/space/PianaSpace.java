package ir.piana.dev.webtool2.server.space;

import ir.piana.dev.webtool2.server.annotation.PianaSpaceProperty;

import java.util.Properties;

public class PianaSpace {
    private static Properties properties = new Properties();

    public static void setProperty(
            PianaSpaceProperty pianaSpaceProperty)
            throws Exception {
        if(properties.contains(pianaSpaceProperty.name()))
            throw new Exception("key " +
                    pianaSpaceProperty.name() +
                    " duplicated.");
        properties.put(pianaSpaceProperty.name(),
                pianaSpaceProperty.value());
    }

    public static String getProperty(
            String name)
            throws Exception {
        return properties.getProperty(name);
    }
}
