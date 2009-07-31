package org.mycore.dataimport.pica.test;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.mycore.dataimport.pica.MCRGbvSruConverter;
import org.mycore.dataimport.pica.MCRGbvSruRecordElementIterator;
import org.mycore.dataimport.pica.MCRGbvSruRetriever;
import org.mycore.dataimport.pica.mapping.MCRImportLinkAndCreateMapper;
import org.mycore.importer.MCRImportRecord;
import org.mycore.importer.event.MCRImportStatusEvent;
import org.mycore.importer.event.MCRImportStatusListener;
import org.mycore.importer.mapping.MCRImportMappingManager;

public class ImporterTest implements MCRImportStatusListener {

    private static final Logger LOGGER = Logger.getLogger(ImporterTest.class);
    
    public void testImport() {

        MCRImportMappingManager mappingManager = MCRImportMappingManager.getInstance();
        try {
            mappingManager.init(new File("/home/matthias/pica-import/mapping/pica-mapping.xml"));
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }

        mappingManager.getMapperManager().addMapper("linkAndCreate", MCRImportLinkAndCreateMapper.class);

        mappingManager.addStatusListener(this);
        
        MCRGbvSruRetriever retriever = new MCRGbvSruRetriever("http://gso.gbv.de/sru/DB=1.28/?query=pica.sgb+%3D+%22KAL1*%22&version=1.1&recordSchema=pica&startRecord=9&maximumRecords=352");
//        MCRGbvSruRetriever retriever = new MCRGbvSruRetriever("http://gso.gbv.de/sru/DB=1.28/?query=pica.ppn+%3D+%22006530524%22&version=1.1&operation=searchRetrieve&stylesheet=http%3A%2F%2Fgso.gbv.de%2Fsru%2F%3Fxsl%3DsearchRetrieveResponse&recordSchema=pica&maximumRecords=10&startRecord=1&recordPacking=xml&sortKeys=none&x-info-5-mg-requestGroupings=none");

        retriever.connect();
        MCRGbvSruConverter converter = new MCRGbvSruConverter();

        MCRGbvSruRecordElementIterator it = retriever.retrieve();
        while(it.hasNext()) {
            Element recordElement = it.next();
            List<MCRImportRecord> recordList = converter.convert(recordElement);
            mappingManager.startMapping(recordList);
        }
        LOGGER.info("Import successfully finished!");
    }

    public void objectImported(MCRImportStatusEvent e) {
        // TODO Auto-generated method stub
    }
    public void recordMapped(MCRImportStatusEvent e) {
        System.out.println(e.getObjectName());
    }

    public static void main(String args[]) {
        new ImporterTest().testImport();
    }
}