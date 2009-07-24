package org.mycore.frontend;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.xml.MCRJPortalURIGetJournalID;
import org.mycore.datamodel.common.MCRXMLTableManager;
import org.mycore.datamodel.metadata.MCRObjectID;

public class MCRJPortalLayoutTemplateDetector {

    private final static Logger LOGGER = Logger.getLogger(MCRJPortalLayoutTemplateDetector.class);

    private final static String KEY_PREFIX = "MCR.Module-JPortal.DynamicLayoutTemplates.";

    private static HashMap<String, String> CACHE = new HashMap<String, String>();

    public static String getTemplateID() {

        // get id of current watched journal
        String journalID = MCRJPortalURIGetJournalID.getID();

        if (journalID.equals("")) {
            LOGGER.debug("Journal-ID cannot be calculated, return ''");
            return "";
        }

        // exist in cache ?
        String cacheKey = getCacheKey(journalID);
        if (!CACHE.isEmpty() && CACHE.containsKey(cacheKey)) {
            String template = (String) CACHE.get(cacheKey);
            LOGGER.debug("Calculated template=" + template + " for journal=" + journalID + ", taken from CACHE");
            return template;
        }

        // get "date-from" of journal
        Document objXML = MCRXMLTableManager.instance().readDocument(new MCRObjectID(journalID));
        org.jdom.xpath.XPath xpath = null;
        Integer dateOfJournal = 0;
        Element dateNode = null;
        try {
            xpath = org.jdom.xpath.XPath.newInstance("/mycoreobject/metadata/dates/date[@type='published_from']");
            dateNode = (Element) xpath.selectSingleNode(objXML);
            dateOfJournal = Integer.valueOf(dateNode.getTextTrim());
        } catch (Exception e) {
            LOGGER.debug("No /mycoreobject/metadata/dates/date[@type='published_from'] can be found, return ''");
            e.printStackTrace();
            return "";
        }
        
        // get template
        MCRConfiguration mcrConfig = MCRConfiguration.instance();
        String template = "";
        int pos = 1;
        int numberOfTemplates = mcrConfig.getProperties(KEY_PREFIX + "yearFrom").size();
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
                // put to cache
                CACHE.put(journalID, template);
            }
            pos++;
        }

        LOGGER.debug("Calculated template=" + template + " for date=" + dateOfJournal);
        return template;
    }

    private static String getCacheKey(String journalID) {
        return journalID.trim();
    }
}
