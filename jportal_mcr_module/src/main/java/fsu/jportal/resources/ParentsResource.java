package fsu.jportal.resources;

import java.io.IOException;
import java.io.StringWriter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.commons.lang.time.StopWatch;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

@Path("parents")
public class ParentsResource {

    @GET
    @Path("{childID}")
    public String getParents(@PathParam("childID") String childID) throws IOException {
        Element parents = getParents1(childID);
        StringWriter stringWriter = new StringWriter();
        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        xmlOutputter.output(parents, stringWriter);
        return stringWriter.toString();
    }

    public Element getParents1(String childID) {
        Document childXML = MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(childID));
        Element parents = new Element("parents");
        try {
            XPath parentIdXpath = XPath.newInstance("/mycoreobject/structure/parents/parent[@inherited='0']");
            XPath titleXpath = XPath.newInstance("/mycoreobject/metadata/maintitles/maintitle[@inherited='0']/text()");
            while (true) {
                Element parent = (Element) parentIdXpath.selectSingleNode(childXML);
                if (parent != null) {
                    parent.setAttribute("inherited", String.valueOf(parents.getContentSize()));
                    parents.addContent(parent.detach());
                    childXML = MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(parent.getAttributeValue("href", MCRConstants.XLINK_NAMESPACE)));
                }else{
                    break;
                }
            }
        } catch (JDOMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return parents;
    }
}
