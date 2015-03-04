package org.mycore.frontend;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

import fsu.jportal.common.xml.MCRJPortalURIGetJournalID;

public class MCRJPortalLayoutTemplateDetector {

    private final static Logger LOGGER = Logger.getLogger(MCRJPortalLayoutTemplateDetector.class);

    private final static String KEY_PREFIX = "MCR.Module-JPortal.DynamicLayoutTemplates.";

    public static String getTemplateID(String id) {
        MCRObjectID mcrId = MCRObjectID.getInstance(id);
        MCRObject obj = MCRMetadataManager.retrieveMCRObject(mcrId);
        MCRMetaElement me = obj.getMetadata().getMetadataElement("hidden_jpjournalsID");
        if (me != null && me.size() == 1) {
            MCRMetaLangText metaText = (MCRMetaLangText) me.getElement(0);
            return getJournalTemplateID(metaText.getText());
        }
        return getTemplateID();
    }

    public static String getTemplateID() {
        // get id of current watched journal
        String journalID = MCRJPortalURIGetJournalID.getID();

        if (journalID.equals("")) {
            LOGGER.debug("Journal-ID cannot be calculated, return ''");
            return "";
        }
        return getJournalTemplateID(journalID);
    }

    private static String getJournalTemplateID(String journalID) {
        // get "date-from" of journal
        Document objXML;
        try {
            objXML = MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(journalID));
        } catch (Exception exc) {
            LOGGER.error("Unable to get journal " + journalID, exc);
            return "";
        }
        Integer dateOfJournal = 0;
        Element dateNode = null;
        XPathExpression<Element> xpath = XPathFactory.instance().compile(
                "/mycoreobject/metadata/dates/date[@type='published_from' or @type='published']", Filters.element());
        dateNode = xpath.evaluateFirst(objXML);
        if (dateNode == null) {
            LOGGER.error("No /mycoreobject/metadata/dates/date[@type='published_from'] can be found, return empty string.");
            return "";
        }
        dateOfJournal = Integer.valueOf(dateNode.getTextTrim());

        // get template
        MCRConfiguration mcrConfig = MCRConfiguration.instance();
        String template = "";
        int pos = 1;
        int numberOfTemplates = mcrConfig.getPropertiesMap(KEY_PREFIX + "yearFrom").size();
        while (pos <= numberOfTemplates) {
            // get from and until dates
            int dateFrom = mcrConfig.getInt(KEY_PREFIX + "yearFrom." + Integer.toString(pos));
            int dateUntil = 0;
            if (pos < numberOfTemplates)
                dateUntil = mcrConfig.getInt(KEY_PREFIX + "yearFrom." + Integer.toString(pos + 1)) - 1;
            else
                dateUntil = 10000;
            // date with template found ?
            if ((dateOfJournal >= dateFrom) && (dateOfJournal <= dateUntil)) {
                template = mcrConfig.getString(KEY_PREFIX + "name." + Integer.toString(pos));
            }
            pos++;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Calculated template=" + template + " for date=" + dateOfJournal);
        }
        return template;
    }

}
