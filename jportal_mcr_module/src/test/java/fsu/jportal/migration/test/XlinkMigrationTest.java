package fsu.jportal.migration.test;

import java.io.InputStream;
import java.util.HashMap;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Test;
import org.mycore.common.MCRTestCase;
import org.mycore.common.xml.MCRXSLTransformation;

public class XlinkMigrationTest extends MCRTestCase {

    @Test
    public void changeXlinkAttr() throws Exception {
        InputStream journalXMLStream = getClass().getResourceAsStream("/xlinkMigration/journal.xml");
        SAXBuilder saxBuilder = new SAXBuilder();
        Document journalXMLDoc = saxBuilder.build(journalXMLStream);
        Source stylesheet = new StreamSource(getClass().getResourceAsStream("/xsl/xlinkMigration.xsl"));
        Document transform = MCRXSLTransformation.transform(journalXMLDoc, stylesheet, new HashMap<>());

        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        xmlOutputter.output(transform, System.out);
    }

}
