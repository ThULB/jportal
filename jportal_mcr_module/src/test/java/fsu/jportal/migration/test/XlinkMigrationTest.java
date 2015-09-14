package fsu.jportal.migration.test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.config.MCRConfigurationLoader;
import org.mycore.common.config.MCRConfigurationLoaderFactory;
import org.mycore.common.xml.MCRXSLTransformation;

public class XlinkMigrationTest {

    private String mcrBaseDir;

    @Before
    public void init() {
        MCRConfiguration mcrConf = MCRConfiguration.instance();
        MCRConfigurationLoader configurationLoader = MCRConfigurationLoaderFactory.getConfigurationLoader();
        Map<String, String> load = configurationLoader.load();
        load.put("MCR.Metadata.Type.jpjournal", "true");
        load.put("MCR.Metadata.Type.jpvolume", "true");
        load.put("MCR.Metadata.Type.jpinst", "true");
        load.put("MCR.Metadata.Type.person", "true");
        load.put("MCR.CommandLineInterface.SystemName", "JUnit Test");
        String buildDir = System.getProperty("project.buildDir");
        String mcrbaseDirName = "mcrbaseDir";

        try {
            if (buildDir == null) {
                mcrBaseDir = Files.createTempDirectory(mcrbaseDirName).toString();
            } else {
                mcrBaseDir = Paths.get(buildDir).resolve(mcrbaseDirName).toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Using MCR.basedir: " + mcrBaseDir);
        load.put("MCR.basedir", mcrBaseDir);
        mcrConf.initialize(load, true);
    }

    @After
    public void cleanUp() {
        try {
            Files.walkFileTree(Paths.get(mcrBaseDir), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

            });
        } catch (IOException e) {
            e.printStackTrace();
        }
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
