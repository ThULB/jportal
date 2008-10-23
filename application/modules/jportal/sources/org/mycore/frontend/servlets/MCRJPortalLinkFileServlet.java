package org.mycore.frontend.servlets;

import org.apache.log4j.Logger;
import org.apache.xpath.operations.String;

public class MCRJPortalLinkFileServlet extends MCRServlet {

    private static Logger LOGGER = Logger.getLogger(MCRJPortalLinkFileServlet.class);;

    private static final String IMAGE_MARKED_KEY = "XSL.MCR.Module-iview.markedImageURL";

    public void doGetPost(MCRServletJob job) throws IOException {
        HttpServletRequest request = job.getRequest();
        MCRSession session = MCRSessionMgr.getCurrentSession();
        String objectToBeLinked = request.getParameter("jportalObjectToBeLinked");
        String fileToBeLinked = (String) session.get(IMAGE_MARKED_KEY);

        // create xml containing link
        Element link = new Element("ifsLink");
        link.setAttribute("lang", "de", Namespace.XML_NAMESPACE);
        link.setText(fileToBeLinked);
        Element linkWrappingTag = new Element("ifsLinks");
        linkWrappingTag.setAttribute("class", "MCRMetaLangText");
        linkWrappingTag.addContent(link);

        // update object xml
        Document objectXML = MCRXMLTableManager.instance().retrieveAsJDOM(new MCRObjectID(objectToBeLinked));
        objectXML.getRootElement().getChild("metadata").addContent(linkWrappingTag);

        // save object
        XMLOutputter xout = new XMLOutputter();
        xout.output(objectXML, System.out);
        MCRObject mo ;
        mo.setFromJDOM(objectXML);
        mo.createInDatastore();

        
    }
}











