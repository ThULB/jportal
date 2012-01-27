package fsu.jportal.metadata.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.JDOMSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mycore.common.MCRConfiguration;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.ifs2.MCRMetadataStore;
import org.mycore.datamodel.ifs2.MCRStoreManager;
import org.mycore.datamodel.ifs2.MCRVersioningMetadataStore;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.xml.sax.SAXException;

import fsu.jportal.metadata.RubricLabel;
import fsu.jportal.metadata.XMLMetaElement;
import fsu.xml.SchemaValidator;


public class RubricLabelTest {
    @Before
    public void init() {
        System.setProperty("MCR.Configuration.File", "config/test.properties");
        Properties mcrProperties = MCRConfiguration.instance().getProperties();
        mcrProperties.setProperty("MCR.Metadata.Type.jpclassi", "true");
        mcrProperties.setProperty("MCR.Metadata.Store.BaseDir", "/tmp");
        mcrProperties.setProperty("MCR.Metadata.Store.SVNBase", "/tmp/versions");
        mcrProperties.setProperty("MCR.IFS2.Store.jportal_jpclassi.ForceXML", "true");
        mcrProperties.setProperty("MCR.IFS2.Store.jportal_jpclassi.BaseDir", "ram:///tmp");
        mcrProperties.setProperty("MCR.IFS2.Store.jportal_jpclassi.SlotLayout", "4-2-2");
        mcrProperties.setProperty("MCR.IFS2.Store.jportal_jpclassi.SVNRepositoryURL", "ram:///tmp");
        
        try {
            MCRStoreManager.createStore("jportal_jpclassi", MCRMetadataStore.class);
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @After
    public void cleanUp(){
        MCRStoreManager.removeStore("jportal_jpclassi");
    }
    
    @Test
    public void creatingRubricObjAndValidating() throws Exception {
        XMLMetaElement<RubricLabel> rubric = new XMLMetaElement<RubricLabel>("rubric");
        rubric.addMetaElemEntry(new RubricLabel("de", "Rubriken Test fuer MyCoRe", "test de"));
        rubric.addMetaElemEntry(new RubricLabel("de", "Rubric test for MyCoRe", "test en"));
        
        MCRObject mcrObj = createMCRObj();
        mcrObj.getMetadata().setMetadataElement(rubric.toMCRMetaElement());

        String dataModelLocation = "/datamodel-jpclassi.xsd";
        try {
            SchemaValidator schemaValidator = new SchemaValidator(dataModelLocation);
            JDOMSource source = new JDOMSource(mcrObj.createXML());
            schemaValidator.validate(source);
        } catch (SAXException e) {
            e.printStackTrace();
            fail("Schema validation failed for " + dataModelLocation);
        }
        
        MCRXMLMetadataManager.instance().create(mcrObj.getId(), mcrObj.createXML(), new Date());
    }
    
    private void xmlOutput(Document mcrObjXML) throws IOException {
        assertNotNull(mcrObjXML);
        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        xmlOutputter.output(mcrObjXML, System.out);
    }
    
    private MCRObject createMCRObj() {
        MCRObject mcrObject = new MCRObject();
        MCRObjectID mcrObjectID = MCRObjectID.getNextFreeId("jportal_jpclassi");
        mcrObject.setId(mcrObjectID);
        mcrObject.setLabel(mcrObjectID.toString());
        mcrObject.setSchema("datamodel-jpclassi.xsd");
        return mcrObject;
    }
}
