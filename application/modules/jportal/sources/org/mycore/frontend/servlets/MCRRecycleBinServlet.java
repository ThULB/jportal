package org.mycore.frontend.servlets;

import java.io.FileOutputStream;
import java.util.Enumeration;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectService;

public class MCRRecycleBinServlet extends MCRServlet {

    private static final String FS = System.getProperty("file.seperator", "/");
    protected static String recycleDir = CONFIG.getString("MCR.recycleBin", "data" + FS + "recycleBin");
    protected static String recyclePage = "content" + FS + "main" + FS + "recyclebin.xml";

    @Override
    protected void doGetPost(MCRServletJob job) throws Exception {
        String submitID = job.getRequest().getParameter("submit");
        if(submitID == null || submitID.equals("")) {
            // TODO - error page
            return;
        }
        // process the checkbox items
        Enumeration<String> e = job.getRequest().getParameterNames();
        while(e.hasMoreElements()) {
            String pName = e.nextElement();
            if(!pName.startsWith("cb_"))
                continue;
            String id = pName.replaceFirst("cb_", "");

            if(submitID.equals("Delete")) {
                exportAndDelete(id);
            } else if(submitID.equals("Restore")) {
                restoreObject(id);
            }
        }
        // wait 2 seconds -> updateInDatastore needs this time
        Thread.sleep(2000);        
        // send response
        Element element = MCRURIResolver.instance().resolve("webapp:" + recyclePage);
        getLayoutService().doLayout(job.getRequest(), job.getResponse(), new Document(element));
    }

    protected void exportAndDelete(String id) throws Exception {
        MCRObject obj = new MCRObject();
        // export
        obj.receiveFromDatastore(id);
        Document doc = obj.createXML();
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        FileOutputStream output = new FileOutputStream(recycleDir + FS + obj.getLabel() + ".xml");
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