package fsu.jportal.gson;

import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFilesystemNode;

public class FileNodeWraper {
    private String maindoc;
    private MCRFilesystemNode[] children;
    private MCRFilesystemNode node;
    
    public FileNodeWraper(MCRFilesystemNode node, String maindoc) {
        if(node instanceof MCRDirectory){
            setChildren(((MCRDirectory) node).getChildren());
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
    
}
