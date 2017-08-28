package fsu.jportal.frontend;

import fsu.jportal.backend.JPJournal;
import fsu.jportal.backend.JPPeriodicalComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.metadata.*;

import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
        List<MCRMetaISO8601Date> dates = StreamSupport.stream(journal.getDates().spliterator(), false)
            .filter(d -> d.getInherited() == 0).collect(Collectors.toList());
        // get the from date
        Optional<MCRMetaISO8601Date> from = StreamSupport.stream(dates.spliterator(), false)
            .filter(d -> d.getType().equals(JPPeriodicalComponent.DateType.published.name())
                || d.getType().equals(JPPeriodicalComponent.DateType.published_from.name()))
            .findFirst();
        // is there a from date?
        if (!from.isPresent()) {
            throw new MCRException(
                "No /mycoreobject/metadata/dates/date[@type='published_from'] can be found, return empty string.");
        }
        Integer yearOfJournal = Year.from(from.get().getMCRISO8601Date().getDt()).getValue();
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
