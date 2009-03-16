package org.mycore.frontend.servlets;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.datamodel.common.MCRLinkTableManager;
import org.mycore.datamodel.metadata.MCRMetaLink;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectService;
import org.mycore.datamodel.metadata.MCRObjectStructure;
import org.mycore.user.MCRUserMgr;

/**
 * Editor servlet for JPortal which extends the delete action.
 * @author Matthias Eichner
 */
public class MCRJPortalStartEditorServlet extends MCRStartEditorServlet {

    private static final long serialVersionUID = 1L;

    private static final String FS = System.getProperty("file.seperator", "/");
    protected static String restorePage = pagedir + CONFIG.getString("MCR.SWF.PageRestore", "editor_restore.xml");
    protected static String restoreErrorPage = pagedir + CONFIG.getString("MCR.SWF.PageErrorRestore", "editor_error_restore.xml");
    protected static String exportErrorPage = pagedir + CONFIG.getString("MCR.SWF.PageErrorExport", "editor_error_export.xml");
    protected static String linkErrorPage = pagedir + CONFIG.getString("MCR.SWF.PageErrorLink", "editor_error_link.xml");
    protected static String childlinkedErrorPage = pagedir + CONFIG.getString("MCR.SWF.PageErrorChildLinked", "editor_error_childlinked.xml");

    protected static String recycleDir = CONFIG.getString("MCR.recycleBin", "data" + FS + "recycleBin");

    @Override
    public void sdelobj(MCRServletJob job, CommonData cd) throws IOException {
        if (!MCRAccessManager.checkPermission(cd.mytfmcrid, "deletedb")) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + usererrorpage));
            return;
        }
        if (cd.mytfmcrid.length() == 0) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + mcriderrorpage));
            return;
        }
        if(hasLinks(cd.mytfmcrid)) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + linkErrorPage));
            return;
        }
        
        try {
            MCRObject obj = new MCRObject();
            obj.receiveFromDatastore(cd.mytfmcrid);
            ArrayList<MCRObject> childs = getChilds(obj);

            // check if childs are linked
            boolean childLinked = false;
            for(MCRObject child : childs) {
                if(hasLinks(child.getId().getId()))
                    childLinked = true;
            }
            if(childLinked == true) {
                job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + childlinkedErrorPage));
                return;   
            }

            // get some useful infos
            MCRSession session = MCRSessionMgr.getCurrentSession();
            String user = session.getCurrentUserID();
            String userRealName = MCRUserMgr.instance().retrieveUser(user).getUserContact().getFirstName() + " "
                    + MCRUserMgr.instance().retrieveUser(user).getUserContact().getLastName();

            // delete all objects (set flags)
            deleteObject(obj, userRealName, user);
            for(MCRObject child : childs) {
                deleteObject(child, userRealName, user);
            }

            cd.myfile = deletepage;
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                e.printStackTrace();
            } else {
                LOGGER.error(e.getMessage());
            }
            cd.myfile = deleteerrorpage;
        }
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + cd.myfile));
    }

    /**
     * Returns all children of a MCRObject
     * @param mcrObj the parent MCRObject
     * @return all childs
     */
    protected ArrayList<MCRObject> getChilds(MCRObject mcrObj) {
        ArrayList<MCRObject> childs = new ArrayList<MCRObject>();
        MCRObjectStructure struct = mcrObj.getStructure();
        for(int i = 0; i < struct.getChildSize(); i++) {
            String childID = struct.getChild(i).getXLinkHref();
            MCRObject mcrChild = new MCRObject();
            mcrChild.receiveFromDatastore(childID);
            childs.add(mcrChild);
            childs.addAll(getChilds(mcrChild));
        }
        return childs;
    }

    protected void deleteObject(MCRObject mcrObj, String userRealName, String user) {
        MCRObjectService service = mcrObj.getService();
        if(!service.isFlagSet("deleted")) {
            service.addFlag("deleted");
            service.addFlag("deletedFrom", userRealName + "(" + user + ")");
        }
        try {
            mcrObj.updateInDatastore();
        } catch(Exception exc) {
            LOGGER.error("Exception occurred durring deleting object " + mcrObj);
        }
    }

    protected boolean hasLinks(String id) {
        int linkCount = MCRLinkTableManager.instance().countReferenceLinkTo(id);
        if(linkCount > 0)
            return true;
        return false;
    }

    /**
     * Exports the metadata object before deleting in database.
     */
    public void exportAndDelete(MCRServletJob job, CommonData cd) throws IOException {
        if (!MCRAccessManager.checkPermission(cd.mytfmcrid, "deletedb")) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + usererrorpage));
            return;
        }
        if (cd.mytfmcrid.length() == 0) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + mcriderrorpage));
            return;
        }

        MCRObject obj = new MCRObject();
        // export
        try {
            obj.receiveFromDatastore(cd.mytfmcrid);
            Document doc = obj.createXML();
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            FileOutputStream output = new FileOutputStream(recycleDir + FS + obj.getLabel() + ".xml");
            outputter.output(doc, output);
        } catch(Exception e) {
            if (LOGGER.isDebugEnabled()) {
                e.printStackTrace();
            } else {
                LOGGER.error(e.getMessage());
            }
            cd.myfile = exportErrorPage;
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + cd.myfile));
            return;
        }
        // delete
        try {
            obj.deleteFromDatastore(cd.mytfmcrid);
            cd.myfile = deletepage;
        } catch(Exception e) {
            if (LOGGER.isDebugEnabled()) {
                e.printStackTrace();
            } else {
                LOGGER.error(e.getMessage());
            }
            cd.myfile = deleteerrorpage;
        }
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + cd.myfile));
    }

    /**
     * Removes the 'deleted' flag of a metadata object. The object is now
     * visible for all search querys.
     */
    public void srestoreobj(MCRServletJob job, CommonData cd) throws IOException {
        if (cd.mytfmcrid.length() == 0) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + mcriderrorpage));
            return;
        }
        try {
            MCRObject obj = new MCRObject();
            obj.receiveFromDatastore(cd.mytfmcrid);
            MCRObjectService service = obj.getService();
            if(service.isFlagSet("deleted")) {
                service.removeFlags("deletedFrom");
                int index = service.getFlagIndex("deleted");
                if(index != -1)
                    service.removeFlag(index);
            }
            obj.updateInDatastore();
            cd.myfile = restorePage;
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                e.printStackTrace();
            } else {
                LOGGER.error(e.getMessage());
            }
            cd.myfile = restoreErrorPage;
        }
        // send response
        job.getRequest().setAttribute("XSL.restoredObject", cd.mytfmcrid);
        Element element = MCRURIResolver.instance().resolve("webapp:" + cd.myfile);
        getLayoutService().doLayout(job.getRequest(), job.getResponse(), new Document(element));
    }
}