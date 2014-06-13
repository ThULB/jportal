package fsu.jportal.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DerivatePath {
    static final Pattern pathPattern = Pattern.compile("(.*):((/(.*/)*)(.*)$){1}");

    String ownerID;

    String directoryPath;

    String resourceName;

    String absPath;

    private boolean isDirectory;

    public DerivatePath(String path) {
        if(path.endsWith("/")){
            setDirectory(true);
            path.substring(0, path.length());
        }
        parsePath(path);
    }

    private void parsePath(String path) {
        Matcher pathMatcher = pathPattern.matcher(path);
        while (pathMatcher.find()) {
            ownerID = pathMatcher.group(1);
            absPath = pathMatcher.group(2);
            directoryPath = pathMatcher.group(3);
            resourceName = pathMatcher.group(5);
        }
    }

    public String getOwnerID() {
        return ownerID;
    }

    public String getAbsolutePath() {
        return absPath;
    }
    
    public String getDirectoryPath() {
        return directoryPath;
    }

    public String getFileName() {
        return resourceName;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    private void setDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
    }

}