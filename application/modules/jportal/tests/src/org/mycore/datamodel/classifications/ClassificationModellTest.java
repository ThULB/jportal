package org.mycore.datamodel.classifications;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.IMocksControl;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.mycore.datamodel.classifications2.MCRCategLinkService;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;

public class ClassificationModellTest extends TestCase {
    private IMocksControl mockControl = createControl();
    
    public void testListClassifications() throws Exception {
        MCRCategoryDAO categoryDAOMock = createDAOMock();
        mockControl.replay();
        
        List<MCRCategory> rootCategories = categoryDAOMock.getRootCategories();
        
        assertNotNull(rootCategories);
        assertEquals(4, rootCategories.size());
        
        int i = 0;
        for (MCRCategory mcrCategory : rootCategories) {
            assertEquals(MCRCategoryID.rootID("rootID" + i), mcrCategory.getId());
            i++;
        }
        
        MCRCategLinkService linkServiceMock = mockControl.createMock(MCRCategLinkService.class);
//        expect(linkServiceMock.hasLinks(category))
        
        ClassificationModell modell = new ClassificationModell(categoryDAOMock, linkServiceMock);
        Element classifications = modell.getClassifications();
        
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        outputter.output(classifications, System.out);
        mockControl.verify();
    }

    private MCRCategoryDAO createDAOMock() {
        MCRCategoryDAO categoryDAOMock = mockControl.createMock(MCRCategoryDAO.class);
        List<MCRCategory> rootCategoryListMock = rootCategoryList(4);
        expect(categoryDAOMock.getRootCategories()).andReturn(rootCategoryListMock).atLeastOnce();
        return categoryDAOMock;
    }

    private List<MCRCategory> rootCategoryList(int size) {
        ArrayList<MCRCategory> arrayList = new ArrayList<MCRCategory>();
        
        for (int i = 0; i < size; i++) {
            MCRCategory categoryMock = mockControl.createMock(MCRCategory.class);
            expect(categoryMock.getId()).andReturn(MCRCategoryID.rootID("rootID" + i)).atLeastOnce();
            expect(categoryMock.getCurrentLabel()).andReturn(new MCRLabel("de", "rootLabel" + i, "rootLabel Descr" + i)).atLeastOnce();
            arrayList.add(categoryMock);
        }
        return arrayList;
    }
}
