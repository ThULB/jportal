package fsu.jportal.urn;

import java.util.List;

import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRFileMetadata;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectDerivate;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.urn.hibernate.MCRURN;
import org.mycore.urn.services.MCRURNManager;

public class URNTools {
    public static void updateURNFileName(MCRURN urn, String newName) {
        urn.setFilename(newName);
        urn.setRegistered(false);
        MCRURNManager.update(urn);
        
        MCRObjectID id = MCRObjectID.getInstance(urn.getId());
        MCRDerivate derivate = MCRMetadataManager.retrieveMCRDerivate(id);

        MCRObjectDerivate objectDerivate = derivate.getDerivate();
        for (MCRFileMetadata mcrFileMetadata : objectDerivate.getFileMetadata()) {
            String urnStr = mcrFileMetadata.getUrn();
            if(urnStr != null && urnStr.equals(urn.getURN())){  
                mcrFileMetadata.setName(urn.getPath() + urn.getFilename());
            }
        }
        MCRMetadataManager.updateMCRDerivateXML(derivate);
    }

    public static MCRURN getURNForFile(MCRFilesystemNode file) {
        MCRObjectID derivID = MCRObjectID.getInstance(file.getOwnerID());
        // we need a method like MCRURNManager.get(MCRFile file) return MCRURN without a loop
        List<MCRURN> urnList = MCRURNManager.get(derivID);
        for (MCRURN urn : urnList) {
            String path = urn.getPath() + urn.getFilename();
            if(path.equals(file.getAbsolutePath())){
                return urn;
            }
        }
        
        return null;
    }
}
