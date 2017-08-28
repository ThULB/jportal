package spike;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectMetadata;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;


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
