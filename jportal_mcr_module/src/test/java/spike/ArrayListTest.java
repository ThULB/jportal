package spike;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Content;
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.EntityRef;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

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
