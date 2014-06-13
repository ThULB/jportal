package fsu.jportal.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class DerivatePath {
    static final Pattern pathPattern = Pattern.compile("(.*):((/(.*/)*)(.*)$){1}");
    
    String derivateID;

    String parentPath;

    String fileName;

    String absPath;

    /**
     * @param path "derivate_id:/path/to/file"
     */
    public DerivatePath(String path) {
        if(path.endsWith("/")){
            path.substring(0, path.length());
        }
        
        parsePath(path);
    }
    
    private void parsePath(String path) {
        Matcher pathMatcher = pathPattern.matcher(path);
        while (pathMatcher.find()) {
            derivateID = pathMatcher.group(1);
            absPath = pathMatcher.group(2);
            parentPath = pathMatcher.group(3);
            
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

    public String getFileName() {
        return fileName;
    }
}