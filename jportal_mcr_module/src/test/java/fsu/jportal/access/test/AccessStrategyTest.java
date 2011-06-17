package fsu.jportal.access.test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.text.MessageFormat;
import java.util.Properties;

import org.jdom.Document;
import org.jdom.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mycore.access.MCRAccessInterface;
import org.mycore.access.strategies.MCRAccessCheckStrategy;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.common.MCRUserInformation;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

import fsu.jportal.access.AccessStrategy;
import fsu.jportal.access.AccessStrategyConfig;

public class AccessStrategyTest {
    private static final String ROOT = "root";

    private AccessStrategy accessStrategy;

    private MCRAccessInterface aclMock;

    private MCRAccessCheckStrategy idStrategyMock;

    private MCRXMLMetadataManager xmlMetaDataMgr;

    private static final String JOURNALID = "jportal_jpjournal_000000001";

    @Before
    public void init() {
        System.setProperty("MCR.Configuration.File", "config/test.properties");
        Properties mcrProperties = MCRConfiguration.instance().getProperties();
        mcrProperties.setProperty("MCR.Users.Superuser.UserName", ROOT);
        mcrProperties.setProperty("MCR.Access.Class", "fsu.jportal.access.test.FakeAccessImpl");
        mcrProperties.setProperty("MCR.Metadata.Type.jpjournal", "true");
        mcrProperties.setProperty("MCR.Metadata.Type.foo", "true");

        aclMock = createMock(MCRAccessInterface.class);
        idStrategyMock = createMock(MCRAccessCheckStrategy.class);
        xmlMetaDataMgr = createMock(MCRXMLMetadataManager.class);
        AccessStrategyConfig strategyConfig = createMock(AccessStrategyConfig.class);
        expect(strategyConfig.getAccessInterface()).andReturn(aclMock).anyTimes();
        expect(strategyConfig.getXMLMetadataMgr()).andReturn(xmlMetaDataMgr).anyTimes();
        expect(strategyConfig.getAccessCheckStrategy(AccessStrategyConfig.OBJ_ID_STRATEGY)).andReturn(idStrategyMock);
        accessStrategy = new AccessStrategy(strategyConfig);
        replay(strategyConfig);
    }

    @After
    public void cleanUp() {
        MCRSessionMgr.getCurrentSession().setUserInformation(MCRSystemUserInformation.getGuestInstance());
    }

    @Test
    public void checkRootUser() throws Exception {
        MCRUserInformation userInfoMock = createMock(MCRUserInformation.class);
        expect(userInfoMock.getCurrentUserID()).andReturn(ROOT);
        replay(aclMock,userInfoMock);

        MCRSessionMgr.getCurrentSession().setUserInformation(userInfoMock);
        assertTrue("Superuser should has access", accessStrategy.checkPermission("foo", "perm"));
        verify(aclMock,userInfoMock);
    }

    @Test
    public void readDerivateNoRule() throws Exception {
        String id = "id";
        String permission = "read-derivates";

        expect(aclMock.hasRule(id, permission)).andReturn(false);
        replay(aclMock);

        assertTrue("Permission should be true if there is no such rule " + permission, accessStrategy.checkPermission(id, permission));
        verify(aclMock);
    }
    
    @Test
    public void readDerivateHasRule() throws Exception {
        String id = "id";
        String permission = "read-derivates";
        
        expect(aclMock.hasRule(id, permission)).andReturn(true);
        expect(idStrategyMock.checkPermission(id, permission)).andReturn(false);
        replay(aclMock,idStrategyMock);
        
        assertFalse("Permission should be false " + permission, accessStrategy.checkPermission(id, permission));
        verify(aclMock,idStrategyMock);
    }

    @Test
    public void readDerivatePermTrue() throws Exception {
        String id = "id";
        String permission = "read-derivates";

        expect(aclMock.hasRule(id, permission)).andReturn(true);
        expect(idStrategyMock.checkPermission(id, permission)).andReturn(true);
        replay(aclMock, idStrategyMock);

        assertTrue("Permission should be true " + permission, accessStrategy.checkPermission(id, permission));
        verify(aclMock, idStrategyMock);
    }

