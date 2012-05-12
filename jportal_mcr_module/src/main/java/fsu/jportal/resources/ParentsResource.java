package fsu.jportal.resources;

import java.io.IOException;
import java.io.StringWriter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import fsu.jportal.resources.filter.MyCoReSecurityFilterFactory.MCRDBAccess;
import fsu.jportal.xml.XMLContentTools;

@Path("parents")
public class ParentsResource {

    @GET
    @Path("{childID}")
    @MCRDBAccess
    public String getParents(@PathParam("childID") String childID) throws IOException {
        Element parents = new XMLContentTools().getParents(childID);
        StringWriter stringWriter = new StringWriter();
        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        xmlOutputter.output(new Document(parents), stringWriter);
        return stringWriter.toString();
    }

}
