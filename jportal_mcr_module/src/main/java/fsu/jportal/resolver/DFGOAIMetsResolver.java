package fsu.jportal.resolver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.function.Consumer;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.xml.MCRURIResolver;

import fsu.jportal.backend.mcr.JPConfig;
import fsu.jportal.xml.dfg.oai.DFGOAIMetsXMLHandler;
import fsu.jportal.xml.stream.ParserUtils;

/**
 * Created by chi on 17.10.16.
 */
@URIResolverSchema(schema = "dfgOai")
public class DFGOAIMetsResolver implements URIResolver {

    private static Logger LOGGER = LogManager.getLogger();

    private DFGOAIMetsXMLHandler handler;
    
    public DFGOAIMetsResolver() {
        handler = new DFGOAIMetsXMLHandler();
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
        String format = href.substring(href.indexOf("format=") + 7);
        try {
            String newHref = href.split("\\?")[0];
            switch (format) {
            case "mets-dfg-xsl":
                return DFGOAIMetsResolver.dfgMetsXSL(newHref);
            case "mets-dfg":
                return dfgMets(newHref);
            default:
                return oaiDCXSL(newHref);
            }
        } finally {
            LOGGER.info("Transforming " + href + " took " + ((System.currentTimeMillis() - startTime) / 1000) + "s");
        }
    }

    public static Source oaiDCXSL(String href) throws TransformerException {
        String id = JPConfig.getString("OAIRepositoryIdentifier", "noOaiIdentifier");
        String objectId = href.split(":")[1];
        return MCRURIResolver.instance().resolve("xslStyle:jp2oai_dc?identifier=" + id + ":mcrobject:" + objectId, "");
    }

    private static Source dfgMetsXSL(String href) {
        String id = JPConfig.getString("OAIRepositoryIdentifier", "noOaiIdentifier");
        String uriTmp = "xslStyle:jp2mets-dfg?identifier=" + id + ":mcrobject:";
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
            LOGGER.error("Unable to resolve " + h, e);
        }
        return Optional.empty();
    }

    private Source dfgMets(String href) {
        long startTime = System.currentTimeMillis();
        try {
            return handler.handle(href).flatMap(DFGOAIMetsResolver::toSource).orElse(null);
        } finally {
            LOGGER.info("Total time for createObj " + ParserUtils.CREATE_OBJECT_TIME + "ms");
            LOGGER.info("Time for dfgMets (" + href + ") " + (System.currentTimeMillis() - startTime) + "ms");
            ParserUtils.CREATE_OBJECT_TIME = 0;
        }
    }

    private static Optional<Source> toSource(Consumer<XMLStreamWriter> consumer) {
        long startTime = System.currentTimeMillis();
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(byteArrayOutputStream);
            consumer.accept(xmlStreamWriter);

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            return Optional.of(byteArrayInputStream)
                           .map(StreamSource::new);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } finally {
            LOGGER.info("Time for toSource() " + (System.currentTimeMillis() - startTime) + "ms");
        }

        return Optional.empty();
    }

}
