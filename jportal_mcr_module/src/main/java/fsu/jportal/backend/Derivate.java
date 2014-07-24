package fsu.jportal.backend;

import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

public class Derivate {
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
