package fsu.jportal.backend;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fsu.jportal.resolver.JournalFilesResolver;

/**
 * Java abstraction of an object configuration. Each mycore object can have additional
 * configurations stored on the file system. The default store location is:
 * data/journalFiles/{id}/{type}.properties
 * 
 * @author Matthias Eichner
 * @author Huu Chi Vu
 */
public class JPObjectConfiguration {

    private Properties properties;

    private String configFilePath;

    /**
     * Loads the {type} configuration for the given object.
     * 
     * @param objID the object to load
     * @param type the type to load
     * @throws IOException something went wrong due loading
     */
    public JPObjectConfiguration(String objID, String type) throws IOException {
        properties = new Properties();
        // build folder
        String configFolder = objID + "/conf/";
        Path configFolderPath = JournalFilesResolver.getPath(configFolder);
        if (!Files.exists(configFolderPath)) {
            Files.createDirectories(configFolderPath);
        }
        configFilePath = configFolder + type + ".properties";
        load();
    }

    /**
     * Returns the value for the given key or an empty string
     * if nothing is found.
     * 
     * @param key the property key
     * @return the value of the property
     */
    public String get(String key) {
        return get(key, "");
    }

    /**
     * Returns the value for the given key or the default value
     * if nothing is found.
     * 
     * @param key the property key
     * @param defaultValue the value if nothing is found
     * @return the value of the property
     */
    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public void set(String key, String value) {
        properties.setProperty(key, value);
    }

    public void remove(String key) {
        properties.remove(key);
    }

    /**
     * Returns a map of all properties starting with the filter.
     * 
     * @param filter the filter to apply
     * @return a map of key value pairs
     */
    public Map<String, String> keyFilter(String filter) {
        Map<String, String> map = new HashMap<>();
        filter(filter).forEach(entry -> map.put(entry.getKey().toString(), entry.getValue().toString()));
        return map;
    }

    /**
     * Removes all properties where the filter matches the key's.
     * 
     * @param filter the filter to apply
     */
    public void removeByKeyFilter(String filter) {
        filter(filter).map(Entry::getKey).collect(Collectors.toList()).forEach(key -> {
            properties.remove(key);
        });
    }

    private Stream<Entry<Object, Object>> filter(String filter) {
        return properties.entrySet().stream().filter(entry -> {
            return entry.getKey().toString().startsWith(filter);
        });
    }

    public void store() throws IOException {
        Path path = JournalFilesResolver.getPath(configFilePath);
        properties.store(new FileOutputStream(path.toFile()), null);
    }

    private void load() throws IOException {
        FileInputStream journalConfig = JournalFilesResolver.stream(configFilePath);
        if (journalConfig != null) {
            properties.load(journalConfig);
        }
    }

}