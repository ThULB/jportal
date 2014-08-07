package fsu.jportal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.XSLTransformer;

public class AddStructLink {

    public AddStructLink(String from, String to) throws JDOMException, IOException, URISyntaxException {
        XSLTransformer t = new XSLTransformer(this.getClass().getResourceAsStream("/xsl/add_structLink.xsl"));
        SAXBuilder b = new SAXBuilder();
        Document input = b.build(new File(from));
        Document output = t.transform(input);
        XMLOutputter o = new XMLOutputter(Format.getPrettyFormat());
        FileOutputStream fos = new FileOutputStream(new File(to));
        o.output(output, fos);
    }

    public static void main(String[] args) throws Exception {
        new AddStructLink("/data/Dokumente/OCR/test/innsbruck/mets.xml",
            "/data/Dokumente/OCR/test/innsbruck/mets_structLink.xml");
    }
}
