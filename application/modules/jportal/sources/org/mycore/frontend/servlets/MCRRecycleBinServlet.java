package org.mycore.frontend.servlets;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.datamodel.common.MCRLinkTableManager;
import org.mycore.datamodel.metadata.MCRBase;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectService;

public class MCRRecycleBinServlet extends MCRServlet {

    private static final String FS = System.getProperty("file.seperator", "/");
    protected static String recycleBinExportDir = CONFIG.getString("MCR.recycleBinExportDir", "data" + FS + "recycleBin");    
    protected static String recycleBinPage = CONFIG.getString("MCR.recycleBinPage", "content" + FS + "main" + FS + "recyclebin.xml");
    protected static String recycleBinDeletedPage = CONFIG.getString("MCR.recycleBinDeletedPage", "content" + FS + "main" + FS + "recycleBinDeletedPage.xml");

    @Override
    protected void doGetPost(MCRServletJob job) throws Exception {
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

        // the final return element
        Element returnElement = null;

        // if restore button pushed, restore all selectedobjects
        if (submitID.equals("Restore")) {
            for(String id : selectedObjects) {
                MCRBase baseObj = getMCRBaseInstance(id);
                baseObj.receiveFromDatastore(id);
                restoreObject(baseObj);
            }
            returnElement = MCRURIResolver.instance().resolve("webapp:" + recycleBinPage);
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
            // set xsl params
            job.getRequest().setAttribute("XSL.nonLinkedList", getStringList(deletedList));
            job.getRequest().setAttribute("XSL.linkedList", getStringList(linkedObjects));
            job.getRequest().setAttribute("XSL.errorList", getStringList(errorObjects));
            returnElement = MCRURIResolver.instance().resolve("webapp:" + recycleBinDeletedPage);
        }
        // wait 2 seconds -> updateInDatastore needs this time
        Thread.sleep(2000);
        // send response
        getLayoutService().doLayout(job.getRequest(), job.getResponse(), new Document(returnElement));
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