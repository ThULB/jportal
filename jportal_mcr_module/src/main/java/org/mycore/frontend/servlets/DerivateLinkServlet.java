package org.mycore.frontend.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;
import org.mycore.common.MCRUsageException;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.util.DerivateLinkUtil;

public class DerivateLinkServlet extends MCRServlet {

    private static Logger LOGGER = Logger.getLogger(DerivateLinkServlet.class);

    private static final String SET_LINK = "setLink";
    private static final String REMOVE_LINK = "removeLink";
    private static final String SET_IMAGE = "setImage";
    private static final String GET_IMAGE = "getImage";

    private static final String FROM = "from";
    private static final String TO = "to";
    private static final String DERIVATE_ID = "derivateId";
    private static final String FILE = "file";

    public void doGetPost(MCRServletJob job) throws Exception {
        // init
        HttpServletRequest request = job.getRequest();
        String mode = request.getParameter("mode");
        if (!wellRequest(mode))
            throw new MCRUsageException("Bad request, '" + mode + "' mode parameter is empty or not valid !");

        // request dispatcher
        if (mode.equals(SET_LINK) || mode.equals(REMOVE_LINK)) {
            handleLink(job, mode);
        } else if(mode.equals(SET_IMAGE)) {
            handleBookmarkImage(job);
        } else if(mode.equals(GET_IMAGE)) {
            handleGetBookmarkedImage(job);
        }
    }

    private final boolean wellRequest(String mode) {
        if ( mode == null || mode.equals("") ||
             !(mode.equals(SET_LINK) || mode.equals(REMOVE_LINK) ||
               mode.equals(SET_IMAGE) || mode.equals(GET_IMAGE))) {
            return false;
        }
        return true;
    }

    private void handleLink(MCRServletJob job, String mode) throws IOException, JDOMException {
        HttpServletRequest request = job.getRequest();
        String from = request.getParameter(FROM);
        String to = getTo(request);
        MCRObjectID mcrObjId = MCRObjectID.getInstance(from);

        if(mode.equals(SET_LINK)) {
            LOGGER.debug("set link from " + from + " to " + to);
            DerivateLinkUtil.setLink(mcrObjId, to);
        } else {
            LOGGER.debug("remove link from " + from + " to " + to);
            DerivateLinkUtil.removeLink(mcrObjId, to);
        }
        job.getResponse().sendRedirect(super.getBaseURL() + "receive/" + from);
    }

    private void handleBookmarkImage(MCRServletJob job) {
        String file = job.getRequest().getParameter(FILE);
        String derivateId = job.getRequest().getParameter(DERIVATE_ID);
        DerivateLinkUtil.bookmarkImage(derivateId, file);
    }

    private void handleGetBookmarkedImage(MCRServletJob job) throws IOException {
        String answer = DerivateLinkUtil.getBookmarkedImage();
        HttpServletResponse response = job.getResponse();
        response.setContentType("text/plain");
        response.getWriter().write(answer);
        response.getWriter().flush();
        response.getWriter().close();
    }
    
    /**
     * @param request
     */
    private String getTo(HttpServletRequest request) {
        String to = request.getParameter(TO);
        if (to != null && !to.equals("")) {
            return to;
        } else {
            return DerivateLinkUtil.getBookmarkedImage();
        }
    }

}
