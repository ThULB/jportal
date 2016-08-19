package fsu.jportal.resolver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Text;
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

import fsu.jportal.util.JPComponentUtil;

/**
 * Resolves the previous and the next object.
 * 
 * Depending on the objects type and its sort metadata two solr queries
 * are created.
 * 
 * @author Matthias Eichner
 */
@URIResolverSchema(schema = "objectScroll")
public class ObjectScrollResolver implements URIResolver {

    private static final Logger LOGGER = LogManager.getLogger(ObjectScrollResolver.class);

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        try {
            href = href.substring(href.indexOf(":") + 1);
            MCRObjectID mcrID = MCRObjectID.getInstance(href);
            if (!MCRMetadataManager.exists(mcrID)) {
                return new JDOMSource(buildScrollElement());
            }
            MCRObject object = MCRMetadataManager.retrieveMCRObject(mcrID);
            // get parent
            MCRObjectID parentID = object.getStructure().getParentID();
            if (parentID == null) {
                return new JDOMSource(buildScrollElement());
            }
            // build query
            Integer order = JPComponentUtil.getOrder(mcrID);
            order = order != null ? order : 0;
            String prevQry = buildQuery(mcrID, parentID.toString(), "order", order.toString(), false);
            String nextQry = buildQuery(mcrID, parentID.toString(), "order", order.toString(), true);
            // do request
            MCRSolrURL prevURL = new MCRSolrURL((HttpSolrClient) MCRSolrClientFactory.getSolrClient(), prevQry);
            MCRSolrURL nextURL = new MCRSolrURL((HttpSolrClient) MCRSolrClientFactory.getSolrClient(), nextQry);
            SAXBuilder saxBuilder = new SAXBuilder();
            Document prevDoc = saxBuilder.build(prevURL.openStream());
            Document nextDoc = saxBuilder.build(nextURL.openStream());
            return new JDOMSource(
                buildResult((Element) prevDoc.getRootElement().detach(), (Element) nextDoc.getRootElement().detach()));
        } catch (IOException e) {
            LOGGER.error("Unable to get input stream from solr", e);
            throw new TransformerException("Unable to get input stream from solr", e);
        } catch (JDOMException e) {
            throw new TransformerException("Unable to build jdom", e);
        } catch (Exception e) {
            throw new TransformerException("Unable to handle object scroll resolver", e);
        }
    }

    protected Element buildScrollElement() {
        return new Element("scroll");
    }

    protected Element buildResult(Element prev, Element next) {
        Element root = buildScrollElement();
        if (prev != null && prev.getName().equals("response")) {
            root.addContent(buildScrollElement("previous", prev));
        }
        if (next != null && next.getName().equals("response")) {
            root.addContent(buildScrollElement("next", next));
        }
        return root;
    }

    protected Element buildScrollElement(String name, Element e) {
        Element returnElement = new Element(name);
        String numFound = getElementAttr(e, "result/@numFound");
        if (numFound == null || Integer.valueOf(numFound) == 0) {
            return returnElement;
        }
        String id = getElementText(e, "result/doc/str[@name='id']/text()");
        String title = getElementText(e, "result/doc/str[@name='maintitle']/text()");
        if (id == null || title == null) {
            return returnElement;
        }
        return returnElement.setAttribute("id", id).setAttribute("title", title);
    }

    protected final String getElementText(Element e, String xpath) {
        XPathExpression<Text> path = XPathFactory.instance().compile(xpath, Filters.text());
        Text text = path.evaluateFirst(e);
        return text == null ? null : text.getText();
    }

    protected final String getElementAttr(Element e, String xpath) {
        XPathExpression<Attribute> path = XPathFactory.instance().compile(xpath, Filters.attribute());
        Attribute attr = path.evaluateFirst(e);
        return attr == null ? null : attr.getValue();
    }

    protected String buildQuery(MCRObjectID objID, String parentID, String field, String value, boolean next) {
        StringBuilder returnQuery = new StringBuilder("sort=");
        returnQuery.append(field).append("%20").append(next ? "asc" : "desc").append(",");
        returnQuery.append("id").append("%20").append(next ? "asc" : "desc");
        returnQuery.append("&rows=1&q=");
        String qry = "+parent:{parent} +objectType:{objectType} +((+{field}:'{value}' AND +id:\\{";
        if (next) {
            qry += "'{id}' TO *\\}) (+{field}:\\{'{value}' TO *\\}))";
        } else {
            qry += "* TO '{id}'\\}) (+{field}:\\{* TO '{value}'\\}))";
        }
        qry = qry.replaceAll(" ", "%20");
        qry = qry.replaceAll("\\+", "%2B");
        qry = qry.replaceAll("'", "%22");
        Map<String, String> varMap = new HashMap<>();
        varMap.put("parent", parentID);
        varMap.put("id", objID.toString());
        varMap.put("objectType", objID.getTypeId());
        varMap.put("field", field);
        varMap.put("value", value);
        String resolvedQuery = new MCRTextResolver(varMap).resolve(qry);
        return returnQuery.append(resolvedQuery).toString();
    }

}
