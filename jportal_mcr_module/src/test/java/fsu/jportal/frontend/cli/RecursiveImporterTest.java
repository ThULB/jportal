package fsu.jportal.frontend.cli;

import fsu.jportal.backend.io.ImportSink;
import fsu.jportal.backend.io.RecursiveImporter;
import fsu.jportal.frontend.cli.io.HttpImportSource;
import org.jdom2.*;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chi on 23.04.15.
 * @author Huu Chi Vu
 */
public class RecursiveImporterTest {

    public static class MockImportSink implements ImportSink {

        private final ArrayList<Document> objs;

        private ArrayList<Document> classifications;

        public MockImportSink() {
            objs = new ArrayList<>();
            classifications = new ArrayList<>();
        }

        public ArrayList<Document> getObjs() {
            return objs;
        }

        public void save(Document objXML) {
//            printXML(objXML);
            getObjs().add(objXML);
        }



        private void printXML(Document objXML) {
            XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
            try {
                xmlOutputter.output(objXML, System.out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public ArrayList<Document> getClassifications() {
            return classifications;
        }

        public void saveClassification(Document classificationXML) {
//            printXML(classificationXML);
            getClassifications().add(classificationXML);
        }
    }

    @Test
    public void testGetParticipants() throws Exception {
        HttpImportSource httpImportSource = new HttpImportSource(
                "http://zs.thulb.uni-jena.de/receive/jportal_jpjournal_00001219?XSL.Style=xml");
        MockImportSink mockImportSink = new MockImportSink();
        RecursiveImporter importer = new RecursiveImporter(httpImportSource, mockImportSink);
        List<Document> objs = httpImportSource.getObjs();
        assertNotNull(objs);
        assertFalse(objs.isEmpty());
        importer.start();
        System.out.println("Objects imported: " + mockImportSink.getObjs().size());
        System.out.println("Classification imported: " + mockImportSink.getClassifications().size());
//        assertEquals(3, importSink.getObjs().size());
//        assertEquals(5, importSink.getClassifications().size());

    }
}
