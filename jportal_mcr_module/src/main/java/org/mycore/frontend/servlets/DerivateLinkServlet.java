package org.mycore.frontend.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mycore.common.MCRUsageException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.util.DerivateLinkUtil;

public class DerivateLinkServlet extends MCRServlet {

    private static Logger LOGGER = Logger.getLogger(DerivateLinkServlet.class);

    private enum Mode {
        setLink, removeLink, setImage, getImage
    }
    private enum Parameter {
        mode, from, to, derivateId, file
    }

    public void doGetPost(MCRServletJob job) throws Exception {
        // init
        HttpServletRequest request = job.getRequest();
        String modeAsString = request.getParameter(Parameter.mode.name());
        Mode mode = null;
        try {
            mode = Mode.valueOf(modeAsString);
        } catch(Exception exc) {
            throw new MCRUsageException("Bad request, '" + modeAsString + "' mode parameter is empty or not valid !");
        }

        // request dispatcher
        if (mode.equals(Mode.setLink) || mode.equals(Mode.removeLink)) {
            handleLink(job, mode);
        } else if(mode.equals(Mode.setImage)) {
            handleBookmarkImage(job);
        } else if(mode.equals(Mode.getImage)) {
            handleGetBookmarkedImage(job);
        } else {
            LOGGER.warn("Mode " + mode.name() + " is not implemented!");
        }
    }

    private void handleLink(MCRServletJob job, Mode mode) throws IOException, MCRActiveLinkException {
        HttpServletRequest request = job.getRequest();
        String from = request.getParameter(Parameter.from.name());
        String to = getTo(request);
        MCRObjectID mcrObjId = MCRObjectID.getInstance(from);

        if(mode.equals(Mode.setLink)) {
            LOGGER.debug("set link from " + from + " to " + to);
            DerivateLinkUtil.setLink(mcrObjId, to);
        } else {
            LOGGER.debug("remove link from " + from + " to " + to);
            DerivateLinkUtil.removeLink(mcrObjId, to);
        }
        job.getResponse().sendRedirect(super.getBaseURL() + "receive/" + from);
    }

    private void handleBookmarkImage(MCRServletJob job) {
        String file = job.getRequest().getParameter(Parameter.file.name());
        String derivateId = job.getRequest().getParameter(Parameter.derivateId.name());
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
        String to = request.getParameter(Parameter.to.name());
        if (to != null && !to.equals("")) {
            return to;
        } else {
            return DerivateLinkUtil.getBookmarkedImage();
        }
    }

}
