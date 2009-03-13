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

        ArrayList<String> linkedObjects = new ArrayList<String>();
        ArrayList<String> nonLinkedObjects = new ArrayList<String>();
        ArrayList<String> errorObjects = new ArrayList<String>();

        // process the checkbox items
        Enumeration<String> e = job.getRequest().getParameterNames();
        while(e.hasMoreElements()) {
            String pName = e.nextElement();
            if(!pName.startsWith("cb_"))
                continue;
            String id = pName.replaceFirst("cb_", "");
            if(hasLinks(id))
                linkedObjects.add(id);
            else
                nonLinkedObjects.add(id);
        }

        // delete all non linked objects
        ArrayList<String> tempNonLinked = (ArrayList<String>)nonLinkedObjects.clone();
        for (String id : tempNonLinked) {
            if (submitID.equals("Delete")) {
                try {
                    exportAndDelete(id);
                } catch(Exception exc) {
                    errorObjects.add(id);
                    nonLinkedObjects.remove(id);
                }
            } else if (submitID.equals("Restore"))
                restoreObject(id);
        }

        // wait 2 seconds -> updateInDatastore needs this time
        Thread.sleep(2000);
        // send response
        if(submitID.equals("Restore")) {
            Element element = MCRURIResolver.instance().resolve("webapp:" + recycleBinPage);
            getLayoutService().doLayout(job.getRequest(), job.getResponse(), new Document(element));
        } else {
            job.getRequest().setAttribute("XSL.nonLinkedList", getStringList(nonLinkedObjects));
            job.getRequest().setAttribute("XSL.linkedList", getStringList(linkedObjects));
            job.getRequest().setAttribute("XSL.errorList", getStringList(errorObjects));
            Element element = MCRURIResolver.instance().resolve("webapp:" + recycleBinDeletedPage);
            getLayoutService().doLayout(job.getRequest(), job.getResponse(), new Document(element));
        }
    }

    protected String getStringList(List<String> l) {
        String finalString = "";
        for(int i = 0; i < l.size(); i++) {
            finalString += l.get(i);
            if(i < l.size() - 1)
                finalString += ",";
        }
        return finalString;
    }

    protected boolean hasLinks(String id) {
        int linkCount = MCRLinkTableManager.instance().countReferenceLinkTo(id);
        if(linkCount > 0)
            return true;
        return false;
    }

    protected void exportAndDelete(String id) throws Exception {
        MCRObject obj = new MCRObject();
        // export
        obj.receiveFromDatastore(id);
        Document doc = obj.createXML();
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        FileOutputStream output = new FileOutputStream(recycleBinExportDir + FS + obj.getLabel() + ".xml");
        outputter.output(doc, output);
        // delete
        obj.deleteFromDatastore(id);
    }

    protected void restoreObject(String id) throws Exception {
        MCRObject obj = new MCRObject();
        obj.receiveFromDatastore(id);
        MCRObjectService service = obj.getService();
        if(service.isFlagSet("deleted")) {
            service.removeFlags("deletedFrom");
            int index = service.getFlagIndex("deleted");
            if(index != -1)
                service.removeFlag(index);
        }
        obj.updateInDatastore();
    }
}