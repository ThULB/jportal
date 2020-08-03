package fsu.jportal.backend.mcr;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.config.MCRConfigurationBase;

/**
 * Created by chi on 30.07.20
 *
 * @author Huu Chi Vu
 */
public class JPConfig {
    public static Optional<String> getString(String propertyName) {
        return MCRConfiguration2.getString(propertyName);
    }

    public static String getStringOrThrow(String propertyName) {
        return MCRConfiguration2.getStringOrThrow(propertyName);
    }

    public static List<String> getStrings(String name) {
        return MCRConfigurationBase.getString(name)
                .map(MCRConfiguration2::splitValue)
                .orElseThrow(() -> MCRConfiguration2.createConfigurationException(name))
                .collect(Collectors.toList());
    }

    public static String getString(String name, String defaultValue) {
        return MCRConfiguration2.getString(name).orElse(defaultValue);
    }

    public static void initialize(Map<String, String> props, boolean clear) {
        MCRConfigurationBase.initialize(props, clear);
    }

    public static void set(String propName, String value) {
        MCRConfiguration2.set(propName, value);
    }
}
