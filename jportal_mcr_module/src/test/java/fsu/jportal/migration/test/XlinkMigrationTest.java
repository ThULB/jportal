package fsu.jportal.migration.test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.config.MCRConfigurationLoader;
import org.mycore.common.config.MCRConfigurationLoaderFactory;
import org.mycore.common.xml.MCRXSLTransformation;

public class XlinkMigrationTest {
    @Before
    public void init() {
        MCRConfiguration mcrConf = MCRConfiguration.instance();
        MCRConfigurationLoader configurationLoader = MCRConfigurationLoaderFactory.getConfigurationLoader();
        Map<String, String> load = configurationLoader.load();
        mcrConf.initialize(load, true);
        mcrConf.set("MCR.Metadata.Type.jpjournal", "true");
        mcrConf.set("MCR.Metadata.Type.jpvolume", "true");
        mcrConf.set("MCR.Metadata.Type.jpinst", "true");
        mcrConf.set("MCR.Metadata.Type.person", "true");
        mcrConf.set("MCR.CommandLineInterface.SystemName", "JUnit Test");
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
