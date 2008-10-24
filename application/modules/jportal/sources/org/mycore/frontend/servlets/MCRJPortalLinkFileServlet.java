package org.mycore.frontend.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.common.MCRXMLTableManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

public class MCRJPortalLinkFileServlet extends MCRServlet {

    private static Logger LOGGER = Logger.getLogger(MCRJPortalLinkFileServlet.class);;

    private static final java.lang.String IMAGE_MARKED_KEY = "XSL.MCR.Module-iview.markedImageURL";

    public void doGetPost(MCRServletJob job) throws IOException, MCRPersistenceException, MCRActiveLinkException {
        HttpServletRequest request = job.getRequest();
        MCRSession session = MCRSessionMgr.getCurrentSession();
        java.lang.String objectToBeLinked = (java.lang.String) request.getParameter("jportalObjectToBeLinked");
        java.lang.String fileToBeLinked = (java.lang.String) session.get(IMAGE_MARKED_KEY);

        // create xml containing link
        Element link = new Element("ifsLink");
        link.setAttribute("lang", "de", Namespace.XML_NAMESPACE);
        link.setText(fileToBeLinked);

        // update object xml
        Document objectXML = MCRXMLTableManager.instance().retrieveAsJDOM(new MCRObjectID(objectToBeLinked));
        boolean alreadyHasLink = false;
        if (null != objectXML.getRootElement().getChild("metadata").getChild("ifsLinks"))
            alreadyHasLink = true;
        if (alreadyHasLink) {
            objectXML.getRootElement().getChild("metadata").getChild("ifsLinks").addContent(link);
        } else {
            Element linkWrappingTag = new Element("ifsLinks");
            linkWrappingTag.setAttribute("class", "MCRMetaLangText");
            linkWrappingTag.addContent(link);
            objectXML.getRootElement().getChild("metadata").addContent(linkWrappingTag);
        }

        // save object
        XMLOutputter xout = new XMLOutputter();
        xout.output(objectXML, System.out);
        MCRObject mo = new MCRObject();
        mo.setFromJDOM(objectXML);
        mo.updateInDatastore();

        // get back to browser
        job.getResponse().sendRedirect(super.getBaseURL() + "receive/" + objectToBeLinked);
    }
}
