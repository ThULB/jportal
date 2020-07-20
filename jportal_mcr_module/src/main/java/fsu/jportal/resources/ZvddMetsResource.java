package fsu.jportal.resources;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import fsu.jportal.backend.MetadataManager;
import fsu.jportal.mets.ZVDDMetsGenerator;

@Path("mets/zvdd")
public class ZvddMetsResource {

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("{id}")
    public Response get(@PathParam("id") String id, @QueryParam("remotely") boolean remotely) {
        try {
            if (remotely) {
                MetadataManager.setRemotely(true);
            }

            ZVDDMetsGenerator metsGenerator = new ZVDDMetsGenerator();
            Document metsXML = metsGenerator.generateMets(id);
            return createResponseFromXML(metsXML);
        } catch (Exception e) {
            throw new BadRequestException(e);
        } finally {
            MetadataManager.setRemotely(false);
        }
    }

    private Response createResponseFromXML(Document metsXML) {
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        return Response.ok(out.outputString(metsXML), MediaType.APPLICATION_XML).build();
    }
}
