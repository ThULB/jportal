package fsu.jportal.frontend;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.config.MCRConfiguration;

import fsu.jportal.resources.GlobalMessageResource;

public class GlobalMessageFile {
    private static java.nio.file.Path GLOBAL_MESSAGE_FILE;

    private static Logger LOGGER = LogManager.getLogger(GlobalMessageFile.class);

    static {
        try {
            String mcrDataDir = MCRConfiguration.instance().getString("MCR.datadir");
            java.nio.file.Path confDir = Paths.get(mcrDataDir, "config");
            String confFileName = "jp-globalmessage.xml";
            GLOBAL_MESSAGE_FILE = confDir.resolve(confFileName);

            if (!Files.exists(confDir)) {
                Files.createDirectories(confDir);
                LOGGER.info("Creating config directory: " + confDir.toString());
            }

            if (!Files.exists(GLOBAL_MESSAGE_FILE)) {
                InputStream origMsgFile = GlobalMessageResource.class
                    .getResourceAsStream("/META-INF/resources/config/" + confFileName);
                Files.copy(origMsgFile, GLOBAL_MESSAGE_FILE);
                LOGGER.info("Creating global message file: " + GLOBAL_MESSAGE_FILE.toString());
            } else {
                LOGGER.info("Using global message file: " + GLOBAL_MESSAGE_FILE.toString());
            }
        } catch (Exception exc) {
            LOGGER.error("Unable to get globalmessage file", exc);
        }
    }

    public static OutputStream getOutputStream() throws IOException {
        return Files.newOutputStream(GLOBAL_MESSAGE_FILE);
    }

    public static InputStream getInputStream() throws IOException {
        return Files.newInputStream(GLOBAL_MESSAGE_FILE);
    }
}
