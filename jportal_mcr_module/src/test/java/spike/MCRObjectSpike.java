package spike;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;
import org.mycore.common.MCRConfiguration;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectMetadata;


public class MCRObjectSpike {

    @Before
    public void init() {
        System.setProperty("MCR.Configuration.File", "config/test.properties");
        MCRConfiguration.instance().set("MCR.Metadata.Type.person", "true");
    }

    @Test
    public void createNewMinimalMCRObject() throws Exception {
        MCRObject mcrObject = createPersonObj();
        assertNotNull(mcrObject);

        MCRObjectMetadata metadata = mcrObject.getMetadata();
        assertNotNull(metadata);

        xmlOutput(mcrObject.createXML());
    }

    private void xmlOutput(Document mcrObjXML) throws IOException {
        assertNotNull(mcrObjXML);
        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        xmlOutputter.output(mcrObjXML, System.out);
    }

    private MCRObject createPersonObj() {
        MCRObject mcrObject = new MCRObject();
        mcrObject.setId(MCRObjectID.getInstance("jportal_person_000000001"));
        mcrObject.setLabel("jportal_person_000000001");
        mcrObject.setSchema("person.xsd");
        return mcrObject;
    }
}
