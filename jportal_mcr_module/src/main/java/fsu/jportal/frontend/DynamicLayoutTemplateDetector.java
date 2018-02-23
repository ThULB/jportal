package fsu.jportal.frontend;

import java.time.temporal.ChronoField;
import java.util.Optional;

import fsu.jportal.backend.JPJournal;
import fsu.jportal.backend.JPPeriodicalComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.metadata.JPMetaDate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectUtils;

public class DynamicLayoutTemplateDetector {

    private final static Logger LOGGER = LogManager.getLogger(DynamicLayoutTemplateDetector.class);

    private final static String KEY_PREFIX = "MCR.Module-JPortal.DynamicLayoutTemplates.";

    public static String getTemplateID(String id) {
        try {
            MCRObjectID mcrId = MCRObjectID.getInstance(id);
            MCRObject obj = MCRMetadataManager.retrieveMCRObject(mcrId);
            MCRObject journal = obj;
            if (!journal.getId().getTypeId().equals("jpjournal")) {
                journal = MCRObjectUtils.getRoot(obj);
            }
            return getJournalTemplateID(journal.getId().toString());
        } catch (Exception exc) {
            LOGGER.error("Something went wrong while getting the template id for " + id, exc);
            // return default template
            return "template_default";
        }
    }

    private static String getJournalTemplateID(String journalID) {
        JPJournal journal = new JPJournal(journalID);
        // collect all inherited == 0 dates
        Optional<JPMetaDate> published = journal.getDate(JPPeriodicalComponent.DateType.published);
        // get the from date
        // is there a from date?
        if (!published.isPresent()) {
            throw new MCRException(
                "No /mycoreobject/metadata/dates/date[@type='published'] can be found, return empty string.");
        }
        Integer yearOfJournal = published.get().getDateOrFrom().get(ChronoField.YEAR);
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
            if ((yearOfJournal >= dateFrom) && (yearOfJournal <= dateUntil)) {
                template = mcrConfig.getString(KEY_PREFIX + "name." + Integer.toString(pos));
            }
            pos++;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Calculated template=" + template + " for date=" + yearOfJournal);
        }
        return template;
    }

}
