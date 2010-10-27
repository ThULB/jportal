package org.mycore.frontend.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;
import org.mycore.backend.ifs.MCRJPortalLink;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRUsageException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRObjectID;

public class MCRJPortalLinkFileServlet extends MCRServlet {

    private static Logger LOGGER = Logger.getLogger(MCRJPortalLinkFileServlet.class);

    private static final String PARAM_PREFIX = "jportalLinkFileServlet.";

    private static final String SET_PARAM = "setLink";

    private static final String REMOVE_PARAM = "removeLink";

    private static final String FROM_PARAM = PARAM_PREFIX + "from";

    private static final String TO_PARAM = PARAM_PREFIX + "to";

    public void doGetPost(MCRServletJob job) throws IOException, MCRPersistenceException, MCRActiveLinkException, JDOMException {

        // init
        HttpServletRequest request = job.getRequest();
        if (!wellRequest(request))
            throw new MCRUsageException("Bad request, " + PARAM_PREFIX + "mode parameter is empty or not valid !");

        // request dispatcher
        String mode = getMode(request);
        if (mode.equals(SET_PARAM)) {
            setLink(request);
        } else if (mode.equals(REMOVE_PARAM)) {
            removeLink(request);
        }

        // get back to browser
        job.getResponse().sendRedirect(super.getBaseURL() + "receive/" + getFrom(request));
    }

    /**
     * @param request
     */
    private String getTo(HttpServletRequest request) {
        if (null != request.getParameter(TO_PARAM) && !request.getParameter(TO_PARAM).equals("")) {
            return request.getParameter(TO_PARAM);
        } else {
            // use saved image URL from imaga viewer
            return (String) MCRSessionMgr.getCurrentSession().get("XSL.MCR.Module-iview.markedImageURL");
        }

    }

    /**
     * @param request
     */
    private String getFrom(HttpServletRequest request) {
        return request.getParameter(FROM_PARAM);
    }

    private final boolean wellRequest(HttpServletRequest request) {
        String mode = getMode(request);
        if (mode == null || mode.equals("") || !(mode.equals(SET_PARAM) || mode.equals(REMOVE_PARAM)))
            return false;
        return true;
    }

    /**
     * @param request
     * @return
     */
    private String getMode(HttpServletRequest request) {
        return request.getParameter(PARAM_PREFIX + "mode");
    }

    private void setLink(HttpServletRequest request) throws MCRPersistenceException, MCRActiveLinkException, IOException {
        MCRObjectID from = MCRObjectID.getInstance(getFrom(request));
        String to = getTo(request);
        LOGGER.debug("set link from " + from + " to " + to);
        MCRJPortalLink link = new MCRJPortalLink(from, to);
        link.set();
    }

    private void removeLink(HttpServletRequest request) throws MCRActiveLinkException, JDOMException, IOException {
        MCRObjectID from = MCRObjectID.getInstance(getFrom(request));
        String to = getTo(request);
        LOGGER.debug("remove link from " + from + " to " + to);
        MCRJPortalLink link = new MCRJPortalLink(from, to);
        link.remove();
    }
}
