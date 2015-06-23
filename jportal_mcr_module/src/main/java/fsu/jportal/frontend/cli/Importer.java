package fsu.jportal.frontend.cli;

import fsu.jportal.backend.io.ImportSink;
import fsu.jportal.backend.io.RecursiveImporter;
import fsu.jportal.frontend.cli.io.HttpImportSource;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.utils.MCRXMLTransformer;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;

import java.net.URISyntaxException;

/**
 * Created by chi on 22.04.15.
 * @author Huu Chi Vu
 */
@MCRCommandGroup(name = "Jportal Importer")
public class Importer {
    private static Logger LOGGER = Logger.getLogger(Importer.class);

    private static String host;

    @MCRCommand(syntax = "importObj {0} {1}", help = "importObj webappURL id")
    public static void importObj(String urlStr, String id) {
        HttpImportSource httpImportSource = new HttpImportSource(urlStr, id);
        ImportSink localSystem = new LocalSystemSink();
        new RecursiveImporter(httpImportSource, localSystem).start();
    }

    private static class LocalSystemSink implements ImportSink {

        @Override
        public void save(Document objXML) {
            MCRObject mcrObject = new MCRObject(objXML);

            if (MCRMetadataManager.exists(mcrObject.getId())) {
                mcrObject.setImportMode(true);
                try {
                    MCRMetadataManager.update(mcrObject);
                } catch (MCRActiveLinkException e) {
                    e.printStackTrace();
                }
                LOGGER.info("Updated object: " + mcrObject.getId().toString());
            } else {
                MCRMetadataManager.create(mcrObject);
                LOGGER.info("Created object: " + mcrObject.getId().toString());
            }
        }

        @Override
        public void saveClassification(Document classificationXML) {
            try {
                MCRCategory category = MCRXMLTransformer.getCategory(classificationXML);

                MCRCategoryDAO categoryDAO = MCRCategoryDAOFactory.getInstance();
                if (categoryDAO.exist(category.getId())) {
                    categoryDAO.replaceCategory(category);
                    LOGGER.info("Updated classification: " + category.getId().toString());
                } else {
                    categoryDAO.addCategory(null, category);
                    LOGGER.info("Created classification: " + category.getId().toString());
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }
}
