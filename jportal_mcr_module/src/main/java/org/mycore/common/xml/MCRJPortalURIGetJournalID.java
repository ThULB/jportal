package org.mycore.common.xml;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;
import org.jdom.xpath.XPath;
import org.mycore.common.MCRCache;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.MCRLayoutUtilities;
import org.mycore.services.fieldquery.MCRQuery;
import org.mycore.services.fieldquery.MCRQueryManager;
import org.mycore.services.fieldquery.MCRResults;

public class MCRJPortalURIGetJournalID implements URIResolver {

    private static final Logger LOGGER = Logger.getLogger(MCRJPortalURIGetJournalID.class);

    private static final MCRConfiguration CONFIG = MCRConfiguration.instance();

    private static String URI = "jportal_getJournalID";

    static javax.xml.transform.TransformerFactory factory = javax.xml.transform.TransformerFactory.newInstance();

    private static MCRCache WEBCONTEXTtwoJID_CACHE;

    public MCRJPortalURIGetJournalID() {
        initCache();
    }

    private void initCache() {
        WEBCONTEXTtwoJID_CACHE = new MCRCache(1000, "MCRJPortalURIGetJournalID");
    }

    /**
     * Syntax: jportal_getJournalID:XPath2BeFilled
     * 
     * -> Use, if you want to know the Journal-ID that has an own layout template
     * 
     * @param uri
     *            URI in the syntax above
     *            
     * @return 
     * <dummyRoot>
     * 	 <hidden var="XPath2BeFilled" default="journalID" />
     * </dummyRoot>
     */

    public Element resolveElement(String uri) {
        LOGGER.debug("start resolving " + uri);

        if (!wellURI(uri))
            throw new IllegalArgumentException("Invalid format of uri given to resolve " + URI + "=" + uri);

        String[] uriParams = uri.split(":");
        String xPath2BeFilled = uriParams[1];

        Element returnXML = new Element("dummyRoot");
        returnXML.addContent(new Element("hidden").setAttribute("var", xPath2BeFilled).setAttribute("default", getID()));

        return returnXML;
    }

    /**
     * @return The Journal-ID as String or "" if no Journal-ID can be found.
     * @throws JDOMException 
     */
    public static String getID() {
        // in jp-layout-main.xsl - renderLayout the current object ID will be
        // set in the session. The method name "getLastValidPageID" is miss leading.
        // It was used for another reason. Should be changed in the next version.
        String currentObjID = MCRLayoutUtilities.getLastValidPageID();

        // TODO: fix this
        try {
            MCRObjectID.getInstance(currentObjID);
            String[] oldJournalID = (String[]) MCRSessionMgr.getCurrentSession().get("journalIDForObj");
            if (oldJournalID != null && oldJournalID.length == 2 && currentObjID.equals(oldJournalID[0])) {
                return oldJournalID[1];
            }
            Document objXML = MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(currentObjID));
            try {
                XPath hiddenJournalIDXpath = XPath.newInstance("/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID");
                String journalID = hiddenJournalIDXpath.valueOf(objXML);
    
                if (journalID != null && !journalID.equals("")) {
                    String[] journalIDForObj = { currentObjID, journalID };
                    MCRSessionMgr.getCurrentSession().put("journalIDForObj", journalIDForObj);
                }
    
                return journalID;
            } catch (JDOMException e) {
                e.printStackTrace();
            }
        } catch(MCRException exc) {
            exc.printStackTrace();
        }

        return "";
    }
    
    /**
     * @param webSiteContext:
     *            Address of static xml file in navigation.xml that has a
     *            template assignment which fits a
     *            Journal-XML/metadata/websitecontext/text()
     * 
     * @return Journal-ID as String or "" if no Journal-ID can be found
     */
    private static String getJournalID(String webSiteContext) {
        // search for all jpjournal ids containing this website context
        String query = "(objectType = \"jpjournal\") and (webcontext = \"" + webSiteContext + "\")";
        Document input = getQueryDocument(query, null, null);
        // Execute query
        long start = System.currentTimeMillis();
        MCRResults resultIDs = MCRQueryManager.search(MCRQuery.parseXML(input));
        long qtime = System.currentTimeMillis() - start;
        LOGGER.debug("MCRSearching total query time: " + qtime);
        try {
            XMLOutputter out = new XMLOutputter();
            if (resultIDs != null) {
                LOGGER.debug("--gefundener jids=");
                out.output(resultIDs.buildXML(), System.out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String journalID = "";
        if (resultIDs.getNumHits() == 0) {
            LOGGER.warn("No journal found, that contains current website context=" + webSiteContext);
        } else if (resultIDs.getNumHits() > 1) {
            LOGGER.warn("Unexactly! More than one journal found, that contains current website context=" + webSiteContext + ". "
                            + "Do not use getJPJournalID resolver");
        } else {
            journalID = resultIDs.getHit(0).getID();
            LOGGER.debug("--ID=" + journalID);
        }
        return journalID;
    }

    private static String getWebSiteContext(String lastPage) {
        // get website context
        Element webSiteContextElem = new Element("root");
        String baseDir = CONFIG.getString("MCR.basedir");
        StreamSource xsl = new StreamSource(MCRJPortalURIGetJournalID.class.getResourceAsStream("/xsl/getWebsiteContext.xsl"));
        JDOMSource source = new JDOMSource(webSiteContextElem);
        JDOMResult result = new JDOMResult();
        try {
            Transformer transformer = factory.newTransformer(xsl);
            transformer.setParameter("lastpage", lastPage);
            transformer.setParameter("basedir", baseDir);
            transformer.transform(source, result);

        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        webSiteContextElem = result.getDocument().getRootElement();

        if (webSiteContextElem.getTextTrim().equals("root")) {
            LOGGER.warn("Didn't find website context URL in navigation! So, no journal id can be given back");
        } else {
            XMLOutputter out = new XMLOutputter();
            try {
                LOGGER.debug("Found websitecontext=");
                out.output(webSiteContextElem, System.out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String webSiteContext = webSiteContextElem.getTextTrim();
        return webSiteContext;
    }

    private boolean wellURI(String uri) {
        String[] parameters = uri.split(":");
        if (parameters.length == 2 && parameters[0].equals(URI) && !parameters[1].equals("")) {
            return true;
        }
        return false;
    }

    private static Document getQueryDocument(String query, String sortby, String order) {
        Element queryElement = new Element("query");
        queryElement.setAttribute("maxResults", "0");
        queryElement.setAttribute("numPerPage", "0");
        Document input = new Document(queryElement);

        Element conditions = new Element("conditions");
        queryElement.addContent(conditions);
        conditions.setAttribute("format", "text");
        conditions.addContent(query);
        org.jdom.Element root = input.getRootElement();
        if (sortby != null) {
            final Element fieldElement = new Element("field").setAttribute("name", sortby);
            if (order != null) {
                fieldElement.setAttribute("order", order);
            }
            root.addContent(new Element("sortBy").addContent(fieldElement));
        }
        if (LOGGER.isDebugEnabled()) {
            XMLOutputter out = new XMLOutputter(org.jdom.output.Format.getPrettyFormat());
            LOGGER.debug(out.outputString(input));
        }
        return input;
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        return new JDOMSource(resolveElement(href));
    }

}
