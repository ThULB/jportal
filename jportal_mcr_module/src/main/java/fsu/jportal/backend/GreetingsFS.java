package fsu.jportal.backend;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.content.MCRContent;

import com.google.common.base.Charsets;

import fsu.jportal.resolver.JournalFilesResolver;

public class GreetingsFS {

    private static final Logger LOGGER = LogManager.getLogger(GreetingsFS.class);

    private final Path GREETINGS_DIR;

    private final String GREETINGS_FILE = "intro.xml";

    private final Path GREETINGS_FILEPATH;

    private final String journalID;

    public GreetingsFS(String journalID) {
        this.journalID = journalID;
        GREETINGS_DIR = JournalFilesResolver.getPath(journalID);
        if (!Files.exists(GREETINGS_DIR)) {
            try {
                Files.createDirectories(GREETINGS_DIR);
            } catch (Exception exc) {
                LOGGER.error("while creating import directory " + GREETINGS_DIR, exc);
            }
        }
        GREETINGS_FILEPATH = GREETINGS_DIR.resolve(GREETINGS_FILE);
    }

    public void store(MCRContent content) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(GREETINGS_FILEPATH, Charsets.UTF_8)) {
            writer.write(content.asString());
        }
    }

    public boolean exists() {
        return Files.exists(GREETINGS_FILEPATH);
    }

    public JDOMSource receive() throws IOException, JDOMException {
        if (exists()) {
            try (BufferedReader reader = Files.newBufferedReader(GREETINGS_FILEPATH, Charsets.UTF_8)) {
                StringBuffer buf = new StringBuffer();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    buf.append(line);
                }
                SAXBuilder builder = new SAXBuilder();
                return new JDOMSource(builder.build(new StringReader(buf.toString())));
            }
        }
        return null;
    }

    public JDOMSource receiveDefault() throws IOException, JDOMException {
        SAXBuilder builder = new SAXBuilder();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("META-INF/resources/jp-index.xml");
        return new JDOMSource(builder.build(is));
    }

    public void delete() throws IOException {
        if (exists()) {
            Files.delete(GREETINGS_FILEPATH);
        }
    }

    public String getjournalID() {
        return journalID;
    }

}