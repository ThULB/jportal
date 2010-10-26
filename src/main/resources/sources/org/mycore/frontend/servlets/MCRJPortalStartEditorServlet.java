package org.mycore.frontend.servlets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.datamodel.common.MCRLinkTableManager;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFile;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRBase;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectService;
import org.mycore.datamodel.metadata.MCRObjectStructure;
import org.mycore.user.MCRUserMgr;

/**
 * Editor servlet for JPortal which extends the delete action.
 * @author Matthias Eichner
 */
public class MCRJPortalStartEditorServlet extends MCRStartEditorServlet {

    private static final long serialVersionUID = 1L;
    private static final MCRConfiguration CONFIG = MCRConfiguration.instance();
    private static final String FS = System.getProperty("file.seperator", "/");
    protected static String restorePage = pagedir + CONFIG.getString("MCR.SWF.PageRestore", "editor_restore.xml");
    protected static String restoreErrorPage = pagedir + CONFIG.getString("MCR.SWF.PageErrorRestore", "editor_error_restore.xml");
    protected static String exportErrorPage = pagedir + CONFIG.getString("MCR.SWF.PageErrorExport", "editor_error_export.xml");
    protected static String linkErrorPage = pagedir + CONFIG.getString("MCR.SWF.PageErrorLink", "editor_error_link.xml");
    protected static String childlinkedErrorPage = pagedir + CONFIG.getString("MCR.SWF.PageErrorChildLinked", "editor_error_childlinked.xml");

    protected static String recycleDir = CONFIG.getString("MCR.recycleBin", "data" + FS + "recycleBin");
    
    protected static String filestoreDir = CONFIG.getString("MCR.IFS.ContentStore.FS.URI");

