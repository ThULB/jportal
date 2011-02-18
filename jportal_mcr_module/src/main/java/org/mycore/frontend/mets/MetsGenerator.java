package org.mycore.frontend.mets;

import javax.activation.MimetypesFileTypeMap;

import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFileContentTypeFactory;
import org.mycore.datamodel.ifs.MCRFileNodeServlet;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.files.FLocat;
import org.mycore.mets.model.files.File;
import org.mycore.mets.model.files.FileGrp;
import org.mycore.mets.model.files.FileSec;

public class MetsGenerator {

    private static String imageBaseUrl;
    
    static {
        StringBuffer url = new StringBuffer();
        url.append(MCRFileNodeServlet.getBaseURL());
        url.append("servlets/MCRFileNodeServlet/");
        imageBaseUrl = url.toString();
    }

    public Mets generate(MCRDerivate mcrDer) {
        // get mycore object
        MCRObjectID objId = mcrDer.getDerivate().getMetaLink().getXLinkHrefID();
        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(objId);
        // create the mets
        return createMets(mcrObj, mcrDer);
    }

    private Mets createMets(MCRObject obj, MCRDerivate der) {
        Mets mets = new Mets();

//        mets.addAmdSec(section)
//        mets.addDmdSec(section)
        mets.setFileSec(createFileSec(der));
//        mets.setLogicalStructMap(lsm)
//        mets.setPysicalStructMap(psm)
//        mets.setStructLink(structLink);
        
        return mets;
    }

    private FileSec createFileSec(MCRDerivate der) {
        FileSec fsec = new FileSec();
        FileGrp fgroup = new FileGrp(FileGrp.USE_DEFAULT);
        MCRFilesystemNode root = MCRFilesystemNode.getRootNode(der.getId().toString());
        MCRDirectory dir = root.getRootDirectory();

        for(MCRFilesystemNode mcrFile : dir.getChildren()) {
            String id = mcrFile.getID();
            String path = mcrFile.getAbsolutePath();
            
//            MCRFileContentTypeFactory.getType(mcrFile.getContentTypeID()).getMimeType()
            
            String mimeType = new MimetypesFileTypeMap().getContentType(path);
            File metsFile = new File(id, mimeType);
            metsFile.setFLocat(new FLocat(FLocat.LOCTYPE_URL, path));
            fgroup.addFile(metsFile);
        }         
        return fsec;
    }
    
}
