package fsu.jportal.backend.pi.doi;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.mycore.access.MCRAccessException;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRXSLTransformer;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRBase;
import org.mycore.pi.doi.MCRDOIService;
import org.mycore.pi.doi.MCRDigitalObjectIdentifier;
import org.mycore.pi.exceptions.MCRPersistentIdentifierException;
import org.xml.sax.SAXException;

public class Service extends MCRDOIService {
    public Service(String serviceID) {
        super(serviceID);
    }

    @Override
    public synchronized MCRDigitalObjectIdentifier register(MCRBase obj, String additional, boolean updateObject) throws MCRAccessException, MCRActiveLinkException, MCRPersistentIdentifierException, ExecutionException, InterruptedException {
        // check journal config if allowed
        MCRXSLTransformer transformer = MCRXSLTransformer.getInstance("xsl/mycoreobject-datacite.xsl");
        try {
            MCRContent dataciteXML = transformer.transform(new MCRJDOMContent(obj.createXML()));
            countMissingTags(dataciteXML.asXML());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        return super.register(obj, additional, updateObject);
    }

    private void countMissingTags(Document resultDoc) throws MCRPersistentIdentifierException {
        Element rootElement = resultDoc.getRootElement();

        String missingTags = Stream.of("titles", "creators", "publisher", "publicationYear", "resourceType")
                .map(ElemContent::new)
                .map(element -> element.listIn(rootElement))
                .filter(e -> e.getContent().size() == 0)
                .map(ElemContent::getName)
                .collect(Collectors.joining(","));

        if(!"".equals(missingTags)){
            String message = "Datacite XML missing tags error.";
            throw new MCRPersistentIdentifierException(message, missingTags, 900);
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
