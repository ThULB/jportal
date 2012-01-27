package fsu.jportal.migration.test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.xpath.XPath;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.xml.MCRXSLTransformation;
import org.mycore.datamodel.common.MCRXMLMetadataManager;

public class XlinkMigrationTest {
    @Before
    public void init() {
        System.setProperty("MCR.Configuration.File", "config/test.properties");
        Properties mcrProperties = MCRConfiguration.instance().getProperties();
        mcrProperties.setProperty("MCR.Metadata.Type.jpjournal", "true");
        mcrProperties.setProperty("MCR.Metadata.Type.jpvolume", "true");
        mcrProperties.setProperty("MCR.Metadata.Type.jpinst", "true");
        mcrProperties.setProperty("MCR.Metadata.Type.person", "true");
    }

    @Test
    public void changeXlinkAttr() throws Exception {
        InputStream journalXMLStream = getClass().getResourceAsStream("/xlinkMigration/journal.xml");
        SAXBuilder saxBuilder = new SAXBuilder();
        Document journalXMLDoc = saxBuilder.build(journalXMLStream);
        Source stylesheet = new StreamSource(getClass().getResourceAsStream("/xsl/xlinkMigration.xsl"));
        Document transform = MCRXSLTransformation.transform(journalXMLDoc, stylesheet, new HashMap());
        
        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        xmlOutputter.output(transform, System.out);
    }
}
