package fsu.jportal.resolver;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
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
import org.mycore.common.MCRException;
import org.mycore.common.MCRTextResolver;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaInterface;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectMetadata;
import org.mycore.solr.MCRSolrServerFactory;
import org.mycore.solr.search.MCRSolrURL;

/**
 * Resolves the previous and the next object.
 * 
 * Depending on the objects type and its sort metadata two solr queries
 * are created.
 * 
 * @author Matthias Eichner
 */
public class ObjectScrollResolver implements URIResolver {

    private static final Logger LOGGER = Logger.getLogger(ObjectScrollResolver.class);

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        href = href.substring(href.indexOf(":") + 1);
        MCRObjectID mcrID = MCRObjectID.getInstance(href);
        MCRObject mcrObj;

        if (!MCRMetadataManager.exists(mcrID)) {
            return new JDOMSource(buildScrollElement());
        }
        
        try {
            mcrObj = MCRMetadataManager.retrieveMCRObject(mcrID);
        } catch (Exception exc) {
            throw new TransformerException("unable to get object " + href, exc);
        }
        // get parent
        MCRObjectID parentID = mcrObj.getStructure().getParentID();
        if (parentID == null) {
            return new JDOMSource(buildScrollElement());
        }
        // build query
        String sortValue = getSortValue(mcrObj);
        String prevQry = buildQuery(mcrID, parentID.toString(), "indexPosition", sortValue, false);
        String nextQry = buildQuery(mcrID, parentID.toString(), "indexPosition", sortValue, true);
        // do request
        MCRSolrURL prevURL = new MCRSolrURL((HttpSolrServer) MCRSolrServerFactory.getSolrServer(), prevQry);
        MCRSolrURL nextURL = new MCRSolrURL((HttpSolrServer) MCRSolrServerFactory.getSolrServer(), nextQry);
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            Document prevDoc = saxBuilder.build(prevURL.openStream());
            Document nextDoc = saxBuilder.build(nextURL.openStream());
            return new JDOMSource(buildResult((Element) prevDoc.getRootElement().detach(), (Element) nextDoc
                    .getRootElement().detach()));
        } catch (IOException e) {
            LOGGER.error("Unable to get input stream from solr", e);
            throw new TransformerException("Unable to get input stream from solr", e);
        } catch (JDOMException e) {
            throw new TransformerException("Unable to build jdom", e);
        }
    }

    protected Element buildScrollElement() {
        return new Element("scroll");
    }

    protected Element buildResult(Element prev, Element next) throws JDOMException {
        Element root = buildScrollElement();
        if (prev != null && prev.getName().equals("response")) {
            root.addContent(buildScrollElement("previous", prev));
        }
        if (next != null && next.getName().equals("response")) {
            root.addContent(buildScrollElement("next", next));
        }
        return root;
    }

    protected Element buildScrollElement(String name, Element e) throws JDOMException {
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

    protected final String getElementText(Element e, String xpath) throws JDOMException {
        XPathExpression<Text> path = XPathFactory.instance().compile(xpath, Filters.text());
        Text text = path.evaluateFirst(e);
        return text == null ? null : text.getText();
    }

    protected final String getElementAttr(Element e, String xpath) throws JDOMException {
        XPathExpression<Attribute> path = XPathFactory.instance().compile(xpath, Filters.attribute());
        Attribute attr = path.evaluateFirst(e);
        return attr == null ? null : attr.getValue();
    }

    protected String buildQuery(MCRObjectID objID, String parentID, String field, String value, boolean next) {
        StringBuilder returnQuery = new StringBuilder("sort=");
        value = value != null ? value : "0";
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

    /**
     * Returns the search type with its given value.
     * 
     * @param mcrObj
     * @return
     */
    protected String getSortValue(MCRObject mcrObj) {
        String objectType = mcrObj.getId().getTypeId();
        MCRObjectMetadata metadata = mcrObj.getMetadata();
        if (objectType.equals("jpvolume")) {
            return getElementValue(metadata, "hidden_positions");
        } else if (objectType.equals("jparticle")) {
            return getElementValue(metadata, "sizes");
        }
        throw new MCRException("Unsupported object type " + objectType);
    }

    private String getElementValue(MCRObjectMetadata metadata, String element) {
        MCRMetaElement metaElement = metadata.getMetadataElement(element);
        if (metaElement != null) {
            Iterator<MCRMetaInterface> it = metaElement.iterator();
            while (it.hasNext()) {
                MCRMetaInterface metaInterface = it.next();
                if (metaInterface.getInherited() == 0) {
                    try {
                        String text = ((MCRMetaLangText) metaInterface).getText();
                        text = text.replaceAll("\"", "\\\\\"");
                        return URLEncoder.encode(text, "UTF-8");
                    } catch (UnsupportedEncodingException uee) {
                        LOGGER.error(uee);
                        break;
                    }
                }
            }
        }
        return null;
    }

}
