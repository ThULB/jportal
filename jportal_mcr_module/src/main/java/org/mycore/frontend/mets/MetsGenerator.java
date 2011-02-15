package org.mycore.frontend.mets;

import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mets.model.Mets;

public class MetsGenerator {

    public void generate(MCRDerivate mcrDer) {
        // get mycore object
        MCRObjectID objId = mcrDer.getDerivate().getMetaLink().getXLinkHrefID();
        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(objId);
        
        createMets(mcrObj, mcrDer);
    }
    
    private void createMets(MCRObject obj, MCRDerivate der) {
        Mets mets = new Mets();

//        mets.addAmdSec(section)
//        mets.addDmdSec(section)
//        
//        mets.setFileSec(fileSec)
//        mets.setLogicalStructMap(lsm)
//        mets.setPysicalStructMap(psm)
//        mets.setStructLink(structLink);
    }
    
}
