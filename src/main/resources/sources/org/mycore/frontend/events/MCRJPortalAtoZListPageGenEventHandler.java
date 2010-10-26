package org.mycore.frontend.events;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.frontend.pagegeneration.JournalListManager;

/**
 * This EventHandler generates the AtoZ list if a journal is added,
 * deleted or updated. The data is stored to the journalList.xml file.
 * @author Matthias Eichner
 */
public class MCRJPortalAtoZListPageGenEventHandler extends MCREventHandlerBase {

    private static final Logger LOGGER = Logger.getLogger(MCRJPortalAtoZListPageGenEventHandler.class);

    @Override
    protected void handleObjectCreated(MCREvent evt, MCRObject obj) {
        // no journal, no new build of the xml file
        if(!isJournal(obj))
            return;
        try {
            LOGGER.info("Create entry in A-Z list for " + obj.getId());
            JournalListManager.instance().createJournalLists();
        } catch(Exception e) {
            LOGGER.error(e);
        }
    }

    @Override
    protected void handleObjectDeleted(MCREvent evt, MCRObject obj) {
        // no journal, no new build of the xml file
        if(!isJournal(obj))
            return;
        try {
            LOGGER.info("Delete entry in A-Z list for " + obj.getId());
            JournalListManager.instance().createJournalLists();
        } catch(Exception e) {
            LOGGER.error(e);
        }
    }

    @Override
    protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        // no journal, no new build of the xml file
        if(!isJournal(obj))
            return;
        try {
            LOGGER.info("Updating entry in A-Z list for " + obj.getId());
            JournalListManager.instance().createJournalLists();
        } catch(Exception e) {
            LOGGER.error(e);
        }
    }

    /**
     * Checks if MCRObject is a journal.
     * @param the MCRObject to test
     * @return true if the MCRObject an journal, otherwise false
     */
    public boolean isJournal(MCRObject obj) {
        Document doc = obj.createXML();
        String id = doc.getRootElement().getAttributeValue("ID");
        if(id.contains("journal"))
            return true;
        return false;
    }

}