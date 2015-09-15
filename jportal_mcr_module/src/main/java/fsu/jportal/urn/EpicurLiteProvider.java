package fsu.jportal.urn;

import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.datamodel.ifs.MCRFile;
import org.mycore.datamodel.ifs.MCRFileNodeServlet;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.iview2.services.MCRIView2Tools;
import org.mycore.urn.epicurlite.EpicurLite;
import org.mycore.urn.epicurlite.IEpicurLiteProvider;
import org.mycore.urn.hibernate.MCRURN;

public class EpicurLiteProvider implements IEpicurLiteProvider {
    static final Logger LOGGER = LogManager.getLogger(EpicurLiteProvider.class);

    @Override
    public EpicurLite getEpicurLite(MCRURN urn) {
        EpicurLite elp = new EpicurLite(urn);
        // the base urn
        if (urn.getPath() == null || urn.getPath().trim().length() == 0) {
            elp.setFrontpage(true);
        }

        elp.setUrl(getURL(urn));
        return elp;
    }

    @Override
    public URL getURL(MCRURN urn) {
        String derivID = urn.getId();
        String path = urn.getPath();
        String filename = urn.getFilename();

        try {
            String spec = null;
            if (urn.getPath() == null || urn.getPath().trim().length() == 0) {
                MCRDerivate derivate = (MCRDerivate) MCRMetadataManager.retrieve(MCRObjectID.getInstance(urn.getId()));
                spec = MCRFrontendUtil.getBaseURL() + "receive/" + derivate.getOwnerID() + "?derivate=" + urn.getId();
            }
            // an urn for a certain file, links to iview2
            else {
                String absPath = path + filename;
                MCRFile file = MCRFile.getMCRFile(MCRObjectID.getInstance(urn.getId()), absPath);

                if (MCRIView2Tools.isFileSupported(file.toPath())) {
                    spec = MCRServlet.getServletBaseURL() + "MCRIviewClient?derivate=" + derivID + "&startImage="
                        + absPath;
                } else {
                    LOGGER.info("File is not displayable within iView2. Use "
                        + MCRFileNodeServlet.class.getSimpleName() + " as url");
                    String derivateId = file.getOwnerID();
                    String filePath = "/" + derivateId + file.getAbsolutePath();
                    spec = MCRServlet.getServletBaseURL() + MCRFileNodeServlet.class.getSimpleName() + filePath;
                }
                return new URL(spec);
            }
        } catch (Exception e) {
            LOGGER.error("Unable to get url of urn " + urn);
        }
        return null;
    }

}
