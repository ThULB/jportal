package org.mycore.frontend.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileLocation {
    static final Pattern locationPattern = Pattern.compile("/(jportal_\\w*_[0-9]{1,8})((/.*)*/(.*)$)?");

    String ownerID;

    String path;

    String fileName;

    String absPath;

    public FileLocation(String oldFile) {
        parseOwnerIDAndPath(oldFile);
    }

    private void parseOwnerIDAndPath(String oldFile) {
        Matcher locationMatcher = locationPattern.matcher(oldFile);
        while (locationMatcher.find()) {
            ownerID = locationMatcher.group(1);
            absPath = locationMatcher.group(2);
            path = locationMatcher.group(3);
            fileName = locationMatcher.group(4);
        }
    }

    public String getOwnerID() {
        return ownerID;
    }

    public String getAbsolutPath() {
        return absPath;
    }
    
    public String getPath() {
        return path;
    }

    public String getFileName() {
        return fileName;
    }

}