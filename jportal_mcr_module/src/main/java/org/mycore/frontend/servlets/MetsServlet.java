package org.mycore.frontend.servlets;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.mycore.common.MCRUsageException;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.mets.MetsGenerator;

public class MetsServlet extends MCRServlet {

    private static Logger LOGGER = Logger.getLogger(MetsServlet.class);
    
    private enum Mode {
        generate
    }
    private enum Parameter {
        mode, objid, derid
    }

    @Override
    protected void doGetPost(MCRServletJob job) throws Exception {
        HttpServletRequest request = job.getRequest();
        String modeAsString = request.getParameter(Parameter.mode.name());
        Mode mode = null;
        try {
            mode = Mode.valueOf(modeAsString);
        } catch(Exception exc) {
            throw new MCRUsageException("Bad request, '" + modeAsString + "' mode parameter is empty or not valid !");
        }

        if(Mode.generate.equals(mode)) {
            handleGenerate(job);
        } else {
            LOGGER.warn("Mode " + mode.name() + " is not implemented!");
        }
    }

    private void handleGenerate(MCRServletJob job) {
        HttpServletRequest request = job.getRequest();
        // get derivate
        String derIdAsString = request.getParameter(Parameter.derid.name());
        MCRObjectID derId = MCRObjectID.getInstance(derIdAsString);
        MCRDerivate mcrDer = MCRMetadataManager.retrieveMCRDerivate(derId);

        MetsGenerator metsGenerator = new MetsGenerator();
        metsGenerator.generate(mcrDer);
    }
}