    /**
     * Sets the deleted flag for the delivered mcrobject and all his children.
     */
    @Override
    public void sdelobj(MCRServletJob job, CommonData cd) throws IOException {
        if (!MCRAccessManager.checkPermission(cd.mytfmcrid, "deletedb")) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + usererrorpage));
            return;
        }
        if (cd.mytfmcrid.getId().length() == 0) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + mcriderrorpage));
            return;
        }
        if(hasLinks(cd.mytfmcrid.getId())) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + linkErrorPage));
            return;
        }
        
        try {
            MCRObject obj = new MCRObject();
            obj.receiveFromDatastore(cd.mytfmcrid);
            ArrayList<MCRObject> childs = getNonDeletedChilds(obj);

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
            markAsDeleted(obj, userRealName, user);
            for(MCRObject child : childs) {
                markAsDeleted(child, userRealName, user);
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

    
    @Override
    public void seditder(MCRServletJob job, CommonData cd) throws IOException {
        if (!MCRAccessManager.checkPermission(cd.myremcrid.getId(), "writedb")) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + usererrorpage));
            return;
        }
        if (!cd.mysemcrid.isValid()) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + mcriderrorpage));
            return;
        }
        StringBuffer sb = new StringBuffer();
        Properties params = new Properties();
        sb.append("request:receive/").append(cd.mysemcrid).append("?XSL.Style=editor");
        params.put("sourceUri", sb.toString());
        sb = new StringBuffer();
        sb.append(getBaseURL()).append("receive/").append(cd.myremcrid.getId());
        params.put("cancelUrl", sb.toString());
        params.put("se_mcrid", cd.mysemcrid.getId());
        params.put("re_mcrid", cd.myremcrid.getId());
        params.put("type", cd.mytype);
        params.put("step", cd.mystep);
        sb = new StringBuffer();
        sb.append(getBaseURL()).append(pagedir).append("jp_editor_form_commit-derivate.xml");
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(buildRedirectURL(sb.toString(), params)));
    }

    /**
     * Sets the deleted flag for the delivered derivate. If the derivate has no
     * content (file deleted) then it will be completly removed.
     */
    @Override
    public void sdelder(MCRServletJob job, CommonData cd) throws IOException {
        if (!MCRAccessManager.checkPermission(cd.myremcrid, "deletedb")) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + usererrorpage));
            return;
        }
        if (cd.mysemcrid.getId().length() == 0) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + mcriderrorpage));
            return;
        }

        // get some useful infos
        MCRSession session = MCRSessionMgr.getCurrentSession();
        String user = session.getCurrentUserID();
        String userRealName = MCRUserMgr.instance().retrieveUser(user).getUserContact().getFirstName() + " "
                + MCRUserMgr.instance().retrieveUser(user).getUserContact().getLastName();

        MCRDerivate der = new MCRDerivate();
        try {
            // get the derivate from db
            der.receiveFromDatastore(cd.mysemcrid);
            // get file/directory
            MCRFilesystemNode fileNode = MCRFilesystemNode.getRootNode(der.getId().getId());

            // check if derivate has files left.
            if(deleteZombieFiles(fileNode))
                // delete the whole derivate
                der.deleteFromDatastore(cd.mysemcrid.getId());
            else
                // set deleted flag
                markAsDeleted(der, userRealName, user);
            StringBuffer sb = new StringBuffer();
            sb.append("receive/").append(cd.myremcrid);
            cd.myfile = sb.toString();
        } catch (Exception e) {
            cd.myfile = deleteerrorpage;
        }
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + cd.myfile));
    }

    @Override
    public void sdelfile(MCRServletJob job, CommonData cd) throws IOException {
        if (!MCRAccessManager.checkPermission(cd.mysemcrid.getId(), "deletedb")) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + usererrorpage));
            return;
        }
        if (!cd.mysemcrid.isValid()) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + mcriderrorpage));
            return;
        }

        int all = 0;

        int i = cd.extparm.indexOf("####nrall####");
        int j = 0;

        if (i != -1) {
            j = cd.extparm.indexOf("####", i + 13);
            all = Integer.parseInt(cd.extparm.substring(i + 13, j));
        }

        i = cd.extparm.indexOf("####nrthe####");

        if (i != -1) {
            j = cd.extparm.indexOf("####", i + 13);
            Integer.parseInt(cd.extparm.substring(i + 13, j));
        }

        if (all > 1) {
            i = cd.extparm.indexOf("####filename####");

            if (i != -1) {
                String filename = cd.extparm.substring(i + 16, cd.extparm.length());

                try {
                    MCRDirectory rootdir = MCRDirectory.getRootDirectory(cd.mysemcrid.getId());
                    rootdir.getChildByPath(filename).delete();
                } catch (Exception ex) {
                    LOGGER.warn("Can't remove file " + filename, ex);
                }
            }
        }

        StringBuffer sb = new StringBuffer();
        sb.append(getBaseURL()).append("servlets/MCRFileNodeServlet/").append(cd.mysemcrid).append("/?hosts=local");
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(sb.toString()));
    }

    /**
     * TODO: change this if ifs2 comes -> need to be solved global!
     * Removes all zombie entries in database.
     * @param node the root file node
     * @return returns true if all files of the derivate are deleted,
     * if files remain false is returned 
     */
    protected boolean deleteZombieFiles(MCRFilesystemNode node) {
        return deleteZombieFiles(node, new ZombieFileStatus());
    }

    private boolean deleteZombieFiles(MCRFilesystemNode node, ZombieFileStatus status) {
        if(node instanceof MCRDirectory) {
            // do recursive search through directories
            MCRDirectory dir = (MCRDirectory)node;
            for(MCRFilesystemNode childs : dir.getChildren())
                deleteZombieFiles(childs, status);
        } else if(node instanceof MCRFile) {
            MCRFile file = (MCRFile)node;
            if(!fileExists(file)) {
                file.delete();
                status.deletedCount++;
            }
            status.fileCount++;
        }
        return status.deletedCount >= 1 && status.deletedCount == status.fileCount;
    }
    
    private class ZombieFileStatus {
        public int deletedCount = 0;
        public int fileCount = 0;
    }

    protected boolean fileExists(MCRFile file) {
        File f = new File(filestoreDir + FS + file.getStorageID());
        return f.exists();
    }

    /**
     * Returns all children of a MCRObject where the deleted flag is not set.
     * @param mcrObj the parent MCRObject
     * @return all childs
     */
    protected ArrayList<MCRObject> getNonDeletedChilds(MCRObject mcrObj) {
        ArrayList<MCRObject> childs = new ArrayList<MCRObject>();
        MCRObjectStructure struct = mcrObj.getStructure();
        for(int i = 0; i < struct.getChildSize(); i++) {
            String childID = struct.getChild(i).getXLinkHref();
            MCRObject mcrChild = new MCRObject();
            mcrChild.receiveFromDatastore(childID);
            // add only childs which are not deleted
            if(!isDeletedFlagSet(mcrChild))
                childs.add(mcrChild);
            childs.addAll(getNonDeletedChilds(mcrChild));
        }
        return childs;
    }

    /**
     * Checks if the deleted flag of an mcrobject is set.
     * @param obj the mcr object
     * @return returns true if the flag is set, otherwise false
     */
    protected boolean isDeletedFlagSet(MCRObject obj) {
        MCRObjectService service = obj.getService();
        if(service.isFlagSet("deleted"))
            return true;
        return false;
    }

    /**
     * Marks a MCRBaseObject as deleted.
     * @param mcrBase the base object which will be marked as deleted
     * @param userRealName the real name of the user who deletes this object
     * @param user the short user name
     */
    protected void markAsDeleted(MCRBase mcrBase, String userRealName, String user) {
        MCRObjectService service = mcrBase.getService();
        if(!service.isFlagSet("deleted")) {
            service.addFlag("deleted");
            service.addFlag("deletedFrom", userRealName + "(" + user + ")");
        }
        try {
            mcrBase.updateInDatastore();
            
            
        } catch(Exception exc) {
            LOGGER.error("Exception occurred durring deleting object " + mcrBase);
        }
    }

    /**
     * Checks if the object is linked to other objects
     * @param id the object id
     * @return if the object is linked
     */
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
        if (cd.mytfmcrid.getId().length() == 0) {
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
            obj.deleteFromDatastore(cd.mytfmcrid.getId());
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
     * Removes the 'deleted' flag of a metadata object. The object then is
     * visible for all search querys.
     */
    public void srestoreobj(MCRServletJob job, CommonData cd) throws IOException {
        if (cd.mytfmcrid.getId().length() == 0) {
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
  
    @Override
    public void seditobj(MCRServletJob job, CommonData cd) throws IOException {
        if (!MCRAccessManager.checkPermission(cd.mytfmcrid, "writedb")) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + usererrorpage));
            return;
        }
        if (cd.mytfmcrid.getId().length() == 0) {
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + mcriderrorpage));
            return;
        }

        StringBuffer sb = new StringBuffer();
        // TODO: should transform mcrobject and use "session:" to save roundtrip
        Properties params = new Properties();
        sb.append("request:receive/").append(cd.mytfmcrid).append("?XSL.Style=editor");
        params.put("sourceUri", sb.toString());
        sb = new StringBuffer();
        sb.append(job.getRequest().getHeader("Referer"));
        params.put("returnUrl", sb.toString());
        params.put("mcrid", cd.mytfmcrid.getId());
        params.put("type", cd.mytype);
        params.put("step", cd.mystep);
        String base = getBaseURL() + cd.myfile;
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(buildRedirectURL(base, params)));
    }
}