package org.mycore.common.xml;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.transform.JDOMSource;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.MCRLayoutUtilities;

public class MCRJPortalURIGetJournalID implements URIResolver {

    private static final Logger LOGGER = Logger.getLogger(MCRJPortalURIGetJournalID.class);

    private static String URI = "jportal_getJournalID";

    static javax.xml.transform.TransformerFactory factory = javax.xml.transform.TransformerFactory.newInstance();

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
        if(currentObjID.equals("")) {
            return "";
        }
        MCRObjectID mcrId;
        try {
            mcrId = MCRObjectID.getInstance(currentObjID);
        } catch(MCRException exc) {
            LOGGER.error("invalid MyCoRe ID " + currentObjID);
            return "";
        }
        String[] oldJournalID = (String[]) MCRSessionMgr.getCurrentSession().get("journalIDForObj");
        if (oldJournalID != null && oldJournalID.length == 2 && currentObjID.equals(oldJournalID[0])) {
            return oldJournalID[1];
        }
        Document objXML = MCRXMLMetadataManager.instance().retrieveXML(mcrId);
        XPathExpression<Element> hiddenJournalIDXpath = XPathFactory.instance().compile(
                "/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID", Filters.element());
        Element hiddenJournalIDElement = hiddenJournalIDXpath.evaluateFirst(objXML);
        if(hiddenJournalIDElement == null) {
            LOGGER.error("unable to parse  object " + currentObjID);
            return "";
        }
        String journalID = hiddenJournalIDElement.getText();
        if (journalID != null && !journalID.equals("")) {
            String[] journalIDForObj = { currentObjID, journalID };
            MCRSessionMgr.getCurrentSession().put("journalIDForObj", journalIDForObj);
        }
        return journalID;
    }

    private boolean wellURI(String uri) {
        String[] parameters = uri.split(":");
        if (parameters.length == 2 && parameters[0].equals(URI) && !parameters[1].equals("")) {
            return true;
        }
        return false;
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        return new JDOMSource(resolveElement(href));
    }

}
