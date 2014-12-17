package org.mycore.common.xml;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

import fsu.jportal.xml.JPXMLFunctions;

public class MCRJPortalURIGetJournalID {

    private static final Logger LOGGER = Logger.getLogger(MCRJPortalURIGetJournalID.class);

    static javax.xml.transform.TransformerFactory factory = javax.xml.transform.TransformerFactory.newInstance();

    /**
     * @return The Journal-ID as String or "" if no Journal-ID can be found.
     * @throws JDOMException 
     */
    public static String getID() {
        // in jp-layout-main.xsl - renderLayout the current object ID will be
        // set in the session. The method name "getLastValidPageID" is miss leading.
        // It was used for another reason. Should be changed in the next version.
        //        String currentObjID = MCRLayoutUtilities.getLastValidPageID();

        String currentObjID = JPXMLFunctions.getLastValidPageID();
        if (currentObjID.equals("") || currentObjID.contains("_jpinst_") || currentObjID.contains("_person_")) {
            return "";
        }
        MCRObjectID mcrId;
        try {
            mcrId = MCRObjectID.getInstance(currentObjID);
        } catch (MCRException exc) {
            LOGGER.error("invalid MyCoRe ID " + currentObjID);
            return "";
        }
        String[] oldJournalID = (String[]) MCRSessionMgr.getCurrentSession().get("journalIDForObj");
        if (oldJournalID != null && oldJournalID.length == 2 && currentObjID.equals(oldJournalID[0])) {
            return oldJournalID[1];
        }
        Document objXML;
        try {
            objXML = MCRXMLMetadataManager.instance().retrieveXML(mcrId);
        } catch (Exception exc) {
            LOGGER.error("Unable to retrieve object " + mcrId, exc);
            return "";
        }
        if (objXML == null) {
            LOGGER.error("Unable to retrieve object " + mcrId);
            return "";
        }
        XPathExpression<Element> hiddenJournalIDXpath = XPathFactory.instance().compile(
            "/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID", Filters.element());
        Element hiddenJournalIDElement = hiddenJournalIDXpath.evaluateFirst(objXML);
        if (hiddenJournalIDElement == null) {
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

}
