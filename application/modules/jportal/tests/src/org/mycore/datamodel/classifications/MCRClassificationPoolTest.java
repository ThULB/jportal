package org.mycore.datamodel.classifications;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;

import junit.framework.TestCase;

import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryID;

public class MCRClassificationPoolTest extends TestCase {
    private MCRClassificationPool classiPool;
    private MockHelper categoryDAOMock = new MockHelper();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        classiPool = new MCRClassificationPool(categoryDAOMock.createDAO(), categoryDAOMock.createLinkService());
        assertNotNull(classiPool);
    }

    @Override
    protected void tearDown() throws Exception {
        categoryDAOMock.reset();
        super.tearDown();
    }

    public void testGetAllIDs() throws Exception {
        categoryDAOMock.replay();
        Set<MCRCategoryID> allIDs = classiPool.getAllIDs();
        assertNotNull(allIDs);
        assertEquals(categoryDAOMock.ROOTCATEG_SIZE, allIDs.size());

        for (int i = 0; i < categoryDAOMock.ROOTCATEG_SIZE; i++) {
            assertTrue(allIDs.contains(MCRCategoryID.rootID("rootID_" + i)));
        }
        categoryDAOMock.verify();
    }

    public void testUpdateClassification() throws Exception {
        MCRCategory classification = categoryDAOMock.createMCRCategory("rootID", "", new LinkedList<MCRCategory>());
        MCRCategory fakeCategory = categoryDAOMock.createMCRCategory("rootID", "fakeCateg", new LinkedList<MCRCategory>());
        categoryDAOMock.replay();
        
        classification.getChildren().add(fakeCategory);
        
        classiPool.updateClassification(classification);
        Set<MCRCategoryID> allIDs = classiPool.getAllIDs();

        assertTrue(allIDs.contains(MCRCategoryID.rootID("rootID")));

        classiPool.updateClassification(fakeCategory);
        classiPool.getMovedCategories().add(fakeCategory.getId());
        classiPool.saveAll();
        categoryDAOMock.verify();
    }
}
