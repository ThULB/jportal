package fsu.jportal.access.test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.MessageFormat;
import java.util.Properties;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mycore.access.MCRAccessInterface;
import org.mycore.access.strategies.MCRAccessCheckStrategy;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRConstants;
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

    private static final String JOURNALID = "jportal_jpjournal_00000001";

    private AccessStrategyConfig strategyConfig;

    private MCRUserInformation userInfoMock;

    @Before
    public void init() {
        System.setProperty("MCR.Configuration.File", "config/test.properties");
        Properties mcrProperties = MCRConfiguration.instance().getProperties();
        mcrProperties.setProperty("MCR.Users.Superuser.UserName", ROOT);
        //        mcrProperties.setProperty("MCR.Access.Class", "fsu.jportal.access.test.FakeAccessImpl");
        mcrProperties.setProperty("MCR.Metadata.Type.jpjournal", "true");
        mcrProperties.setProperty("MCR.Metadata.Type.jpvolume", "true");
        mcrProperties.setProperty("MCR.Metadata.Type.jparticle", "true");
        mcrProperties.setProperty("MCR.Metadata.Type.derivate", "true");
        mcrProperties.setProperty("MCR.Metadata.Type.class", "true");
        mcrProperties.setProperty("MCR.Metadata.ObjectID.NumberPattern", "00000000");

        aclMock = createMock("aclMock", MCRAccessInterface.class);
        idStrategyMock = createMock("idStrategyMock", MCRAccessCheckStrategy.class);
        objTypeStrategyMock = createMock("objTypeStrategyMock", MCRAccessCheckStrategy.class);
        xmlMetaDataMgr = createMock("xmlMetaDataMgr", MCRXMLMetadataManager.class);
        strategyConfig = createMock("strategyConfig", AccessStrategyConfig.class);
        userInfoMock = createMock("userInfoMock", MCRUserInformation.class);
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
        reset(aclMock, idStrategyMock, objTypeStrategyMock, xmlMetaDataMgr, strategyConfig, userInfoMock);
    }

    @Test
    public void isSuperUser() throws Exception {
        expect(userInfoMock.getUserID()).andReturn(ROOT);
        replay(aclMock, userInfoMock);

        MCRSessionMgr.getCurrentSession().setUserInformation(userInfoMock);
        assertTrue("Superuser should has access", accessStrategy.checkPermission("foo", "perm"));
        verify(aclMock, userInfoMock);
    }

    @Test
    public void noSuperUser_ObjHasOwnRule_access() throws Exception {
        noSuperUser_ObjHasOwnRule("user", true, true, true);
    }

    @Test
    public void noSuperUser_ObjHasOwnRule_noaccess() throws Exception {
        noSuperUser_ObjHasOwnRule("user", true, false, false);
    }

    private void noSuperUser_ObjHasOwnRule(String userID, boolean hasRule, boolean hasAccess, boolean expectedAccess) {
        String id = "POOLPRIVILEGE";
        String permission = "read";
        expect(userInfoMock.getUserID()).andReturn(userID);
        expect(aclMock.hasRule(id, permission)).andReturn(hasRule);
        expect(aclMock.checkPermission(id, permission)).andReturn(hasAccess);
        replay(aclMock, userInfoMock);

        MCRSessionMgr.getCurrentSession().setUserInformation(userInfoMock);
        String errMsg = MessageFormat.format("User id: {0}, has rule: {1}, should has access: {2}.", userID, hasRule, expectedAccess);
        assertEquals(errMsg, expectedAccess, accessStrategy.checkPermission(id, permission));
        verify(aclMock, userInfoMock);
    }

    @Test
    public void noSuperUser_parentHasRule_access() throws Exception {
        String id = "jportal_derivate_00000001";
        String parentID = "jportal_jparticle_00000001";
        String permission = "read";
        String userID = "user";
        boolean hasRule = false;
        boolean hasParentRule = true;
        boolean hasAccess = true;
        boolean expectedAccess = true;

        expect(userInfoMock.getUserID()).andReturn(userID);
        expect(aclMock.hasRule("default_derivate", permission)).andReturn(false);
        expect(aclMock.hasRule("default", permission)).andReturn(false);
        expect(aclMock.hasRule(id, permission)).andReturn(hasRule);
        expect(aclMock.hasRule(parentID, permission)).andReturn(hasParentRule);
        expect(aclMock.checkPermission(parentID, permission)).andReturn(hasAccess);
        expect(xmlMetaDataMgr.exists(MCRObjectID.getInstance(id))).andReturn(true);

        MCRObjectID mcrObjectID = null;

        try {
            mcrObjectID = MCRObjectID.getInstance(id);
        } catch (Exception e) {
        }

        expect(xmlMetaDataMgr.retrieveXML(mcrObjectID)).andReturn(createderivateXML(parentID)).anyTimes();
        replay(aclMock, userInfoMock, xmlMetaDataMgr);

        MCRSessionMgr.getCurrentSession().setUserInformation(userInfoMock);
        String errMsg = MessageFormat.format("User id: {0}, has rule: {1}, should has access: {2}.", userID, hasRule, expectedAccess);
        assertEquals(errMsg, expectedAccess, accessStrategy.checkPermission(id, permission));
        verify(aclMock, userInfoMock, xmlMetaDataMgr);
    }
    
    @Test
    public void parentIsJournal_access() throws Exception {
        String id = "jportal_jpvolume_00058034";
        String parentID = "jportal_jpjournal_00000046";
        String permission = "read";
        String userID = "user";
        boolean hasRule = false;
        boolean hasParentRule = false;
        boolean hasAccess = true;
        boolean expectedAccess = true;
        
        expect(userInfoMock.getUserID()).andReturn(userID);
        expect(aclMock.hasRule(id, permission)).andReturn(hasRule);
        expect(aclMock.hasRule("default_jpvolume", permission)).andReturn(hasRule);
        expect(aclMock.hasRule("default", permission)).andReturn(hasRule);
        expect(xmlMetaDataMgr.exists(MCRObjectID.getInstance(id))).andReturn(true);
        
        MCRObjectID mcrObjectID = null;
        
        try {
            mcrObjectID = MCRObjectID.getInstance(id);
        } catch (Exception e) {
        }
        
        expect(xmlMetaDataMgr.retrieveXML(mcrObjectID)).andReturn(createObjectXML(parentID));
        
        expect(aclMock.hasRule(parentID, permission)).andReturn(hasParentRule);
//        expect(xmlMetaDataMgr.exists(MCRObjectID.getInstance(parentID))).andReturn(true);
        
        MCRObjectID journalID = null;
        
        try {
            journalID = MCRObjectID.getInstance(parentID);
        } catch (Exception e) {
        }
//        expect(xmlMetaDataMgr.retrieveXML(journalID)).andReturn(createJournalXML(parentID));
        // dont channge default_xxxxxx test still needed for editing objects
        expect(aclMock.hasRule("default_jpjournal", permission)).andReturn(true);
        expect(aclMock.checkPermission("default_jpjournal", permission)).andReturn(true);
        
        replay(aclMock, userInfoMock, xmlMetaDataMgr);
        
        MCRSessionMgr.getCurrentSession().setUserInformation(userInfoMock);
        String errMsg = MessageFormat.format("User id: {0}, has rule: {1}, should has access: {2}.", userID, hasRule, expectedAccess);
        assertEquals(errMsg, expectedAccess, accessStrategy.checkPermission(id, permission));
        verify(aclMock, userInfoMock, xmlMetaDataMgr);
    }

    @Test
    public void noSuperUser_journalHasRule_access() throws Exception {
        String id = "jportal_derivate_00000001";
        String parentID = "jportal_jparticle_00000001";
        String permission = "read";
        String userID = "user";
        boolean hasRule = false;
        boolean hasParentRule = false;
        boolean hasJournalRule = true;
        boolean hasAccess = true;
        boolean expectedAccess = true;

        expect(userInfoMock.getUserID()).andReturn(userID);
        expect(aclMock.hasRule("default_jparticle", permission)).andReturn(false);
        expect(aclMock.hasRule("default_derivate", permission)).andReturn(false);
        expect(aclMock.hasRule("default", permission)).andReturn(false).times(2);
        expect(aclMock.hasRule(id, permission)).andReturn(hasRule);
        expect(aclMock.hasRule(parentID, permission)).andReturn(hasParentRule);
        expect(aclMock.hasRule(JOURNALID, permission)).andReturn(hasJournalRule);
        expect(aclMock.checkPermission(JOURNALID, permission)).andReturn(hasAccess);

        MCRObjectID mcrObjectID = null;

        try {
            mcrObjectID = MCRObjectID.getInstance(id);
        } catch (Exception e) {
        }

        expect(xmlMetaDataMgr.retrieveXML(mcrObjectID)).andReturn(createderivateXML(parentID)).anyTimes();

        MCRObjectID mcrParentID = null;

        try {
            mcrParentID = MCRObjectID.getInstance(parentID);
        } catch (Exception e) {
        }

        expect(xmlMetaDataMgr.retrieveXML(mcrParentID)).andReturn(createObjectXML(JOURNALID)).anyTimes();
        expect(xmlMetaDataMgr.exists(MCRObjectID.getInstance(id))).andReturn(true);
        expect(xmlMetaDataMgr.exists(MCRObjectID.getInstance(parentID))).andReturn(true);
        replay(aclMock, userInfoMock, xmlMetaDataMgr);

        MCRSessionMgr.getCurrentSession().setUserInformation(userInfoMock);
        String errMsg = MessageFormat.format("User id: {0}, has rule: {1}, should has access: {2}.", userID, hasRule, expectedAccess);
        assertEquals(errMsg, expectedAccess, accessStrategy.checkPermission(id, permission));
        verify(aclMock, userInfoMock, xmlMetaDataMgr);
    }

    @Test
    public void isInEditorsGroupHasAccess() throws Exception {
        String id = JOURNALID;
        String permission = "create_volume";

        expect(userInfoMock.getUserID()).andReturn("user");
        expect(aclMock.hasRule(id, permission)).andReturn(false);
        expect(aclMock.hasRule("CRUD", permission)).andReturn(true);
        expect(aclMock.checkPermission("CRUD", permission)).andReturn(true);
        replay(aclMock, userInfoMock);

        MCRSessionMgr.getCurrentSession().setUserInformation(userInfoMock);
        assertTrue("Superuser should has access", accessStrategy.checkPermission(id, permission));
        verify(aclMock, userInfoMock);
    }

    @Test
    public void checkPermForClassificationHasRule() throws Exception {
        String id = "jportal_class_00000083";
        String permission = "writedb";
        boolean hasRule = true;
        boolean expectedAccess = false;

        expect(aclMock.hasRule(id, permission)).andReturn(hasRule);
        expect(aclMock.checkPermission(id, permission)).andReturn(false);
        replay(aclMock);

        String errMsg = MessageFormat.format("Check perm classi, has rule: {0}, should has access: {1}.", hasRule, expectedAccess);
        assertEquals(errMsg, expectedAccess, accessStrategy.checkPermission(id, permission));
        verify(aclMock);
    }

    @Test
    public void checkPermForClassificationNoRule() throws Exception {
        String id = "jportal_class_00000083";
        String permission = "writedb";
        boolean hasRule = false;
        boolean expectedAccess = false;

        expect(aclMock.hasRule(id, permission)).andReturn(hasRule);
        expect(aclMock.hasRule("default_class", permission)).andReturn(hasRule);
        expect(aclMock.hasRule("default", permission)).andReturn(hasRule);
        expect(xmlMetaDataMgr.exists(MCRObjectID.getInstance(id))).andReturn(false);
        replay(aclMock, xmlMetaDataMgr);

        String errMsg = MessageFormat.format("Check perm classi, has rule: {0}, should has access: {1}.", hasRule, expectedAccess);
        assertEquals(errMsg, expectedAccess, accessStrategy.checkPermission(id, permission));
        verify(aclMock, xmlMetaDataMgr);
    }

    @Test
    public void checkAbitaryID() throws Exception {
        String id = "fsu.jportal.resources.ClassificationResource";
        String permission = "/auth_GET";
        
        boolean hasRule = false;
        boolean expectedAccess = false;

        expect(aclMock.hasRule(id, permission)).andReturn(hasRule);
        expect(aclMock.hasRule("default", permission)).andReturn(hasRule);
        replay(aclMock, xmlMetaDataMgr);

        String errMsg = MessageFormat.format("Check perm no MCRObjectID, has rule: {0}, should has access: {1}.", hasRule, expectedAccess);
        assertEquals(errMsg, expectedAccess, accessStrategy.checkPermission(id, permission));
        verify(aclMock, xmlMetaDataMgr);
    }

    private Document createderivateXML(String id) {
        Element rootElement = new Element("mycorederivate");
        Element metadata = new Element("derivate");
        Element hiddenIDs = new Element("linkmetas");
        Element hiddenID = new Element("linkmeta");
        hiddenID.setAttribute("href", id, MCRConstants.XLINK_NAMESPACE);
        hiddenIDs.addContent(hiddenID);
        metadata.addContent(hiddenIDs);
        rootElement.addContent(metadata);
        return new Document(rootElement);
    }

    private Document createObjectXML(String id) {
        Element rootElement = new Element("mycoreobject");
        Element metadata = new Element("metadata");
        Element hiddenIDs = new Element("hidden_jpjournalsID");
        Element hiddenID = new Element("hidden_jpjournalID");
        hiddenID.addContent(id);
        hiddenIDs.addContent(hiddenID);
        metadata.addContent(hiddenIDs);
        rootElement.addContent(metadata);
        return new Document(rootElement);
    }
    
    private Document createJournalXML(String id) {
        Element rootElement = new Element("mycoreobject");
        Element metadata = new Element("metadata");
        rootElement.addContent(metadata);
        return new Document(rootElement);
    }
}
