package fsu.jportal.event;

import static fsu.jportal.util.ImprintUtil.getImprintID;

import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.common.MCRLinkTableManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

public class UpdateJournaldHandler extends MCREventHandlerBase {
    @Override
    protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        MCRObjectID mcrId = obj.getId();
        String mcrIdString = mcrId.toString();
        if ("jpjournal".equals(mcrId.getTypeId())) {
            MCRLinkTableManager ltm = MCRLinkTableManager.instance();
            String imprintID = getImprintID(mcrIdString, "imprint");
            if (imprintID != null && !imprintID.trim().isEmpty()) {
                ltm.addReferenceLink(mcrIdString, imprintID, "imprint", null);
            }
            String partnerID = getImprintID(mcrIdString, "partner");
            if (partnerID != null && !partnerID.trim().isEmpty()) {
                ltm.addReferenceLink(mcrIdString, partnerID, "partner", null);
            }
            String greetingID = getImprintID(mcrIdString, "greeting");
            if (greetingID != null && !greetingID.trim().isEmpty()) {
                ltm.addReferenceLink(mcrIdString, greetingID, "greeting", null);
            }
        }
    }
}
