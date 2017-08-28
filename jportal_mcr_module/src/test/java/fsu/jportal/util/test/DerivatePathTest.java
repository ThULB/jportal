package fsu.jportal.util.test;

import fsu.jportal.util.DerivatePath;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DerivatePathTest {
    @Test
    public void justOwnerID() {
        String derivateID = "jportal_derivate_00000024";
        String absPath = "/";
        String path = derivateID + ":" + absPath;
        DerivatePath filePath = new DerivatePath(path);
        
        assertPath(derivateID, null, null, absPath, filePath);
    }
    
    @Test
    public void oneChildLevel() {
        String derivateID = "jportal_derivate_00000024";
        String parentPath = "/";
        String fileName = "level1";
        String absPath = parentPath + "/" + fileName;
        String path = derivateID + ":" + absPath;
        DerivatePath filePath = new DerivatePath(path);
        
        assertPath(derivateID, parentPath, fileName, absPath, filePath);
    }

    
    @Test
    public void twoChildLevel() {
        String derivateID = "jportal_derivate_00000024";
        String parentPath = "/level1/";
        String fileName = "level2";
        String absPath = parentPath + "/" + fileName;
        String path = derivateID + ":" + absPath;
        DerivatePath filePath = new DerivatePath(path);
        
        assertPath(derivateID, parentPath, fileName, absPath, filePath);
    }
    
    @Test
    public void threeChildLevel() {
        String derivateID = "jportal_derivate_00000024";
        String parentPath = "/level1/level2";
        String fileName = "level3";
        String absPath = parentPath + "/" + fileName;
        String path = derivateID + ":" + absPath;
        DerivatePath filePath = new DerivatePath(path);
        
        DerivatePath parent = filePath.getParent();
        
        assertPath(derivateID, parentPath, fileName, absPath, filePath);
    }

    private void assertPath(String derivateID, String parentPath, String fileName, String absPath, DerivatePath filePath) {
        assertEquals("Derivate ID", derivateID, filePath.getDerivateID());
        assertEquals("Absolute path", absPath, filePath.getAbsolutePath());
        assertEquals("Parent path", parentPath, filePath.getParentPath());
        assertEquals("File name", fileName, filePath.getFileName());
    }
}
