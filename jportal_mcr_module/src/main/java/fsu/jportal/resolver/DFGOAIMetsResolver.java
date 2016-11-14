package fsu.jportal.resolver;

import fsu.jportal.xml.JPXMLFunctions;
import fsu.jportal.xml.dfg.oai.DFGOAIMetXMLCreator;
import fsu.jportal.xml.stream.DerivateFileInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.ifs.MCRContentInputStream;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRContentTypes;
import org.mycore.datamodel.niofs.MCRPath;

import javax.xml.stream.*;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by chi on 17.10.16.
 */
@URIResolverSchema(schema = "dfgOai")
public class DFGOAIMetsResolver implements URIResolver {

    private static Logger LOGGER = LogManager.getLogger();

    private final String oaiIdentifier;

    private final Function<String, Optional<XMLStreamReader>> objSupplier;

    private final Function<String, Stream<DerivateFileInfo>> derivateSupplier;

    private final UnaryOperator<String> getPublishedISODate;

    public DFGOAIMetsResolver(String oaiIdentifier,
                              Function<String, Optional<XMLStreamReader>> objSupplier,
                              Function<String, Stream<DerivateFileInfo>> derivateSupplier,
                              UnaryOperator<String> getPublishedISODate) {

        this.oaiIdentifier = oaiIdentifier;
        this.objSupplier = objSupplier;
        this.derivateSupplier = derivateSupplier;
        this.getPublishedISODate = getPublishedISODate;
    }

    public DFGOAIMetsResolver() {
        this(MCRBackend.oaiId(),
             MCRBackend::mcrXMLMetadataManager,
             MCRBackend::ifs,
             MCRBackend::getPublishedISODate);
    }

    /**
     * @param href dfgOai:{McrObjID}
     * @param base
     * @return
     * @throws TransformerException
     */
    @Override
    public Source resolve(String href, String base) throws TransformerException {
        LOGGER.info("Start transforming oai " + href + "...");
        long startTime = System.currentTimeMillis();
        try {
            return Optional.of(href)
                           .filter(uri -> uri.endsWith("?format=mets-dfg-xsl"))
                           .map(uri -> uri.split("\\?")[0])
                           .map(DFGOAIMetsResolver::dfgMetsXSL)
                           .orElseGet(() -> dfgMets(href.split("\\?")[0]));
        } finally {
            LOGGER.info("Transforming " + href + " took " + ((System.currentTimeMillis() - startTime) / 1000) + "s");
        }

    }

    private static Source dfgMetsXSL(String href) {
        String uriTmp = "xslStyle:jp2mets-dfg?identifier=" + MCRBackend.oaiId() + ":mcrobject:";
        return Optional.of(href)
                       .filter(uri -> uri.startsWith("dfgOai:"))
                       .map(uri -> uri.split(":")[1])
                       .map(uriTmp::concat)
                       .flatMap(DFGOAIMetsResolver::resolve)
                       .orElse(null);
    }

    private static Optional<Source> resolve(String h) {
        try {
            Source source = MCRURIResolver.instance().resolve(h, "");
            return Optional.of(source);
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    private Source dfgMets(String href) {
        return Optional.ofNullable(href)
                       .filter(uri -> uri.startsWith("dfgOai:"))
                       .map(uri -> uri.split(":")[1])
                       .map(mcrObjID -> DFGOAIMetXMLCreator
                               .oaiRecord(mcrObjID,
                                          oaiIdentifier,
                                          objSupplier,
                                          derivateSupplier,
                                          getPublishedISODate))
                       .flatMap(DFGOAIMetsResolver::toSource)
                       .orElse(null);
    }

    private static Optional<Source> toSource(Consumer<XMLStreamWriter> consumer) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(byteArrayOutputStream);
            consumer.accept(xmlStreamWriter);

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            return Optional.of(byteArrayInputStream)
                           .map(StreamSource::new);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        return Optional.empty();
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
            } catch (IOException | XMLStreamException e) {
                e.printStackTrace();
            }

            return Optional.empty();
        }

        public static String getPublishedISODate(String id) {
            return JPXMLFunctions.getPublishedISODate(id);
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
            } catch (IOException e) {
                e.printStackTrace();
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
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Stream.empty();
        }
    }


}
