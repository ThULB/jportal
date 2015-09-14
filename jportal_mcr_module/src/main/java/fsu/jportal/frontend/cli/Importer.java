package fsu.jportal.frontend.cli;

import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;

import fsu.jportal.backend.io.ImportSink;
import fsu.jportal.backend.io.RecursiveImporter;
import fsu.jportal.frontend.cli.io.HttpImportSource;
import fsu.jportal.frontend.cli.io.LocalSystemSink;

/**
 * Created by chi on 22.04.15.
 * @author Huu Chi Vu
 */
@MCRCommandGroup(name = "Jportal Importer")
public class Importer {

    @MCRCommand(syntax = "importObj {0} {1}", help = "importObj webappURL id")
    public static void importObj(String urlStr, String id) {
        HttpImportSource httpImportSource = new HttpImportSource(urlStr, id);
        ImportSink localSystem = new LocalSystemSink(urlStr);
        new RecursiveImporter(httpImportSource, localSystem).start();
    }

//    @MCRCommand(syntax = "importGBVObj {0} {1}", help = "importGBVObj webappURL id")
//    public static void importGBVObj(String urlStr, String id) {
//        HttpImportSource httpImportSource = new HttpImportSource(urlStr, id);
//        GBVSystemSink gbvSystem = new GBVSystemSink(urlStr);
//        new RecursiveImporter(httpImportSource, gbvSystem).start();
//    }
}