    @Test
    public void readDerivatePermFalse() throws Exception {
        String id = "id";
        String permission = "read-derivates";

        expect(aclMock.hasRule(id, permission)).andReturn(true);
        expect(idStrategyMock.checkPermission(id, permission)).andReturn(false);
        replay(aclMock, idStrategyMock);

        assertFalse("Permission should be false " + permission, accessStrategy.checkPermission(id, permission));
        verify(aclMock, idStrategyMock);
    }

    @Test
    public void isValidID() throws Exception {
        String objid = JOURNALID;
        isValidID(true, false, true, false,objid);
        isValidID(true, true, true, true,objid);
        isValidID(true, false, false, false,objid);
        isValidID(true, true, false, true,objid);
        isValidID(false, false, true, true,objid);
        isValidID(false, true, true, true,objid);
        isValidID(false, false, false, false,objid);
        isValidID(false, true, false, false,objid);
    }
    
    private void isValidID(boolean hasRule, boolean defaultIDPerm, boolean defaultPerm, boolean expectedPermission, String objid) {
        reset(aclMock, idStrategyMock);
        String defaultID = "default_jpjournal";
        String defaultVal = "default";
        String permission = "write";
        expect(aclMock.hasRule(defaultID, permission)).andReturn(hasRule).anyTimes();
        expect(aclMock.checkPermission(defaultID, permission)).andReturn(defaultIDPerm).anyTimes();
        expect(aclMock.checkPermission(defaultVal, permission)).andReturn(defaultPerm).anyTimes();
        replay(aclMock, idStrategyMock);

        String errMsg = MessageFormat.format("isValidID - isJPID case: hasRule = {0}, defaultIDPerm = {1}, defaultPerm = {2}, expected result {3}. ", hasRule, defaultIDPerm, defaultPerm, expectedPermission);
        assertEquals(errMsg + permission, expectedPermission, accessStrategy.checkPermission(objid, permission));
        verify(aclMock, idStrategyMock);
    }
    
    @Test
    public void notValidIDParentNoAccess() throws Exception {
        String id = "jportal_foo_000000001";
        String permission = "write";

        expect(idStrategyMock.checkPermission(JOURNALID, permission)).andReturn(false);
        expect(xmlMetaDataMgr.retrieveXML(MCRObjectID.getInstance(id))).andReturn(createObjectXML());
        replay(aclMock, idStrategyMock, xmlMetaDataMgr);

        assertFalse("Permission should be false " + permission, accessStrategy.checkPermission(id, permission));
        verify(aclMock, idStrategyMock, xmlMetaDataMgr);
    }
    
    @Test
    public void notValidIDParentAccess() throws Exception {
        String id = "jportal_foo_000000001";
        String defaultid = "default_foo";
        String permission = "write";
        
        expect(idStrategyMock.checkPermission(JOURNALID, permission)).andReturn(true);
        expect(aclMock.hasRule(defaultid, permission)).andReturn(true);
        expect(aclMock.checkPermission(defaultid, permission)).andReturn(false);
        expect(xmlMetaDataMgr.retrieveXML(MCRObjectID.getInstance(id))).andReturn(createObjectXML());
        replay(aclMock, idStrategyMock, xmlMetaDataMgr);
        
        assertFalse("Permission should be false " + permission, accessStrategy.checkPermission(id, permission));
        verify(aclMock, idStrategyMock, xmlMetaDataMgr);
    }

    private Document createObjectXML() {
        Element rootElement = new Element("mycoreobject");
        Element metadata = new Element("metadata");
        Element hiddenIDs = new Element("hidden_jpjournalsID");
        Element hiddenID = new Element("hidden_jpjournalID");
        hiddenID.addContent(JOURNALID);
        hiddenIDs.addContent(hiddenID);
        metadata.addContent(hiddenIDs);
        rootElement.addContent(metadata);
        return new Document(rootElement);
    }
}
