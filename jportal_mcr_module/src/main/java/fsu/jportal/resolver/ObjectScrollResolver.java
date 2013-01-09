package fsu.jportal.resolver;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.transform.JDOMSource;
import org.jdom.xpath.XPath;
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

    private enum SearchType {
        maintitle, size, position
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        href = href.substring(href.indexOf(":") + 1);
        MCRObjectID mcrId = MCRObjectID.getInstance(href);
        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(mcrId);
        // get parent
        MCRObjectID parentID = mcrObj.getStructure().getParentID();
        if (parentID == null) {
            return new JDOMSource(buildScrollElement());
        }
        // get search pair
        Pair<SearchType, String> pair = getSearchPair(mcrObj);
        if (pair == null) {
            return new JDOMSource(buildScrollElement());
        }
        // build query
        String prevQry = buildQuery(mcrId.getTypeId(), parentID.toString(), pair.key.name(), pair.value, false);
        String nextQry = buildQuery(mcrId.getTypeId(), parentID.toString(), pair.key.name(), pair.value, true);
        // do request
        MCRSolrURL prevURL = new MCRSolrURL(MCRSolrServerFactory.getSolrServer(), prevQry);
        MCRSolrURL nextURL = new MCRSolrURL(MCRSolrServerFactory.getSolrServer(), nextQry);
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            Document prevDoc = saxBuilder.build(prevURL.openStream());
            Document nextDoc = saxBuilder.build(nextURL.openStream());
            return new JDOMSource(buildResult((Element) prevDoc.getRootElement().detach(), (Element) nextDoc.getRootElement().detach()));
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
        if(numFound == null || Integer.valueOf(numFound) == 0) {
            return returnElement;
        }
        String id = getElementText(e, "result/doc/str[@name='id']/text()");
        String title = getElementText(e, "result/doc/str[@name='maintitle']/text()");
        if(id == null || title == null) {
            return returnElement;
        }
        return returnElement.setAttribute("id", id).setAttribute("title", title);
    }

    protected final String getElementText(Element e, String xpath) throws JDOMException {
        Text text = (Text) XPath.selectSingleNode(e, xpath);
        return text != null ? text.getText() : null;
    }
    protected final String getElementAttr(Element e, String xpath) throws JDOMException {
        Attribute attr = (Attribute) XPath.selectSingleNode(e, xpath);
        return attr != null ? attr.getValue() : null;
    }

    // sort=maintitle+desc&q=%2Bparent:jportal_jpvolume_00000001+%2BobjectType:jparticle+%2Bmaintitle:{* TO "abc"}&rows=1
    protected String buildQuery(String objectType, String parentID, String field, String value, boolean next) {
        StringBuilder sb = new StringBuilder("sort=");
        sb.append(field).append("+").append(next ? "asc" : "desc");
        sb.append("&q=%2Bparent:").append(parentID);
        sb.append("+%2BobjectType:").append(objectType);
        sb.append("+%2B").append(field).append(":{");
        if (next) {
            sb.append("%22").append(value).append("%22%20TO%20*}");
        } else {
            sb.append("*%20TO%20%22").append(value).append("%22}");
        }
        sb.append("&rows=1");
        return sb.toString();
    }

    /**
     * Returns the search type with its given value.
     * 
     * @param mcrObj
     * @return
     */
    protected Pair<SearchType, String> getSearchPair(MCRObject mcrObj) {
        String objectType = mcrObj.getId().getTypeId();
        if (!objectType.equals("jpvolume") && !objectType.equals("jparticle")) {
            return null;
        }
        MCRObjectMetadata metadata = mcrObj.getMetadata();
        if (objectType.equals("jpvolume")) {
            String value = getElementValue(metadata, "hidden_positions");
            if (value != null) {
                return new Pair<SearchType, String>(SearchType.position, value);
            }
        } else {
            String value = getElementValue(metadata, "sizes");
            if (value != null) {
                return new Pair<SearchType, String>(SearchType.size, value);
            }
        }
        String value = getElementValue(metadata, "maintitles");
        return value != null ? new Pair<SearchType, String>(SearchType.maintitle, value) : null;
    }

    private String getElementValue(MCRObjectMetadata metadata, String element) {
        MCRMetaElement metaElement = metadata.getMetadataElement(element);
        if (metaElement != null) {
            Iterator<MCRMetaInterface> it = metaElement.iterator();
            while(it.hasNext()) {
                MCRMetaInterface metaInterface = it.next();
                if(metaInterface.getInherited() == 0) {
                    try {
                        return URLEncoder.encode(((MCRMetaLangText) metaInterface).getText(), "UTF-8");
                    } catch(UnsupportedEncodingException uee) {
                        LOGGER.error(uee);
                        break;
                    }
                }
            }
        }
        return null;
    }

    private static final class Pair<K, V> {
        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K key;

        public V value;
    }

}
