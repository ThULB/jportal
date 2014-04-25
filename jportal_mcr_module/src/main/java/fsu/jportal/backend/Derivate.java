package fsu.jportal.backend;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFile;
import org.mycore.datamodel.ifs.MCRFileMetadataManager;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRFileMetadata;
import org.mycore.datamodel.metadata.MCRMetaIFS;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectDerivate;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.imagetiler.MCRImage;
import org.mycore.iview2.services.MCRIView2Tools;

import com.google.common.io.Files;

public class Derivate {
    private static Logger LOGGER = Logger.getLogger(Derivate.class);
    private MCRDerivate derivate;
    private MCRDirectory rootDir;
    private String derivID;

    public Derivate(String derivID) {
        this.derivID = derivID;
    }
    
    public String getMaindoc(){
        return getDerivate().getDerivate().getInternals().getMainDoc();
    }
    
    public void setMaindoc(String path){
        getDerivate().getDerivate().getInternals().setMainDoc(path);
        updateDerivate();
    }

    private void updateDerivate() {
        MCRMetadataManager.updateMCRDerivateXML(getDerivate());
    }
    
    public MCRFilesystemNode getChildByPath(String path) {
        MCRDirectory rootDir = getRootDir();
        if(rootDir == null){
            return null;
        }
        
        return rootDir.getChildByPath(path);
    }
    
    private MCRDerivate getDerivate() {
        if(derivate == null){
            MCRObjectID mcrid = MCRObjectID.getInstance(derivID);
            setDerivate(MCRMetadataManager.retrieveMCRDerivate(mcrid));
        }
        
        return derivate;
    }

    private void setDerivate(MCRDerivate derivate) {
        this.derivate = derivate;
    }

    public MCRDirectory getRootDir() {
        if(rootDir == null){
            setRootDir(MCRDirectory.getRootDirectory(derivID));
        }
        
        return rootDir;
    }

    private void setRootDir(MCRDirectory derivFileDir) {
        this.rootDir = derivFileDir;
    }
}
