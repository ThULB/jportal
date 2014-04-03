package fsu.jportal.gson;

import java.lang.reflect.Type;

import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFilesystemNode;

public class FileNodeWraper {
    private FileNodeWraper() {
    }
    
    private String maindoc;
    private Class type;
    private MCRFilesystemNode[] children;
    
    public static FileNodeWraper newInstance(MCRDirectory node, String maindoc){
        MCRFilesystemNode[] children = node.getChildren();
        return new FileNodeWraper();
    }
    
    public static FileNodeWraper newInstance(MCRFilesystemNode node){
        return new FileNodeWraper();
    }
    
    public Type getType(){
        return null;
    }

    public FileNodeWraper(MCRDirectory dir, String derivID) {
//        this.setDir(dir);
        this.setDerivID(derivID);
    }

    public String getDerivID() {
        return maindoc;
    }

    public void setDerivID(String derivID) {
        this.maindoc = derivID;
    }

    public MCRDirectory getDir() {
        return null;
    }

    public void setDir(MCRDirectory dir) {
//        this.dir = dir;
    }
    
}
