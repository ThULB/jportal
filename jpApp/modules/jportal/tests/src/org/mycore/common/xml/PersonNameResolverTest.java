package org.mycore.common.xml;

import java.io.File;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import junit.framework.TestCase;

public class PersonNameResolverTest extends TestCase {
    public void testResolve() throws Exception {
        SAXBuilder builder = new SAXBuilder();
        final Document personXML = builder.build(new File("jpApp/modules/jportal/tests/resources/mcrObj/person.xml"));
        assertNotNull(personXML);
        
        PersonNameResolver personNameResolver = new PersonNameResolver(){
            @Override
            protected Element getPersonXMl(String uri) {
                return personXML.getRootElement();
            }
        };
        
        Element personNames = personNameResolver.resolveElement("personName:foooID");
        assertNotNull(personNames);
        assertEquals("nameList", personNames.getName());
        System.out.println(personNames.getText());
        assertTrue(personNames.getText().contains("Neumann"));
        assertTrue(personNames.getText().contains("Oldman Old"));
        assertTrue(personNames.getText().contains("Freitext"));
    }
}
