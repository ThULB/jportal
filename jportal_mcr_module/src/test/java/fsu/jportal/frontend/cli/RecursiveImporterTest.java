package fsu.jportal.frontend.cli;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Ignore;
import org.junit.Test;

import fsu.jportal.backend.ImportDerivateObject;
import fsu.jportal.backend.io.ImportSink;
import fsu.jportal.backend.io.RecursiveImporter;
import fsu.jportal.frontend.cli.io.HttpImportSource;

/**
 * Created by chi on 23.04.15.
 * @author Huu Chi Vu
 */
public class RecursiveImporterTest {

    public static class MockImportSink implements ImportSink {

        private final ArrayList<Document> objs;

        private ArrayList<Document> classifications;

        private ArrayList<ImportDerivateObject> derivates;

        public MockImportSink() {
            objs = new ArrayList<>();
            classifications = new ArrayList<>();
            derivates = new ArrayList<>();
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

        public ArrayList<ImportDerivateObject> getDerivates() {
            return derivates;
         }

        public void saveDerivate(ImportDerivateObject deriObj) {
            getDerivates().add(deriObj);
        }

        public void saveDerivateLinks() {

        }
    }

    @Test
    @Ignore
    public void testGetParticipants() throws Exception {
        HttpImportSource httpImportSource = new HttpImportSource("http://zs.thulb.uni-jena.de",
                "jportal_jpjournal_00001219");
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
