package fsu.jportal.mets.test;

import static org.junit.Assert.*;

import fsu.jportal.mets.LLZMetsImporter;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.metadata.*;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by chi on 02.04.15.
 */
@RunWith(JMockit.class)
public class LLZMetsImporterTest {

    @Test
    @Ignore
    public void testLLZImport(@Mocked final MCRObjectID derivID, @Mocked final MCRObjectID ownerID,
        @Mocked final MCRObjectID objID, @Mocked final MCRSystemUserInformation sysInfo) throws Exception {

        new Expectations() {{
            sysInfo.getUserID();
            result = "root";
            objID.getTypeId(); result ="jpvolume";
        }};

        new MockUp<MCRMetaElement>() {
            @Mock
            void $clinit() {
            }
        };

        new MockUp<MCRMetaDefault>() {
            @Mock
            void $clinit() {
            }
        };

        new MockUp<MCRBase>() {
            @Mock
            void $clinit() {
            }
        };

        new MockUp<MCRObject>() {
            @Mock
            void $init(Invocation inv) {
            }

            @Mock
            public MCRObjectID getId(Invocation inv) {
                System.out.println("obj getID");
                return objID;
            }
        };

        new MockUp<MCRDerivate>() {
            @Mock
            void $init() {

            }

            @Mock
            public MCRObjectID getOwnerID() {
                return ownerID;
            }
        };

        new MockUp<MCRMetadataManager>() {
            @Mock
            void $clinit() {

            }

            @Mock
            public MCRDerivate retrieveMCRDerivate(MCRObjectID id) {
                System.out.println("retrieveMCRDerivate");
                return new MCRDerivate();
            }

            @Mock
            public MCRObject retrieveMCRObject(MCRObjectID id) {
                System.out.println("retrieveMCRObject");
                return new MCRObject();
            }

            @Mock
            public void update(final MCRObject mcrObject) {

            }
        };

        LLZMetsImporter metsImporter = new LLZMetsImporter();
        InputStream xmlStream = getClass().getResourceAsStream("/mets/mets.xml");
        Document metsXML = new SAXBuilder().build(xmlStream);
        metsImporter.importMets(metsXML, MCRObjectID.getInstance("derivID"));
    }

    @Test
    public void testMCRObjID(@Mocked final MCRObjectID ownerID, @Mocked final MCRObjectID objID) throws Exception {
        new Expectations() {{
            MCRObjectID.getInstance("ownerID");
            result = ownerID;
            MCRObjectID.getInstance("objID");
            result = objID;
            objID.getTypeId();
            result = "obj";
        }};

        MCRObjectID foo = MCRObjectID.getInstance("ownerID");
        MCRObjectID obj = MCRObjectID.getInstance("objID");
        assertNotNull(obj);
        String objType = obj.getTypeId();
        assertEquals("obj", objType);
        assertFalse("obj".equals(foo.getTypeId()));
    }
}

