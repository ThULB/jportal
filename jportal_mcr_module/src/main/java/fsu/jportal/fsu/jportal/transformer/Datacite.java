package fsu.jportal.fsu.jportal.transformer;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.content.transformer.MCRXSLTransformer;
import org.mycore.pi.exceptions.MCRPersistentIdentifierException;
import org.xml.sax.SAXException;

public class Datacite extends MCRContentTransformer
{
    Logger LOGGER = LogManager.getLogger();
    @Override
    public MCRContent transform(MCRContent mcrContent) throws IOException {
        LOGGER.info("Datacite Transformer Class");
        MCRXSLTransformer transformer = MCRXSLTransformer.getInstance("xsl/mycoreobject-datacite.xsl");
        MCRContent dataciteXML = transformer.transform(mcrContent);

        try {
            countMissingTags(dataciteXML.asXML());
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (MCRPersistentIdentifierException e) {
            e.printStackTrace();
        }

        return dataciteXML;
    }

    private void countMissingTags(Document resultDoc) throws MCRPersistentIdentifierException {
        Element rootElement = resultDoc.getRootElement();

        String missingTags = Stream.of("identifier", "titles", "creators", "publisher", "publicationYear", "resourceType")
                .map(ElemContent::new)
                .map(element -> element.listIn(rootElement))
                .filter(e -> e.getContent().size() == 0)
                .map(ElemContent::getName)
                .collect(Collectors.joining(","));

        if(!"".equals(missingTags)){
            String message = "Datacite XML missing tags error.";
            String info = "[" + missingTags + "]";
            throw new MCRPersistentIdentifierException(message, info, 900);
        }
    }

    private static class ElemContent {
        private final String name;
        private List<Element> content;

        public ElemContent(String name) {
            this.name = name;
        }

        public ElemContent listIn(Element parent){
            this.content = parent.getContent(Filters.element(getName(), Namespace.getNamespace("http://datacite.org/schema/kernel-3")));
            return this;
        }

        public String getName() {
            return name;
        }

        public List<Element> getContent() {
            return content;
        }
    }
}
