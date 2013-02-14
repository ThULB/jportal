package fsu.jportal.resources;

import java.io.IOException;
import java.io.StringWriter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import fsu.jportal.xml.XMLContentTools;

@Path("parents")
public class ParentsResource {

    @GET
    @Path("{childID}")
    public String getParents(@PathParam("childID") String childID) throws IOException {
        Element parents = new XMLContentTools().getParents(childID);
        StringWriter stringWriter = new StringWriter();
        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        xmlOutputter.output(new Document(parents), stringWriter);
        return stringWriter.toString();
    }

}
