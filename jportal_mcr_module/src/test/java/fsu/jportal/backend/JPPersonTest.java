package fsu.jportal.backend;

import org.jdom2.Content;
import org.jdom2.Element;
import org.junit.Test;
import org.mycore.common.MCRJPATestCase;
import org.mycore.datamodel.metadata.MCRMetaXML;

import static org.junit.Assert.assertEquals;

public class JPPersonTest extends MCRJPATestCase {

    @Test
    public void setName() {
        JPPerson p = new JPPerson();
        p.setName("Friedrich", null, "II., Preußen, König");

        MCRMetaXML xml = (MCRMetaXML) p.getObject().getMetadata().getMetadataElement("def.heading").getElement(0);
        assertEquals("Friedrich", find(xml, "name"));
        assertEquals("II., Preußen, König", find(xml, "collocation"));

        p = new JPPerson();
        p.setName("Müller", "Peter", "von", "Kaiser");
        xml = (MCRMetaXML) p.getObject().getMetadata().getMetadataElement("def.heading").getElement(0);
        assertEquals("Peter", find(xml, "firstName"));
        assertEquals("Müller", find(xml, "lastName"));
        assertEquals("von", find(xml, "nameAffix"));
        assertEquals("Kaiser", find(xml, "collocation"));
    }

    protected String find(MCRMetaXML xml, String e) {
        for (Content c : xml.getContent()) {
            if (c instanceof Element && ((Element) c).getName().equals(e)) {
                return ((Element) c).getText();
            }
        }
        return null;
    }

}
