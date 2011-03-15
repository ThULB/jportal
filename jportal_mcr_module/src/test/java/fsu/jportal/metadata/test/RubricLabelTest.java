package fsu.jportal.metadata.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.JDOMSource;
import org.junit.Before;
import org.junit.Test;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.xml.sax.SAXException;

import fsu.jportal.metadata.RubricLabel;
import fsu.jportal.metadata.XMLMetaElement;
import fsu.xml.SchemaValidator;


public class RubricLabelTest {
    @Before
    public void init() {
        System.setProperty("MCR.Metadata.Type.classi", "true");
    }
    
    @Test
    public void creatingRibricObjAndValidating() throws Exception {
        XMLMetaElement rubric = new XMLMetaElement("rubric");
        rubric.addMetaElemEntry(new RubricLabel("de", "Rubriken Test fuer MyCoRe", "test de"));
        rubric.addMetaElemEntry(new RubricLabel("de", "Rubric test for MyCoRe", "test en"));
        
        MCRObject mcrObj = createMCRObj();
        mcrObj.getMetadata().setMetadataElement(rubric.toMCRMetaElement());

        xmlOutput(mcrObj.createXML());        
        
        String dataModelLocation = "/datamodel-jpclassi.xsd";
        try {
            new SchemaValidator(dataModelLocation).validate(new JDOMSource(mcrObj.createXML()));
        } catch (SAXException e) {
            e.printStackTrace();
            fail("Schema validation failed for " + dataModelLocation);
        }
    }
    
    private void xmlOutput(Document mcrObjXML) throws IOException {
        assertNotNull(mcrObjXML);
        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        xmlOutputter.output(mcrObjXML, System.out);
    }
    
    private MCRObject createMCRObj() {
        MCRObject mcrObject = new MCRObject();
        mcrObject.setId(MCRObjectID.getInstance("jportal_classi_000000001"));
        mcrObject.setLabel("jportal_classi_000000001");
        mcrObject.setSchema("datamodel-jpclassi.xsd");
        return mcrObject;
    }
}
