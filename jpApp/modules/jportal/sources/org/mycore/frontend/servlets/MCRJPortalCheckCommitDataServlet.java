package org.mycore.frontend.servlets;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.editor.MCREditorSubmission;
import org.mycore.frontend.editor.MCRRequestParameters;

public class MCRJPortalCheckCommitDataServlet extends MCRCheckDataBase {
	private static final MCRConfiguration CONFIG = MCRConfiguration.instance();
    private static Logger LOGGER = Logger.getLogger(MCRJPortalCheckCommitDataServlet.class);

    private static final long serialVersionUID = 1L;

    /**
     * This method overrides doGetPost of MCRServlet. <br />
     */
    public void doGetPost(MCRServletJob job) throws Exception {
        // read the XML data
        MCREditorSubmission sub = (MCREditorSubmission) (job.getRequest().getAttribute("MCREditorSubmission"));
        Document indoc = sub.getXML();

        // read the parameter
        MCRRequestParameters parms;

        if (sub == null) {
            parms = new MCRRequestParameters(job.getRequest());
        } else {
            parms = sub.getParameters();
        }

        String oldmcrid = parms.getParameter("mcrid");
        String oldtype = parms.getParameter("type");
        String oldstep = parms.getParameter("step");
        LOGGER.debug("XSL.target.param.0 = " + oldmcrid);
        LOGGER.debug("XSL.target.param.1 = " + oldtype);
        LOGGER.debug("XSL.target.param.2 = " + oldstep);

        // get the MCRSession object for the current thread from the session
        // manager.
        MCRSession mcrSession = MCRSessionMgr.getCurrentSession();
        String lang = mcrSession.getCurrentLanguage();
        LOGGER.info("LANG = " + lang);

        // prepare the MCRObjectID's for the Metadata
        String mmcrid = "";
        boolean hasid = false;

        try {
            mmcrid = indoc.getRootElement().getAttributeValue("ID");

            if (mmcrid == null) {
                mmcrid = oldmcrid;
            } else {
                hasid = true;
            }
        } catch (Exception e) {
            mmcrid = oldmcrid;
        }

        MCRObjectID ID = new MCRObjectID(mmcrid);

        if (!ID.getTypeId().equals(oldtype)) {
            ID = new MCRObjectID(oldmcrid);
            hasid = false;
        }

        if (!hasid) {
            indoc.getRootElement().setAttribute("ID", ID.getId());
        }
        // create a metadata object and prepare it
        Document preparedMetadataDocument = prepareMetadata((Document) indoc.clone(), ID, job, lang);
        MCRObject mcrObj = new MCRObject();
        mcrObj.setFromJDOM(preparedMetadataDocument);

        // update or create in datastore
        if(MCRObject.existInDatastore(ID)) {
            mcrObj.updateInDatastore();
        } else {
            mcrObj.createInDatastore();
        }

        // try to go to the returnUrl, otherwise to the edited object
        String url = parms.getParameter("returnUrl");
        if(url == null)
            url = getBaseURL() +  getNextURL(ID, true);

        if (!job.getResponse().isCommitted())
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(url));
    }

    @Override
    protected String getNextURL(MCRObjectID ID, boolean okay) throws MCRActiveLinkException {
        StringBuffer sb = new StringBuffer();
        if (okay) {
            // return all is ready
            sb.append("receive/").append(ID.getId());
        } else {
            sb.append(CONFIG.getString("MCR.SWF.PageDir", "")).append(CONFIG.getString("MCR.SWF.PageErrorStore", "editor_error_store.xml"));
        }
        return sb.toString();
    }

    /**
     * not used
     */
    @Override
    protected void sendMail(MCRObjectID ID) {}
}
