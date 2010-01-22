package org.mycore.dataimport.pica.thulb;

import java.io.File;
import java.util.List;

import org.mycore.common.MCRTestCase;
import org.mycore.dataimport.pica.MCRPicaJournalFilter;
import org.mycore.dataimport.pica.MCRPicaCatalog.RecordSchema;
import org.mycore.importer.MCRImportRecord;
import org.mycore.importer.mapping.MCRImportMappingManager;

public class ThulbImportTest extends MCRTestCase {

    @Override
    protected void setUp() throws Exception {
        System.setProperty("MCR.Configuration.File", "jpApp/modules/jportal/tests/resources/mycore.properties");
        super.setUp();
    }

    public static void testThulbShort() throws Exception {
        String query = "pica.all+%3D+%22Musikalisches+Wochenblatt%22";

        MCRThulbCatalog thulbCatalog = new MCRThulbCatalog();
        thulbCatalog.setRecordSchema(RecordSchema.pica_b);
        thulbCatalog.addRecordFilter(new MCRPicaJournalFilter());
        List<MCRImportRecord> recordList = thulbCatalog.getCatalogData(query, "short_journal");
    }

    public static void testThulbJournalImport() throws Exception {
        // pica.ppn+%3D+%22129473383%22
        String query = "pica.all+%3D+%22Musikalisches+Wochenblatt%22";

        MCRThulbCatalog thulbCatalog = new MCRThulbCatalog();
        thulbCatalog.addRecordFilter(new MCRPicaJournalFilter());
        List<MCRImportRecord> recordList = thulbCatalog.getCatalogData(query, "journal");

        MCRImportMappingManager mappingManager = MCRImportMappingManager.getInstance();
        mappingManager.init(new File("/home/matthias/pica-import/mapping/thulb-catalog.xml"));

        mappingManager.startMapping(recordList);
    }

}
