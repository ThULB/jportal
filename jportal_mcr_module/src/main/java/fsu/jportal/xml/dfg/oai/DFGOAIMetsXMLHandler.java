package fsu.jportal.xml.dfg.oai;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import fsu.jportal.xml.stream.DerivateFileInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.ifs.MCRContentInputStream;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRContentTypes;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.frontend.MCRFrontendUtil;

public class DFGOAIMetsXMLHandler {

    private static Logger LOGGER = LogManager.getLogger();

    private final String oaiIdentifier;

    private final Function<String, Optional<XMLStreamReader>> objSupplier;

    private final Function<String, Stream<DerivateFileInfo>> derivateSupplier;

    private UnaryOperator<String> fileSectionHref;

    public DFGOAIMetsXMLHandler(String oaiIdentifier,
                                Function<String, Optional<XMLStreamReader>> objSupplier,
                                Function<String, Stream<DerivateFileInfo>> derivateSupplier,
                                UnaryOperator<String> fileSectionHref) {

        this.oaiIdentifier = oaiIdentifier;
        this.objSupplier = objSupplier;
        this.derivateSupplier = derivateSupplier;
        this.fileSectionHref = fileSectionHref;
    }

    public DFGOAIMetsXMLHandler() {
        this(MCRBackend.oaiId(),
             MCRBackend::mcrXMLMetadataManager,
             MCRBackend::ifs,
             MCRBackend::fileSectionHref);
    }

    public Optional<Consumer<XMLStreamWriter>> handle(String href) {
        long startTime = System.currentTimeMillis();
        try {
            return Optional.ofNullable(href)
                           .filter(uri -> uri.startsWith("dfgOai:"))
                           .map(uri -> uri.split(":")[1])
                           .map(mcrObjID -> DFGOAIMetsXMLCreator
                                   .oaiRecord(mcrObjID,
                                              oaiIdentifier,
                                              objSupplier,
                                              derivateSupplier,
                                              fileSectionHref));
        } finally {
            LOGGER.info("handle (" + href + ") " + (System.currentTimeMillis() - startTime) + "ms");
        }
    }

    private static class MCRBackend {
        public static String oaiId() {
            return MCRConfiguration.instance().getString("OAIRepositoryIdentifier", "noOaiIdentifier");
        }

        public static Stream<DerivateFileInfo> ifs(String derivateID) {
            return DerivateUtils.stream(derivateID)
                                .filter(Files::isRegularFile)
                                .map(DerivateUtils::fileInfo);
        }

        public static Optional<XMLStreamReader> mcrXMLMetadataManager(String id) {
            try {
                MCRContentInputStream contentInputStream = MCRXMLMetadataManager
                        .instance()
                        .retrieveContent(MCRObjectID.getInstance(id))
                        .getContentInputStream();
                XMLInputFactory factory = XMLInputFactory.newFactory();
                return Optional.of(factory.createXMLStreamReader(contentInputStream));
            } catch (Throwable t) {
                LOGGER.error("Unable to retrieve metadata from " + id, t);
            }
            return Optional.empty();
        }

        public static String fileSectionHref(String s) {
            String baseURL = MCRFrontendUtil.getBaseURL() + "/servlets/MCRTileCombineServlet/MID";
            return Optional.of(s.replaceFirst("ifs", ""))
                    .map(url -> url.replaceAll("\\:/", "/"))
                    .map(baseURL::concat)
                    .orElse("noHref");
        }
    }

    private static class DerivateUtils {
        public static DerivateFileInfo fileInfo(Path path) {
            String fileName = path.getFileName().toString();
            String uri = path.toUri().toString();
            String contentType = getContentType(path);

            return new DerivateFileInfo(contentType, fileName, uri);
        }

        private static String getContentType(Path path) {
            try {
                return MCRContentTypes.probeContentType(path);
            } catch (Throwable t) {
                LOGGER.error("Unable to get content type of " + path, t);
            }
            return "noContentType";
        }

        public static Stream<Path> stream(String derivateID) {
            MCRPath derivRoot = MCRPath.getPath(derivateID, "/");
            return pathStream(derivRoot);
        }

        private static Stream<Path> pathStream(MCRPath path) {
            try {
                return StreamSupport.stream(Files.newDirectoryStream(path).spliterator(), false);
            } catch (Throwable t) {
                LOGGER.error("Unable to stream path " + path, t);
            }
            return Stream.empty();
        }
    }

}
