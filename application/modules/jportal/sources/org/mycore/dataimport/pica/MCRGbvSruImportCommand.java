package org.mycore.dataimport.pica;

import java.io.File;

import org.mycore.importer.mcrimport.MCRImportImporter;

public class MCRGbvSruImportCommand {

    public static void gbcSruImport() throws Exception {

        MCRImportImporter importer = new MCRImportImporter(new File("/home/matthias/pica-import/mapping/pica-mapping.xml"));

        importer.startImport();
    }

}