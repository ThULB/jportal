package fsu.jportal.backend.io;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.utils.MCRXMLTransformer;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaDerivateLink;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaInterface;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;

import fsu.jportal.backend.DerivateTools;
import fsu.jportal.backend.ImportDerivateObject;
import fsu.jportal.backend.ImportFileObject;
import fsu.jportal.frontend.cli.Importer;

/**
 * Created by michel on 07.07.15.
 * @author Michel Büchner
 */
public class LocalSystemSink implements ImportSink {

    private static Logger LOGGER = LogManager.getLogger(Importer.class);

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
        try {
            if (MCRMetadataManager.exists(mcrObject.getId())) {
                mcrObject.setImportMode(true);
                MCRMetadataManager.update(mcrObject);
                LOGGER.info("Updated object: " + mcrObject.getId().toString());
            } else {
                MCRMetadataManager.create(mcrObject);
                LOGGER.info("Created object: " + mcrObject.getId().toString());
            }
        } catch (MCRPersistenceException | MCRAccessException e) {
            LOGGER.error("Error while updating Object " + mcrObject.getId().toString(), e);
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
            LOGGER.error("Error while saving Classification", e);
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
                    DerivateTools
                            .uploadFileWithoutTransaction(url.openStream(), file.getSize(), deriObj.getDocumentID(),
                                                          deriObj.getDerivateID(), file.getPath());
                } catch (Exception e) {
                    LOGGER.error("Error while uploading File " + completePath, e);
                }
            }
        } catch (MCRPersistenceException | MCRAccessException e) {
            LOGGER.error("Error while creating Derivate " + derivate.getId().toString(), e);
        }
    }

    @Override
    public void saveDerivateLinks() {
        for (Link link : derivateLinkList) {
            try {
                DerivateTools.setLink(link.document, link.file);
            } catch (MCRActiveLinkException | MCRAccessException e) {
                LOGGER.error("Error while linking Document " + link.document + " with file " + link.file, e);
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
