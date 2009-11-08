package org.mycore.common.xml;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;

public class MCRJPortalURIGetAllClassIDsTest extends TestCase {
    public void testResolveElement() throws Exception {
        MCRCategoryDAO dao = EasyMock.createMock(MCRCategoryDAO.class);
        
        MCRJPortalURIGetAllClassIDs resolver = new MCRJPortalURIGetAllClassIDs(dao);
//        resolver.resolveElement(uri)
        fail("Not Implemented yet.");
    }
}
