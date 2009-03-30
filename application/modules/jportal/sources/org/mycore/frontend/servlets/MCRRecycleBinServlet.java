package org.mycore.frontend.servlets;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRConfiguration;
import org.mycore.datamodel.common.MCRLinkTableManager;
import org.mycore.datamodel.metadata.MCRBase;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectService;

public class MCRRecycleBinServlet extends MCRServlet {

    private static final String FS = System.getProperty("file.seperator", "/");
    private static final String webappsDir = MCRConfiguration.instance().getString("MCR.basedir") + FS + "build" + FS + "webapps" + FS;
    protected static String recycleBinExportDir = CONFIG.getString("MCR.recycleBinExportDir", "data" + FS + "recycleBin");    
    protected static String recycleBinDeletedPage = CONFIG.getString("MCR.recycleBinDeletedPage", "content" + FS + "main" + FS + "recycleBinDeletedPage.xml");

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
                MCRBase baseObj = getMCRBaseInstance(id);
                baseObj.receiveFromDatastore(id);
                restoreObject(baseObj);
            }
            // wait 2 seconds -> updateInDatastore needs this time
            Thread.sleep(2000);
            // load recycle bin
            job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(getBaseURL() + "servlets/MCRRecycleBinPageGenerationServlet"));
        }
        // if delete button pushed, remove all derivates and all non linked mcrobjects
        else if(submitID.equals("Delete")) {
            // some lists :)
            ArrayList<String> linkedObjects = new ArrayList<String>();
            ArrayList<String> errorObjects = new ArrayList<String>();

            for(String id : selectedObjects) {
                MCRBase baseObj = getMCRBaseInstance(id);
                // derivates will be always removed
                if(baseObj instanceof MCRDerivate) {
                    baseObj.receiveFromDatastore(id);
                    exportAndDelete(baseObj);
                    continue;
                }
                // if the object is linked, add it to the linked list
                if(hasLinks(id)) {
                    linkedObjects.add(id);
                    continue;
                }
                /* try to export and delete the object. if an error occur
                add the id to the error list */
                try {
                    baseObj.receiveFromDatastore(id);
                    exportAndDelete(baseObj);
                } catch(Exception exc) {
                    errorObjects.add(id);
                }
            }
            // build a deleted list: all ((objects - linked objects) - error objects)
            ArrayList<String> deletedList = selectedObjects;
            deletedList.removeAll(linkedObjects);
            deletedList.removeAll(errorObjects);

            // create the result xml file
            createResultFile(deletedList, linkedObjects, errorObjects);
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

    /**
     * Creates an instance from the given id.
     * @param
     * @return
     */
    protected MCRBase getMCRBaseInstance(String id) {
        MCRBase obj = null;
        if(id.contains("derivate"))
            obj = new MCRDerivate();
        else
            obj = new MCRObject();
        return obj;
    }

    protected void exportAndDelete(MCRBase baseObj) throws Exception {
        Document doc = baseObj.createXML();
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        FileOutputStream output = new FileOutputStream(recycleBinExportDir + FS + baseObj.getLabel() + ".xml");
        outputter.output(doc, output);
        // delete
        baseObj.deleteFromDatastore(baseObj.getId().getId());
    }

    protected void restoreObject(MCRBase baseObj) throws Exception {
        MCRObjectService service = baseObj.getService();
        if(service.isFlagSet("deleted")) {
            service.removeFlags("deletedFrom");
            int index = service.getFlagIndex("deleted");
            if(index != -1)
                service.removeFlag(index);
        }
        baseObj.updateInDatastore();
    }
}