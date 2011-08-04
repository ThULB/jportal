package fsu.jportal.xml.test;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.io.InputStream;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.junit.Test;

import fsu.jportal.xml.ClassificationIDExtractor;
import fsu.jportal.xml.XMLDataManager;


public class ClassificationIDExtractorTest {
    @Test
    public void extractClassificationID() throws Exception {
        XMLDataManager xmlDataManager = createMock(XMLDataManager.class.getSimpleName(),XMLDataManager.class);
        String id = "testJournal";
        InputStream xmlObjStream = this.getClass().getResourceAsStream("/mcrObj/objWithClassID.xml");
        SAXBuilder saxBuilder = new SAXBuilder();
        Document xmlDoc = saxBuilder.build(xmlObjStream);
        expect(xmlDataManager.getXML(id)).andReturn(xmlDoc);
        replay(xmlDataManager);
        ClassificationIDExtractor classificationIDExtractor = new ClassificationIDExtractor(xmlDataManager);
        classificationIDExtractor.getClassIDs(id);
        verify(xmlDataManager);
    }
}
