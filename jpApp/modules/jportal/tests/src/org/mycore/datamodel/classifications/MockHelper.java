/**
 * 
 */
package org.mycore.datamodel.classifications;

import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.easymock.IMocksControl;
import org.mycore.datamodel.classifications2.MCRCategLinkService;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;

class MockHelper {
    static final int CHILD_SIZE = 4;

    static final int ROOTCATEG_SIZE = 4;

    private IMocksControl mockControl = createControl();

    public void reset() {
        getMockControl().reset();
    }

    public MCRCategory createMCRCategory(String rootID, String id, List<MCRCategory> childList, Integer level) {
        MCRCategory categoryMock = getMockControl().createMock(MCRCategory.class);
        expect(categoryMock.getId()).andReturn(new MCRCategoryID(rootID, id)).anyTimes();
        expect(categoryMock.getChildren()).andReturn(childList).anyTimes();
        expect(categoryMock.getLabels()).andReturn(getLabelsList()).anyTimes();
        expect(categoryMock.getLabel("de")).andReturn((MCRLabel) getLabelsList().toArray()[0]).anyTimes();
        expect(categoryMock.getLabel("en")).andReturn((MCRLabel) getLabelsList().toArray()[1]).anyTimes();
        expect(categoryMock.getLevel()).andReturn(level).anyTimes();
        expect(categoryMock.hasChildren()).andReturn(!childList.isEmpty()).anyTimes();
        return categoryMock;
    }

    public LinkedList<MCRCategory> childList(String rootID, int size, String childIDPrefix, Integer level) {
        LinkedList<MCRCategory> linkedList = new LinkedList<MCRCategory>();
        for (int i = 0; i < size; i++) {
            linkedList.add(createMCRCategory(rootID, childIDPrefix + i, Collections.EMPTY_LIST, level));
        }
        
        return linkedList;
    }

    private Collection<MCRLabel> getLabelsList() {
        LinkedList<MCRLabel> labels = new LinkedList<MCRLabel>();
        String[] lang = {"de", "en"};
        for (int i = 0; i < lang.length; i++) {
            labels.add(new MCRLabel(lang[i], "text_" + lang[i], "descr_" + lang[i]));
        }
        
        return labels;
    }

    public MCRCategoryDAO createDAO() {
        String rootID = "rootID_withChildren";
        int level = 0;
        int level2 = level + 1;
        LinkedList<MCRCategory> childList = childList(rootID, CHILD_SIZE, "child_", level2);
        childList.add(createMCRCategory(rootID, "withSubs", childList(rootID, CHILD_SIZE, "subChild_", level2 + 1), level2));
        childList.add(createMCRCategory(rootID, "underwithSubs", Collections.EMPTY_LIST, level2));
        MCRCategory categoryMock = createMCRCategory(rootID, "", childList, level);
        
        MCRCategoryDAO categoryDAOMock = getMockControl().createMock(MCRCategoryDAO.class);
        expect(categoryDAOMock.getRootCategoryIDs()).andReturn(createCategoryIDMockList(ROOTCATEG_SIZE)).anyTimes();
        expect(categoryDAOMock.exist(isA(MCRCategoryID.class))).andReturn(false).anyTimes();
        
        categoryDAOMock.addCategory(isA(MCRCategoryID.class), isA(MCRCategory.class));
        expectLastCall().anyTimes();
        
        categoryDAOMock.addCategory((MCRCategoryID) isNull(), isA(MCRCategory.class));
        expectLastCall().anyTimes();
        
        expect(categoryDAOMock.getCategory(isA(MCRCategoryID.class), anyInt())).andReturn(categoryMock).anyTimes();
        return categoryDAOMock;
    }

    public List<MCRCategoryID> createCategoryIDMockList(int size) {
        ArrayList<MCRCategoryID> idList = new ArrayList<MCRCategoryID>();
        for (int i = 0; i < size; i++) {
            idList.add(MCRCategoryID.rootID("rootID_" + i));
        }

        return idList;
    }

    public MCRCategLinkService createLinkService() {
        MCRCategLinkService linkServiceMock = getMockControl().createMock(MCRCategLinkService.class);
        expect(linkServiceMock.getLinksFromCategory(isA(MCRCategoryID.class))).andReturn(getLinkList()).anyTimes();
        expect(linkServiceMock.hasLinks(isA(MCRCategory.class))).andReturn(getLinkBoolMap()).anyTimes();
        expect(linkServiceMock.hasLink(isA(MCRCategory.class))).andReturn(false).anyTimes();
        return linkServiceMock;
    }

    private Map<MCRCategoryID, Boolean> getLinkBoolMap() {
        Map<MCRCategoryID, Boolean> boolMap = new HashMap<MCRCategoryID, Boolean>(){
            @Override
            public Boolean get(Object key) {
                // TODO Auto-generated method stub
                return false;
            }
        };
        return boolMap;
    }

    private Collection<String> getLinkList() {
        LinkedList<String> linkList = new LinkedList<String>();
        for (int i = 0; i < 4; i++) {
            linkList.add("objLink_" + i);
        }
        return linkList;
    }

    public void replay() {
        getMockControl().replay();
    }

    public void verify() {
        getMockControl().verify();
    }

    private void setMockControl(IMocksControl mockControl) {
        this.mockControl = mockControl;
    }

    private IMocksControl getMockControl() {
        return mockControl;
    }

    public MCRPermissionTool createPermissionTool() {
        MCRPermissionTool permissionToolMock = getMockControl().createMock(MCRPermissionTool.class);
        expect(permissionToolMock.checkPermission(isA(String.class))).andReturn(true).anyTimes();
        expect(permissionToolMock.checkPermission(isA(String.class), isA(String.class))).andReturn(true).anyTimes();
        return permissionToolMock;
    }
    
    
}