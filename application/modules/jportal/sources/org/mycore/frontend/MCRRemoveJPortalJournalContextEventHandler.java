package org.mycore.frontend;

import org.apache.log4j.Logger;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

public class MCRRemoveJPortalJournalContextEventHandler extends MCREventHandlerBase {
    private static Logger LOGGER = Logger.getLogger(MCRRemoveJPortalJournalContextEventHandler.class);
    
    @Override
    protected void handleObjectDeleted(MCREvent evt, MCRObject obj) {
        MCRObjectID objectID = obj.getId();
        String objType = objectID.getTypeId();
        
        if (objType.equals("jpjournal")){
            LOGGER.info("Removing ACL for journal \"" + objectID + "\" ...");
            MCRAccessManager.removeAllRules(objectID);
            LOGGER.info("ACL for journal \"" + objectID + "\" removed successfully.");
            MCRJPortalJournalContextForWebpages.removeContext(obj);
        }
    }
    
    @Override
    protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        MCRObjectID objectID = obj.getId();
        String objType = objectID.getTypeId();
        
        if (objType.equals("jpjournal")){
            MCRJPortalJournalContextForWebpages.updateContext(obj);
            LOGGER.info("Handle update \"" + objectID + ".");
        }
    }
}
