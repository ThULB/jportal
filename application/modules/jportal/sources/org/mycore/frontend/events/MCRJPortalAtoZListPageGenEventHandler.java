package org.mycore.frontend.events;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectService;
import org.mycore.frontend.pagegeneration.MCRJPortalAtoZListPageGenerator;

/**
 * This EventHandler generates the AtoZ list if a journal is added,
 * deleted or updated. The data is stored to the journalList.xml file.
 */
public class MCRJPortalAtoZListPageGenEventHandler extends MCREventHandlerBase {

    private static final Logger LOGGER = Logger.getLogger(MCRJPortalAtoZListPageGenEventHandler.class);
    private enum Action {INSERT, REMOVE};

    @Override
    protected void handleObjectCreated(MCREvent evt, MCRObject obj) {
        // no journal, no new build of the xml file
        if(!isJournal(obj))
            return;
        try {
            updateAtoZList(obj, Action.INSERT);
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
            updateAtoZList(obj, Action.REMOVE);
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
            if(isDeletedFlagSet(obj))
                updateAtoZList(obj, Action.REMOVE);
            else
                updateAtoZList(obj, Action.INSERT);
        } catch(Exception e) {
            LOGGER.error(e);
        }
    }

    /**
     * Synchronized method to load, manipulate and save back the A to Z list.
     * @param obj the modified mcr object
     * @param action insert or delete the object
     * @throws Exception
     */
    private synchronized void updateAtoZList(MCRObject obj, Action action) throws Exception {
        MCRJPortalAtoZListPageGenerator pageGen = new MCRJPortalAtoZListPageGenerator();
        // try to load the journal list from the webapps folder.
        try {
            pageGen.loadJournalList();
        } catch(Exception e) {
            // error occured while loading from file -> create the a to z list manually
            pageGen.createJournalList();
        }
        if(action == Action.INSERT)
            pageGen.insertJournalToTree(obj);
        else if(action == Action.REMOVE)
            pageGen.removeJournalFromJournalList(obj);
        pageGen.saveJournalList();
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

    /**
     * Checks if the deleted flag of an mcrobject is set.
     * @param obj the mcr object
     * @return returns true if the flag is set, otherwise false
     */
    public boolean isDeletedFlagSet(MCRObject obj) {
        MCRObjectService service = obj.getService();
        if(service.isFlagSet("deleted"))
            return true;
        return false;
    }

}