package fsu.jportal.urn;

import java.nio.file.Files;
import java.util.List;

import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRFileMetadata;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectDerivate;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.urn.hibernate.MCRURN;
import org.mycore.urn.services.MCRURNManager;

public class URNTools {
    public static void updateURNFileName(MCRURN urn, String path, String newName) {
        boolean registered = true;

        if (path != null && !"".equals(path.trim())) {
            urn.setPath(path);
            registered = false;
        }

        if (newName != null && !"".equals(newName.trim())) {
            urn.setFilename(newName);
            registered = false;
        }

        if (!registered) {
            urn.setRegistered(false);
            MCRURNManager.update(urn);

            MCRObjectID id = MCRObjectID.getInstance(urn.getId());
            MCRDerivate derivate = MCRMetadataManager.retrieveMCRDerivate(id);

            MCRObjectDerivate objectDerivate = derivate.getDerivate();
            for (MCRFileMetadata mcrFileMetadata : objectDerivate.getFileMetadata()) {
                String urnStr = mcrFileMetadata.getUrn();
                if (urnStr != null && urnStr.equals(urn.getURN())) {
                    mcrFileMetadata.setName(urn.getPath() + urn.getFilename());
                }
            }
            MCRMetadataManager.updateMCRDerivateXML(derivate);
        }
    }

    public static MCRURN getURNForFile(MCRPath file) {
        MCRObjectID derivID = MCRObjectID.getInstance(file.getOwner());
        // we need a method like MCRURNManager.get(MCRFile file) return MCRURN without a loop
        List<MCRURN> urnList = MCRURNManager.get(derivID);
        for (MCRURN urn : urnList) {
            String path = urn.getPath() + urn.getFilename();
            if (path.equals("/" + file.subpathComplete().toString())) {
                return urn;
            }
        }

        return null;
    }

    public static void updateURN(MCRPath sourceNode, MCRPath target) {
        if(!Files.exists(sourceNode) || !Files.exists(target)){
            return;
        }
        
        MCRURN urn = getURNForFile(sourceNode);
        if(urn == null){
            return;
        }
        
        String targetName = target.getFileName().toString();
        String targetPath = target.getParent().getOwnerRelativePath();
        if(!targetPath.endsWith("/")){
            targetPath += "/";
        }
        
        updateURNFileName(urn, targetPath, targetName);
    }
}
