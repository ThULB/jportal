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

    private MCRAccessCheckStrategy objTypeStrategyMock;

    private static final String JOURNALID = "jportal_jpjournal_000000001";

    private AccessStrategyConfig strategyConfig;

    @Before
    public void init() {
        System.setProperty("MCR.Configuration.File", "config/test.properties");
        Properties mcrProperties = MCRConfiguration.instance().getProperties();
        mcrProperties.setProperty("MCR.Users.Superuser.UserName", ROOT);
        mcrProperties.setProperty("MCR.Access.Class", "fsu.jportal.access.test.FakeAccessImpl");
        mcrProperties.setProperty("MCR.Metadata.Type.jpjournal", "true");
        mcrProperties.setProperty("MCR.Metadata.Type.foo", "true");

        aclMock = createMock("ACLMock",MCRAccessInterface.class);
        idStrategyMock = createMock("IDStrat", MCRAccessCheckStrategy.class);
        objTypeStrategyMock = createMock("ObjType",MCRAccessCheckStrategy.class);
        xmlMetaDataMgr = createMock(MCRXMLMetadataManager.class);
        strategyConfig = createMock(AccessStrategyConfig.class);
        expect(strategyConfig.getAccessInterface()).andReturn(aclMock).anyTimes();
        expect(strategyConfig.getXMLMetadataMgr()).andReturn(xmlMetaDataMgr).anyTimes();
        expect(strategyConfig.getAccessCheckStrategy(AccessStrategyConfig.OBJ_ID_STRATEGY)).andReturn(idStrategyMock).anyTimes();
        expect(strategyConfig.getAccessCheckStrategy(AccessStrategyConfig.OBJ_TYPE_STRATEGY)).andReturn(objTypeStrategyMock).anyTimes();
        accessStrategy = new AccessStrategy(strategyConfig);
        replay(strategyConfig);
    }

    @After
    public void cleanUp() {
        MCRSessionMgr.getCurrentSession().setUserInformation(MCRSystemUserInformation.getGuestInstance());
        reset(aclMock,idStrategyMock,objTypeStrategyMock,xmlMetaDataMgr,strategyConfig);
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
        String permission = "write";
        isValidID(objid, permission, true, true);
        isValidID(objid, permission, false, false);
        isValidID(null, permission, false, false);
        isValidID(objid, null, false, false);
        isValidID(null, null, false, false);
        isValidID("", permission, false, false);
    }
    
    private void isValidID(String objid, String permission, boolean objTypePerm, boolean expectedPermission) {
        reset(objTypeStrategyMock);
        
        expect(objTypeStrategyMock.checkPermission(objid, permission)).andReturn(objTypePerm).anyTimes();
        replay(objTypeStrategyMock);

        String errMsg = MessageFormat.format("isValidID - isJPID case: objTypePerm = {0}, expected result {1}. ", objTypePerm, expectedPermission);
        assertEquals(errMsg + permission, expectedPermission, accessStrategy.checkPermission(objid, permission));
        verify(objTypeStrategyMock);
    }
    
    @Test
    public void isValidIDNoJiID() throws Exception {
        String id = "jportal_foo_000000001";
        String permission = "write";
        
        isValidIDNoJpID(id, permission, true, false, false);
        isValidIDNoJpID(id, permission, true, true, true);
        isValidIDNoJpID(id, permission, false, true, false);
        isValidIDNoJpID(id, permission, false, false, false);
        isValidIDNoJpID(null, permission, false, false, false);
        isValidIDNoJpID(id, null, false, false, false);
        isValidIDNoJpID(null, null, false, false, false);
    }

    private void isValidIDNoJpID(String id, String permission, boolean parentPerm, boolean typePerm, boolean expectedPermission) {
        reset(idStrategyMock, xmlMetaDataMgr,objTypeStrategyMock);
        expect(idStrategyMock.checkPermission(JOURNALID, permission)).andReturn(parentPerm).anyTimes();
        expect(objTypeStrategyMock.checkPermission(id, permission)).andReturn(typePerm).anyTimes();
        MCRObjectID mcrObjectID = null;
        
        try {
            mcrObjectID = MCRObjectID.getInstance(id);
        } catch (Exception e) {
        }
        
        expect(xmlMetaDataMgr.retrieveXML(mcrObjectID)).andReturn(createObjectXML()).anyTimes();
        replay(idStrategyMock, xmlMetaDataMgr,objTypeStrategyMock);
        
        String errMsg = MessageFormat.format("isValidID - noJPID case: parentPerm = {0}, typePerm = {1}, expected result {2}. ", parentPerm, typePerm, expectedPermission);
        assertEquals(errMsg + permission, expectedPermission, accessStrategy.checkPermission(id, permission));
        verify(idStrategyMock, xmlMetaDataMgr,objTypeStrategyMock);
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
