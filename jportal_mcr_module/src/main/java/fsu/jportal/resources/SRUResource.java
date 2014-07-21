package fsu.jportal.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

import fsu.archiv.mycore.sru.impex.pica.model.PicaRecord;
import fsu.jportal.util.GndUtil;

@Path("sru")
public class SRUResource {

    static Logger LOGGER = Logger.getLogger(SRUResource.class);

    @GET
    @Path("search")
    @Produces(MediaType.APPLICATION_XML)
    public Response query(@QueryParam("q") String query) {
        PicaRecord picaRecord = null;
        try {
            picaRecord = GndUtil.retrieveFromSRU(query);
        } catch (Exception exc) {
            throw new WebApplicationException(exc, Response.status(Status.INTERNAL_SERVER_ERROR)
                .entity("unable to retrieve pica record (" + query + ") from sru interface").build());
        }
        try {
            Document mcrObjectXML = GndUtil.convertPicaRecord(picaRecord);
            Element returnElement = new Element("sruobjects");
            returnElement.addContent(mcrObjectXML.getRootElement().detach());
            XMLOutputter out = new XMLOutputter();
            return Response.ok(out.outputString(returnElement), MediaType.APPLICATION_XML).build();
        } catch (Exception exc) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(picaRecord);
            }
            throw new WebApplicationException(exc, Response.status(Status.INTERNAL_SERVER_ERROR)
                .entity("unable to parse pica record").build());
        }
    }

}
