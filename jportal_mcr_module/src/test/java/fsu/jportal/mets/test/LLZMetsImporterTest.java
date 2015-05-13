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
import org.mycore.datamodel.metadata.*;

import java.io.InputStream;

/**
 * Created by chi on 02.04.15.
 */
@RunWith(JMockit.class)
public class LLZMetsImporterTest {
    @Mocked MCRSystemUserInformation sysInfo;

    @Injectable MCRObjectID derivID;

    @Injectable MCRObjectID objID;

    @Injectable MCRObjectID ownerID;

    @Test
    @Ignore
    public void testLLZImport() throws Exception {
        new Expectations() {{
            sysInfo.getUserID();
            result = "root";
            objID.getTypeId();
            result = "jpvolume";
        }};

        new Expectations() {
            MCRObjectID nextID;

            {
                MCRObjectID.getNextFreeId("jportal_jpvolume");
                result = nextID;
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

//        new MockUp<MCRObjectID>() {
//            MCRObjectID objID;
//
//            @Mock
//            void $clinit() {
//            }
//
//            @Mock
//            void $init(final Invocation inv) {
//
//            }
//
//            @Mock
//            public MCRObjectID getInstance(Invocation invocation, String id) {
//                System.out.println("get ID: " + id);
//                return invocation.getInvokedInstance();
//            }
//
//            @Mock
//            public String getTypeId() {
//                System.out.println("getTypeID");
//                return "foo";
//            }
//
//            @Mock
//            public synchronized MCRObjectID getNextFreeId(String base_id) {
//                return nextID;
//            }
//        };

        new MockUp<MCRDerivate>() {
            @Mock
            MCRObjectID getOwnerID() {
                return ownerID;
            }
        };

        new MockUp<MCRObject>() {
            @Mock
            void $init() {
            }

            @Mock
            public MCRObjectID getId() {
                System.out.println("getID");
                return objID;
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
                return new MCRObject();
            }

            @Mock
            public void update(final MCRObject mcrObject) {

            }
        };

        LLZMetsImporter metsImporter = new LLZMetsImporter();
        InputStream xmlStream = getClass().getResourceAsStream("/mets/mets.xml");
        Document metsXML = new SAXBuilder().build(xmlStream);
        metsImporter.importMets(metsXML, derivID);
    }
}

