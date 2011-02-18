package org.mycore.frontend.servlets;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRConfiguration;
import org.mycore.datamodel.common.MCRLinkTableManager;
import org.mycore.datamodel.metadata.MCRBase;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectService;

public class MCRRecycleBinServlet extends MCRServlet {

    private static final MCRConfiguration CONFIG = MCRConfiguration.instance();
	private static final String FS = System.getProperty("file.seperator", "/");
    private static final String webappsDir = CONFIG.getString("MCR.basedir") + FS + "build" + FS + "webapps" + FS;
    protected static String recycleBinExportDir = CONFIG.getString("MCR.recycleBinExportDir", "data" + FS + "recycleBin");    

    private static Logger LOGGER = Logger.getLogger(MCRRecycleBinServlet.class);
    
    @Override
    protected void doGetPost(MCRServletJob job) throws Exception {
        // check ACL
        if(!MCRAccessManager.checkPermission("deletedb")) {
            // TODO - error page
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + ""));
            return;
        }
        String submitID = job.getRequest().getParameter("submit");
        if(submitID == null || submitID.equals("")) {
            // TODO - error page
            return;
        }

        // get all selected checkbox items
        ArrayList<String> selectedObjects = new ArrayList<String>();
        Enumeration<String> e = job.getRequest().getParameterNames();
        while(e.hasMoreElements()) {
            String pName = e.nextElement();
            if(!pName.startsWith("cb_"))
                continue;
            String id = pName.replaceFirst("cb_", "");
            selectedObjects.add(id);
        }
        // if restore button pushed, restore all selectedobjects
        if (submitID.equals("Restore")) {
            for(String id : selectedObjects) {
                MCRBase baseObj = MCRMetadataManager.retrieve(MCRObjectID.getInstance(id));
                restoreObject(baseObj);
            }
            // wait 2 seconds -> updateInDatastore needs this time
            Thread.sleep(2000);
            // load recycle bin
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + "recycleBin.xml"));
        }
        // if delete button pushed, remove all derivates and all non linked mcrobjects
        else if(submitID.equals("Delete")) {
            // some lists :)
            ArrayList<String> linkedObjects = new ArrayList<String>();
            ArrayList<String> errorObjects = new ArrayList<String>();

            for(String id : selectedObjects) {
                MCRBase baseObj = MCRMetadataManager.retrieve(MCRObjectID.getInstance(id));
                // derivates will be always removed
                if(baseObj instanceof MCRDerivate) {
                    exportAndDelete(baseObj);
                } else {
                    // if the object is linked, add it to the linked list
                    if(hasLinks(id)) {
                        linkedObjects.add(id);
                        continue;
                    }
                    /* try to export and delete the object. if an error occur
                    add the id to the error list */
                    try {
                        exportAndDelete(baseObj);
                    } catch(Exception exc) {
                        LOGGER.error(exc);
                        errorObjects.add(id);
                    }
                }
            }
            // build a deleted list: all ((objects - linked objects) - error objects)
            ArrayList<String> deletedList = selectedObjects;
            deletedList.removeAll(linkedObjects);
            deletedList.removeAll(errorObjects);

            // create the result xml file
            createResultFile(deletedList, linkedObjects, errorObjects);
            // wait 2 seconds -> updateInDatastore needs this time
            Thread.sleep(2000);
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + "recycleBinResultPage.xml"));
        }
    }

    protected void createResultFile(ArrayList<String> deleted, ArrayList<String> linked, ArrayList<String> error) throws Exception {
        // create result file
        Element rootElement = new Element("recycleBinResultPage");
        Element deletedElement = new Element("deleted");
        Element linkedElement = new Element("linked");
        Element errorElement = new Element("error");
        rootElement.addContent(deletedElement);
        rootElement.addContent(linkedElement);
        rootElement.addContent(errorElement);

        for (String id : deleted) {
            Element entry = new Element("entry");
            entry.setAttribute("id", id);
            deletedElement.addContent(entry);
        }
        for (String id : linked) {
            Element entry = new Element("entry");
            entry.setAttribute("id", id);
            linkedElement.addContent(entry);
        }
        for (String id : error) {
            Element entry = new Element("entry");
            entry.setAttribute("id", id);
            errorElement.addContent(entry);
        }
        // write the xml document to the file system
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        FileOutputStream output = new FileOutputStream(webappsDir + "recycleBinResultPage.xml");
        outputter.output(new Document(rootElement), output);
    }

    /**
     * Creates a string from a list in the form: s1,s2,s3...
     * @param l the list with strings
     * @return a single line string
     */
    protected String getStringList(List<String> l) {
        String finalString = "";
        for(int i = 0; i < l.size(); i++) {
            finalString += l.get(i);
            if(i < l.size() - 1)
                finalString += ",";
        }
        return finalString;
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

    protected void exportAndDelete(MCRBase baseObj) throws Exception {
        Document doc = baseObj.createXML();
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        File dir = new File(recycleBinExportDir + FS);
        if(!dir.exists())
            dir.mkdirs();
        StringBuffer fileName = new StringBuffer(baseObj.getId().toString()).append(".xml");
        FileOutputStream output = new FileOutputStream(new File(dir, fileName.toString()));
        outputter.output(doc, output);
        // delete
        if(baseObj instanceof MCRObject) {
            MCRMetadataManager.delete((MCRObject)baseObj);
        } else if(baseObj instanceof MCRDerivate) {
            MCRMetadataManager.delete((MCRDerivate)baseObj);
        }
    }

    protected void restoreObject(MCRBase baseObj) throws Exception {
        MCRObjectService service = baseObj.getService();
        if(service.isFlagSet("deleted")) {
            service.removeFlags("deletedFrom");
            int index = service.getFlagIndex("deleted");
            if(index != -1)
                service.removeFlag(index);
        }
        if(baseObj instanceof MCRObject) {
            MCRMetadataManager.update((MCRObject)baseObj);
        } else if(baseObj instanceof MCRDerivate) {
            MCRMetadataManager.update((MCRDerivate)baseObj);
        }
    }
}