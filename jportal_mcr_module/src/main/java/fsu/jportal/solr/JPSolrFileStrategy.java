package fsu.jportal.solr;

import static org.mycore.solr.MCRSolrConstants.CONFIG_PREFIX;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.logging.log4j.LogManager;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.xml.MCRXMLFunctions;
import org.mycore.solr.index.strategy.MCRSolrFileStrategy;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * JPortal implementation of which files should be send to solr.
 *
 * <ul>
 *     <li>no image/*</li>
 *     <li>no alto files</li>
 *     <li>no mets files</li>
 * </ul>
 *
 * @author Matthias Eichner
 */
public class JPSolrFileStrategy implements MCRSolrFileStrategy {

    private final static Pattern IGNORE_PATTERN;

    private final static List<String> XML_MIME_TYPES = Arrays.asList("application/xml", "text/xml");

    private final static List<String> IGNORE_XML_FILES = Arrays.asList("alto", "mets");

    static {
        String acceptPattern = MCRConfiguration.instance().getString(CONFIG_PREFIX + "MimeTypeStrategy.Pattern");
        IGNORE_PATTERN = Pattern.compile(acceptPattern);
    }

    @Override
    public boolean check(Path path, BasicFileAttributes attrs) {
        String mimeType = MCRXMLFunctions.getMimeType(path.getFileName().toString());
        if (IGNORE_PATTERN.matcher(mimeType).matches()) {
            return false;
        }
        if (XML_MIME_TYPES.contains(mimeType)) {
            Optional<String> rootName = getRootName(path);
            return !IGNORE_XML_FILES.contains(rootName.orElse("").toLowerCase());
        }
        return true;
    }

    private static Optional<String> getRootName(Path path) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser saxParser = factory.newSAXParser();
            ProbeXMLHandler handler = new ProbeXMLHandler();
            try (InputStream is = Files.newInputStream(path)) {
                saxParser.parse(is, handler);
            }
        } catch (ProbeXMLException probeExc) {
            return Optional.of(probeExc.rootName);
        } catch (Exception exc) {
            LogManager.getLogger().warn("unable to probe root node of  " + path);
        }
        return Optional.empty();
    }

    private static class ProbeXMLHandler extends DefaultHandler {

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            throw new ProbeXMLException(qName);
        }

    }

    private static class ProbeXMLException extends RuntimeException {

        private String rootName;

        private ProbeXMLException(String rootName) {
            this.rootName = rootName;
        }

    }

}
