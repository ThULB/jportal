package fsu.jportal.migration.test;

import java.io.InputStream;
import java.util.HashMap;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.xml.MCRXSLTransformation;

public class XlinkMigrationTest {
    @Before
    public void init() {
        System.setProperty("MCR.Configuration.File", "config/test.properties");
        MCRConfiguration instance = MCRConfiguration.instance();
        instance.set("MCR.Metadata.Type.jpjournal", "true");
        instance.set("MCR.Metadata.Type.jpvolume", "true");
        instance.set("MCR.Metadata.Type.jpinst", "true");
        instance.set("MCR.Metadata.Type.person", "true");
    }

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
