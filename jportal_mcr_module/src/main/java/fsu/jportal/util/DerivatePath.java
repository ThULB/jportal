package fsu.jportal.util;

import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFilesystemNode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class DerivatePath {
    // TODO: check for wrong path syntax, derivatID must not be null, parentPath ist null for root
    static final Pattern pathPattern = Pattern.compile("(.*):((/(.*/)*)(.*)$){1}");
    
    String derivateID;

    String parentPath;

    String fileName;

    String absPath;

    /**
     * @param path "derivate_id:/path/to/file"
     */
    public DerivatePath(String path) {
        parsePath(path);
    }

    private String removeSlashAtEnd(String path) {
        if(path.length() > 1 && path.endsWith("/")){
            path = path.substring(0, path.length()-1);
        }
        
        return path;
    }
    
    private void parsePath(String path) {
        Matcher pathMatcher = pathPattern.matcher(path);
        while (pathMatcher.find()) {
            derivateID = pathMatcher.group(1);
            absPath = removeSlashAtEnd(pathMatcher.group(2));
            parentPath = removeSlashAtEnd(pathMatcher.group(3));
            
            fileName = pathMatcher.group(5);
        }
        
        if(parentPath.equals(absPath)){
            parentPath = null;
        }
        
        if(fileName.equals("")){
            fileName = null;
        }
    }

    public String getDerivateID() {
        return derivateID;
    }

    public String getAbsolutePath() {
        return absPath;
    }
    
    public String getParentPath() {
        return parentPath;
    }
    
    public DerivatePath getParent() {
        return new DerivatePath(getDerivateID() + ":" + getParentPath());
    }

    public String getFileName() {
        return fileName;
    }
    
    public MCRFilesystemNode toFileNode(){
        MCRDirectory rootNode = (MCRDirectory) MCRFilesystemNode.getRootNode(getDerivateID());
        if(rootNode == null){
            return null;
        }
        
        return rootNode.getChildByPath(getAbsolutePath());
    }
}