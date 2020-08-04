package fsu.jportal.backend.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fsu.jportal.domain.model.JPProperties;

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

    public Optional<JPProperties> loadJPProperties(String propType) {
        return JPObjectConfigurationPaths.getObjProperties(objID, propType, "json")
                .filter(Files::exists)
                .map(this::unmarshallJPProperties);
    }

    private JPProperties unmarshallJPProperties(Path path) {
        try {
            InputStream propIS = Files.newInputStream(path);
            JAXBContext jaxbContext = JAXBContext.newInstance(JPProperties.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setProperty("eclipselink.media-type", MediaType.APPLICATION_JSON);
            unmarshaller.setProperty("eclipselink.json.include-root", false);
            StreamSource propsStreamSource = new StreamSource(propIS);
            JPProperties props = unmarshaller.unmarshal(propsStreamSource, JPProperties.class).getValue();

            return props;
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void saveJPProperties(String propType, JPProperties properties) {
        JPObjectConfigurationPaths.getObjProperties(objID, propType, "json")
                .map(this::createFileIfNeeded)
                .ifPresent(path -> marshallJPProperties(properties, path));
    }

    private void marshallJPProperties(JPProperties properties, Path path) {
        try {
            OutputStream propOS = Files.newOutputStream(path);
            JAXBContext jaxbContext = JAXBContext.newInstance(JPProperties.class);

            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty("eclipselink.media-type", "application/json");
            marshaller.setProperty("eclipselink.json.include-root", false);

            marshaller.marshal(properties, propOS);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PropertyException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private Path createFileIfNeeded(Path path) {
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return path;
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

            if (created) {
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
