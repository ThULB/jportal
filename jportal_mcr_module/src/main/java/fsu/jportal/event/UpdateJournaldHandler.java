package fsu.jportal.event;

import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.common.MCRLinkTableManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

import static fsu.jportal.util.ImprintUtil.getImprintID;

public class UpdateJournaldHandler extends MCREventHandlerBase {
    @Override
    protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        MCRObjectID mcrId = obj.getId();
        String mcrIdString = mcrId.toString();
        if ("jpjournal".equals(mcrId.getTypeId())) {
            MCRLinkTableManager ltm = MCRLinkTableManager.instance();
            String imprintID = getImprintID(mcrIdString, "imprint");
            if (imprintID != null) {
                ltm.addReferenceLink(mcrIdString, imprintID, "imprint", null);
            }
            String partnerID = getImprintID(mcrIdString, "partner");
            if (partnerID != null) {
                ltm.addReferenceLink(mcrIdString, partnerID, "partner", null);
            }
        }
    }
}