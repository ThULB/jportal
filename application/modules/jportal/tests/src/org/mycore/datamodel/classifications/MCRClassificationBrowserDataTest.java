package org.mycore.datamodel.classifications;

import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.mycore.datamodel.classifications2.MCRCategLinkService;

import junit.framework.TestCase;

public class MCRClassificationBrowserDataTest extends TestCase {
    private MockHelper mockHelper = new MockHelper();
    public void testCreateXMLTree() throws Exception {
        MCRCategLinkService linkService = mockHelper.createLinkService();
        MCRClassificationPool classificationPoolMock = new MCRClassificationPool(mockHelper.createDAO(), linkService);
        MCRPermissionTool permissionTool = mockHelper.createPermissionTool();
        
        mockHelper.replay();
        
        String u = "/Test/withSubs/withSubs";
        String mode = "edit";
        String actclid = "rootID_withChildren";
        String actEditorCategid = "";
        MCRClassificationBrowserData browserData = new MCRClassificationBrowserData(u, mode, actclid, actEditorCategid, classificationPoolMock, permissionTool, linkService);
        assertNotNull(browserData);
        assertNotNull(browserData.getClassification());
        
        Document createXmlTree = browserData.createXmlTree("de");
        
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        outputter.output(createXmlTree, System.out);
        mockHelper.verify();
    }
}
