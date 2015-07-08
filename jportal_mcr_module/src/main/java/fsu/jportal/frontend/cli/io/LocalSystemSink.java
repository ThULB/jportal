package fsu.jportal.frontend.cli.io;

import fsu.jportal.backend.DerivateTools;
import fsu.jportal.backend.ImportDerivateObject;
import fsu.jportal.backend.ImportFileObject;
import fsu.jportal.backend.io.ImportSink;
import fsu.jportal.frontend.cli.Importer;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.utils.MCRXMLTransformer;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by michel on 07.07.15.
 * @author Michel Büchner
 */
public class LocalSystemSink  implements ImportSink{

    private static Logger LOGGER = Logger.getLogger(Importer.class);
    private String host;
    private List<Link> derivateLinkList = new ArrayList<>();

    private class Link {
        String document;
        String file;

        public Link(String document, String file) {
            this.document = document;
            this.file = file;
        }
    }

    public LocalSystemSink(String host) {
        this.host = host;
    }

    @Override
    public void save(Document objXML) {
        MCRObject mcrObject = new MCRObject(objXML);
        saveDerivateLink(mcrObject);
        mcrObject.getStructure().clearDerivates();
        if (MCRMetadataManager.exists(mcrObject.getId())) {
            mcrObject.setImportMode(true);
            try {
                MCRMetadataManager.update(mcrObject);
            } catch (MCRActiveLinkException e) {
                LOGGER.error("Error while updating Object" + mcrObject.getId().toString());
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
            LOGGER.error("Error while saving Classification");
            e.printStackTrace();
        }
    }

    @Override
    public void saveDerivate(ImportDerivateObject deriObj) {
        MCRDerivate derivate = new MCRDerivate(deriObj.getDerivateXML());
        derivate.setImportMode(true);
        try {
            MCRMetadataManager.create(derivate);
            for (ImportFileObject file : deriObj.getChildren()) {
                String completePath = deriObj.getDerivateID() + file.getPath();
                try {
                    URL url = new URL(this.host + "/servlets/MCRFileNodeServlet/" + completePath);
                    DerivateTools.uploadFile(url.openStream(), file.getSize(), deriObj.getDocumentID(), deriObj.getDerivateID(), file.getPath());
                } catch (Exception e) {
                    LOGGER.error("Error while uploading File " + completePath);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error while creating Derivate " + derivate.getId().toString());
            e.printStackTrace();
        }
    }

    @Override
    public void saveDerivateLinks() {
        for (Link link : derivateLinkList) {
            try {
                DerivateTools.setLink(link.document, link.file);
            } catch (MCRActiveLinkException e) {
                LOGGER.error("Error while linking Document " + link.document + " with file " + link.file);
                e.printStackTrace();
            }
        }
    }

    protected void saveDerivateLink(MCRObject obj) {
        MCRMetaElement deriLinks = obj.getMetadata().getMetadataElement("derivateLinks");
        if (deriLinks != null) {
            for (MCRMetaInterface link : deriLinks) {
                derivateLinkList.add(new Link(obj.getId().toString(), ((MCRMetaDerivateLink) link).getXLinkHref()));
            }
        }
        obj.getMetadata().removeMetadataElement("derivateLinks");
    }
}
