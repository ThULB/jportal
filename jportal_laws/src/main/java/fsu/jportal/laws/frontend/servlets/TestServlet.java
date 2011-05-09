package fsu.jportal.laws.frontend.servlets;

import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;

import fsu.mycore.lawcoll.LawCollectionMetsCreator;

public class TestServlet extends MCRServlet {

    @Override
    protected void doGetPost(MCRServletJob job) throws Exception {
        String derivateId = job.getRequest().getParameter("derId");
        
        LawCollectionMetsCreator mc = new LawCollectionMetsCreator();
        Document doc = mc.createMets(derivateId);
        
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        out.output(doc, System.out);
        
        doc.toString();
    }
}
