package fsu.jportal.gson;

import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFilesystemNode;

public class FileNodeWraper {
    private String maindoc;
    private MCRFilesystemNode[] children;
    private MCRFilesystemNode node;
    private boolean isDir = false;
    
    public FileNodeWraper(MCRFilesystemNode node, String maindoc) {
        if(node instanceof MCRDirectory){
            setChildren(((MCRDirectory) node).getChildren());
            setDir(true);
        } else {
            setChildren(new MCRFilesystemNode[]{});
        }
        
        setNode(node);
        setMainDoc(maindoc);
    }

    public String getMaindoc() {
        return maindoc;
    }

    public void setMainDoc(String maindoc) {
        this.maindoc = maindoc;
    }

    public MCRFilesystemNode[] getChildren() {
        return children;
    }

    public void setChildren(MCRFilesystemNode[] children) {
        this.children = children;
    }

    public MCRFilesystemNode getNode() {
        return node;
    }

    public void setNode(MCRFilesystemNode node) {
        this.node = node;
    }

    public boolean isDir() {
        return isDir;
    }

    public void setDir(boolean isDir) {
        this.isDir = isDir;
    }
    
}
