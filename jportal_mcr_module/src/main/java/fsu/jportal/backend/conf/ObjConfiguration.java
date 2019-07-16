package fsu.jportal.backend.conf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

public class ObjConfiguration {
    Logger LOGGER = LogManager.getLogger();

    private final String objID;

    private ObjConfiguration(String objID) {
        this.objID = objID;
    }

    public static ObjConfiguration of(String objID) {
        return new ObjConfiguration(objID);
    }

    public Optional<String> get(String type, String key) {
        return getProperties(type)
                .map(p -> p.getProperty(key));
    }

    public Optional<Properties> getProperties(String type) {
        return JPObjectConfigurationPaths.getObjProperties(objID, type)
                .filter(Files::exists)
                .map(Path::toFile)
                .map(ObjConfiguration::loadProperties);
    }

    public Optional<Properties> addProperties(Map prosMap, String type) {
        Optional<Path> objProperties = JPObjectConfigurationPaths.getObjProperties(objID, type);
        Properties properties = objProperties
                .filter(Files::exists)
                .map(Path::toFile)
                .map(ObjConfiguration::loadProperties)
                .orElseGet(Properties::new);

        properties.putAll(prosMap);

        File propsFile = objProperties.map(Path::toFile).get();

        try {
            boolean created = propsFile.createNewFile();

            if(created){
                LOGGER.info("Created property file " + propsFile.getAbsolutePath());
            }

            properties.store(new FileOutputStream(propsFile), new Date().toString());

            return Optional.of(properties);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    private static Optional<File> createIfNotExists(Path path) {

        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                e.printStackTrace();
                return Optional.empty();
            }
        }

        return Optional.of(path.toFile());
    }

    private static Properties loadProperties(File propsFile) {
        Properties objProperties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream(propsFile)) {
            objProperties.load(fileInputStream);
            return objProperties;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return objProperties;
    }

    public Stream<Path> list() {
        return JPObjectConfigurationPaths.getConfPath(objID)
                .filter(Files::exists)
                .map(ObjConfiguration::walk)
                .orElse(Stream.empty());
    }

    private static Stream<Path> walk(Path path) {
        try {
            return Files.list(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Stream.empty();
    }
}
