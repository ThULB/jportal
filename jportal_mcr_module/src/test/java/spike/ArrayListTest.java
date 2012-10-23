package spike;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Content;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.EntityRef;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.mycore.common.MCRConstants;

import junit.framework.TestCase;

public class ArrayListTest extends TestCase {
    public void testInitialCapacity() throws Exception {
        ArrayList<String> arrayList = new ArrayList<String>(4);
        int i = 0;
        for (String string : arrayList) {
            i++;
        }
//        String s = "<p>&ouml; &auml; &uuml; &szlig; &amp;</p><p>Heureka</p>foooasdfa";
        String s = "<p>sdfewfe</p><p>Heureka</p>foooasdfa";
        
        String rootName = "MyCoReWebPage";
        Element root = new Element(rootName);
        Element section = new Element("section");
        section.setAttribute("lang", "de", Namespace.XML_NAMESPACE);
        section.setAttribute("title", "Fooo");


        root.addContent(section);
        Document document = new Document(root);
        Content entity = new EntityRef("foo", "bar");
        root.addContent(entity);
        DocType doctype = new DocType(rootName);
        document.setDocType(doctype);
        
        addStringContent(section, s);
        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());

        xmlOutputter.output(document, System.out);

        assertEquals(0, i);
    }

    private void addStringContent(Element element, String s) throws JDOMException, IOException {
        s = "<tmp>" + s + "</tmp>";
        SAXBuilder saxBuilder = new SAXBuilder();
        Reader stringReader = new StringReader(s);
        Document stringAsDoc = saxBuilder.build(stringReader);
        List content = stringAsDoc.getRootElement().getContent();
        Iterator iterator = content.iterator();
        while (iterator.hasNext()) {
            Content object = (Content) iterator.next();
            iterator.remove();
            element.addContent(object);
        }
    }
}
