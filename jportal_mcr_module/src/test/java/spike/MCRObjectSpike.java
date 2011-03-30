package spike;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.lang.reflect.Type;

import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectMetadata;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import fsu.jportal.metadata.RubricLabel;
import fsu.jportal.metadata.XMLMetaElement;
import fsu.jportal.metadata.XMLMetaElementEntry;

public class MCRObjectSpike {

    @Before
    public void init() {
        System.setProperty("MCR.Configuration.File", "config/test.properties");
        System.setProperty("MCR.Metadata.Type.person", "true");
    }

    @Test
    public void createNewMinimalMCRObject() throws Exception {
        MCRObject mcrObject = createPersonObj();
        assertNotNull(mcrObject);

        MCRObjectMetadata metadata = mcrObject.getMetadata();
        assertNotNull(metadata);

        xmlOutput(mcrObject.createXML());
    }

    @Test
    public void createPersonObjWithMCRAPI() throws Exception {
        MCRObject mcrObject = createPersonObj();

        XMLMetaElement<PersonName> nameMetaElement = new XMLMetaElement<PersonName>("def.heading");
        nameMetaElement.addMetaElemEntry(new PersonName("Bud", "Spencer"));
        nameMetaElement.addMetaElemEntry(new PersonName("Homer", "Simpson"));
        
        mcrObject.getMetadata().setMetadataElement(nameMetaElement.toMCRMetaElement());

        xmlOutput(mcrObject.createXML());
        
        Gson gson = new Gson();
        Type rubricType = new TypeToken<XMLMetaElement<PersonName>>() {}.getType();
        System.out.println(gson.toJson(rubricType));
    }
    
    @Test
    public void createRubricWithAPI() throws Exception {
        MCRObject mcrObject = createPersonObj();

        XMLMetaElement<RubricLabel> rubric = new XMLMetaElement<RubricLabel>("rubric");
        rubric.addMetaElemEntry(new RubricLabel("de", "Rubriken Test fuer MyCoRe", "test de"));
        rubric.addMetaElemEntry(new RubricLabel("de", "Rubric test for MyCoRe", "test en"));
        
        mcrObject.getMetadata().setMetadataElement(rubric.toMCRMetaElement());

        xmlOutput(mcrObject.createXML());
    }
    
    public class PersonName extends XMLMetaElementEntry{
        final String FIRSTNAME = "firstname";
        final String LASTNAME = "lastname";
        
        public PersonName(String firstname, String lastname) {
            getTagValueMap().put(FIRSTNAME, firstname);
            getTagValueMap().put(LASTNAME, lastname);
        }

        @Override
        public String getLang() {
            return "de";
        }

        @Override
        public String getMetaElemName() {
            return "heading";
        }
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
