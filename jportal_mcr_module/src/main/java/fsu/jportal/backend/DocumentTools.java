package fsu.jportal.backend;

import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.MCRDerivateCommands;
import org.mycore.frontend.cli.MCRObjectCommands;
import org.mycore.frontend.jersey.MCRJerseyUtil;

public class DocumentTools {

    public static int delete(String documentID) {
        MCRObjectID mcrId = MCRJerseyUtil.getID(documentID);
        if(MCRMetadataManager.exists(mcrId)) {
            try {
                if (mcrId.getTypeId().equals("derivate")) {
                    MCRDerivate mcrDer = MCRMetadataManager.retrieveMCRDerivate(mcrId);
                    MCRMetadataManager.delete(mcrDer);
                    return 1; //OK
                } else {
                    MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(mcrId);
                    MCRMetadataManager.delete(mcrObj);
                    return 1; //OK
                }
            } catch (MCRActiveLinkException e) {
                e.printStackTrace();
                return 0; //ERROR
            }       
        }
        return 2; //NOT_FOUND
    }
    
    public static boolean move(String documentID, String targetID) {
        if (!documentID.trim().equals(targetID.trim())) {
            try {
                if (documentID.contains("derivate")) {
                    MCRDerivateCommands.linkDerivateToObject(documentID, targetID);
                    return true;
                } else {
                    MCRObjectCommands.replaceParent(documentID, targetID);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
}
