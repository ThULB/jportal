package org.mycore.dataimport.pica;

import java.io.File;
import java.util.Iterator;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.mycore.common.MCRTestCase;
import org.mycore.importer.MCRImportRecord;

public class PicaImporterTest extends MCRTestCase {

    public static void testElementConverter() throws Exception {
        // ZiNG
        SAXBuilder builder = new SAXBuilder();
        final Document zingDoc = builder.build(new File("jpApp/modules/jportal/tests/resources/dataimport/zing.xml"));
        MCRPicaElementIterator it = new MCRPicaElementIterator(zingDoc);
        Element zingRecord1 = it.next();
        assertNotNull(zingRecord1);
        Element zingRecord2 = it.next();
        assertNotNull(zingRecord2);
        assertEquals("record", zingRecord1.getName());
        assertEquals(3, zingRecord1.getChildren().size());
        assertEquals("record", zingRecord2.getName());
        assertEquals(2, zingRecord2.getChildren().size());

        Element datafieldElement = (Element)zingRecord1.getChildren().get(1);
        assertNotNull(datafieldElement);
        assertEquals("datafield", datafieldElement.getName());
        assertEquals("B", datafieldElement.getAttributeValue("tag"));
        Element subfieldElement = (Element)datafieldElement.getChildren().get(1);
        assertNotNull(subfieldElement);
        assertEquals("r1_B_1", subfieldElement.getText());

        // srw
        final Document srwDoc = builder.build(new File("jpApp/modules/jportal/tests/resources/dataimport/srw.xml"));
        it = new MCRPicaElementIterator(srwDoc);
        Element srwRecord1 = it.next();
        assertNotNull(srwRecord1);
        Element srwRecord2 = it.next();
        assertNotNull(srwRecord2);
        assertEquals("record", srwRecord1.getName());
        assertEquals(3, srwRecord1.getChildren().size());
        assertEquals("record", srwRecord2.getName());
        assertEquals(2, srwRecord2.getChildren().size());

        datafieldElement = (Element)srwRecord2.getChildren().get(1);
        assertNotNull(datafieldElement);
        assertEquals("datafield", datafieldElement.getName());
        assertEquals("B", datafieldElement.getAttributeValue("tag"));
        subfieldElement = (Element)datafieldElement.getChildren().get(0);
        assertNotNull(subfieldElement);
        assertEquals("r2_B_X", subfieldElement.getText());
    }

    public static void testRecordConverter() throws Exception {
        SAXBuilder builder = new SAXBuilder();
        final Document srwDoc = builder.build(new File("jpApp/modules/jportal/tests/resources/dataimport/rc.xml"));
        MCRPicaElementIterator it = new MCRPicaElementIterator(srwDoc);

        MCRPicaElementConverter eC = new MCRPicaElementConverter("testRecord");
        Element recordElement = it.next();
        assertNotNull(recordElement);
        MCRImportRecord record = eC.convert(recordElement);
        assertNotNull(record);
        assertEquals("testRecord", record.getName());
        assertEquals("1", record.getFieldById("001_0").getValue());
        assertEquals("2", record.getFieldById("002_0").getValue());
        assertEquals("3", record.getFieldById("002_1").getValue());
        assertEquals("4", record.getFieldById("003/08_a").getValue());
        assertEquals("5", record.getFieldById("003/08_b").getValue());
        assertEquals("6", record.getFieldById("028L/02_k_d").getValue());
        assertEquals("7", record.getFieldById("028L/02_k_a").getValue());

        assertEquals("8", record.getFieldById("multiField_0").getValue());
        assertEquals(2, record.getFieldsById("multiField_0").size());
        assertEquals("9", record.getFieldsById("multiField_0").get(1).getValue());
    }

}
