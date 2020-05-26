package fsu.jportal.resolver;

import fsu.jportal.backend.JPContainer;
import fsu.jportal.backend.MetadataManager;
import fsu.jportal.util.JPComponentUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.jdom2.*;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.transform.JDOMSource;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRTextResolver;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.search.MCRSolrURL;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves the previous and the next object.
 *
 * @author Matthias Eichner
 */
@URIResolverSchema(schema = "objectScroll")
public class ObjectScrollResolver implements URIResolver {

    private static final Logger LOGGER = LogManager.getLogger(ObjectScrollResolver.class);

    @Override
    public Source resolve(String href, String base) {
        href = href.substring(href.indexOf(":") + 1);
        MCRObjectID mcrID = MCRObjectID.getInstance(href);
        Element scrollElement = buildScrollElement();
        if (!MCRMetadataManager.exists(mcrID)) {
            return new JDOMSource(scrollElement);
        }
        MCRObject object = MetadataManager.retrieveMCRObject(mcrID);
        // get parent
        MCRObjectID parentID = object.getStructure().getParentID();
        if (parentID == null) {
            return new JDOMSource(scrollElement);
        }
        // build query
        Optional<JPContainer> containerOptional = JPComponentUtil.getContainer(parentID);
        if (!containerOptional.isPresent()) {
            return new JDOMSource(scrollElement);
        }
        List<MCRObjectID> children = containerOptional.get().getChildren();
        int index = children.indexOf(mcrID);
        MCRObjectID prev = index > 0 ? children.get(index - 1) : null;
        MCRObjectID next = index + 1 < children.size() ? children.get(index + 1) : null;
        if (prev != null) {
            scrollElement.addContent(new Element("previous").setAttribute("id", prev.toString()));
        }
        if (next != null) {
            scrollElement.addContent(new Element("next").setAttribute("id", next.toString()));
        }
        return new JDOMSource(scrollElement);
    }

    protected Element buildScrollElement() {
        return new Element("scroll");
    }

}
