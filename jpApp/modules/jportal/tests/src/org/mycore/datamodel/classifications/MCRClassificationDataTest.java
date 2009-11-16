package org.mycore.datamodel.classifications;

import java.util.List;

import junit.framework.TestCase;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.xpath.XPath;
import org.mycore.common.MCRConfiguration;
import org.mycore.datamodel.classifications2.MCRCategLinkService;

public class MCRClassificationDataTest extends TestCase {
    private MockHelper mockHelper = new MockHelper();
    
    public void testCreateXMLTree_closedFolder() throws Exception {
        MCRCategLinkService linkService = mockHelper.createLinkService();
        MCRClassificationPool classificationPoolMock = new MCRClassificationPool(mockHelper.createDAO(), linkService);
        MCRPermissionTool permissionTool = mockHelper.createPermissionTool();
        
        mockHelper.replay();
        
        String u = "/Test/withSubs/withSubs";
        String mode = "edit";
        String actclid = "rootID_withChildren";
        String actEditorCategid = "";
        
        RowCreator rowCreator_NoLines = new RowCreator_NoLines(classificationPoolMock, MCRConfiguration.instance());
        MCRClassificationData browserData_NoLines = new MCRClassificationData(u, mode, actclid, actEditorCategid, classificationPoolMock, permissionTool, rowCreator_NoLines);
        assertNotNull(browserData_NoLines);
        assertNotNull(browserData_NoLines.getClassification());
        
        Document data_NoLines = browserData_NoLines.createXmlTree("de");
        
        Attribute rowCount = (Attribute) XPath.selectSingleNode(data_NoLines, "//navigationtree/@rowcount");
        assertEquals("6", rowCount.getValue());
        
        Attribute folderWithSubs = (Attribute) XPath.selectSingleNode(data_NoLines, "//navigationtree/row/col[@plusminusbase='withSubs']/@folder1");
        assertEquals("folder_plus", folderWithSubs.getValue());

        List selectedNodesWithPlusMinus = XPath.selectNodes(data_NoLines, "//navigationtree/row/col[@plusminusbase]");
        assertEquals(1, selectedNodesWithPlusMinus.size());
        
        mockHelper.verify();
    }
    
    public void testCreateXMLTree_openedFolder() throws Exception {
        MCRCategLinkService linkService = mockHelper.createLinkService();
        MCRClassificationPool classificationPoolMock = new MCRClassificationPool(mockHelper.createDAO(), linkService);
        MCRPermissionTool permissionTool = mockHelper.createPermissionTool();
        
        mockHelper.replay();
        
        String u = "/Test/withSubs";
        String mode = "edit";
        String actclid = "rootID_withChildren";
        String actEditorCategid = "";
        
        RowCreator rowCreator_NoLines = new RowCreator_NoLines(classificationPoolMock, MCRConfiguration.instance());
        MCRClassificationData browserData_NoLines = new MCRClassificationData(u, mode, actclid, actEditorCategid, classificationPoolMock, permissionTool, rowCreator_NoLines);
        assertNotNull(browserData_NoLines);
        assertNotNull(browserData_NoLines.getClassification());
        
        Document data_NoLines = browserData_NoLines.createXmlTree("de");
        
        Attribute rowCount = (Attribute) XPath.selectSingleNode(data_NoLines, "//navigationtree/@rowcount");
        assertEquals("10", rowCount.getValue());
        
        Attribute folderWithSubs = (Attribute) XPath.selectSingleNode(data_NoLines, "//navigationtree/row/col[@plusminusbase='withSubs']/@folder1");
        assertEquals("folder_minus", folderWithSubs.getValue());
        
        List selectedNodes = XPath.selectNodes(data_NoLines, "//navigationtree/row/col[@lineLevel='2']");
        assertEquals(4, selectedNodes.size());
        
        List selectedNodesWithPlusMinus = XPath.selectNodes(data_NoLines, "//navigationtree/row/col[@plusminusbase]");
        assertEquals(1, selectedNodesWithPlusMinus.size());
        
//        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
//        outputter.output(data_NoLines, System.out);
        
        mockHelper.verify();
    }
}
