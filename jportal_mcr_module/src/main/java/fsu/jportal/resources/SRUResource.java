package fsu.jportal.resources;

import fsu.archiv.mycore.sru.impex.pica.model.PicaRecord;
import fsu.jportal.util.GndUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.mycore.common.MCRException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("sru")
public class SRUResource {

    static Logger LOGGER = LogManager.getLogger(SRUResource.class);

    @GET
    @Path("search")
    @Produces(MediaType.APPLICATION_XML)
    public Response query(@QueryParam("q") String query) {
        PicaRecord picaRecord = getPicaRecord(query);
        Document mcrObjectXML = toMCRObjectDocument(picaRecord);
        return renderXML(mcrObjectXML);
    }

    @GET
    @Path("check/{gnd}")
    public Response check(@PathParam("gnd") String gnd) {
        PicaRecord picaRecord = getPicaRecord(gnd);
        if (picaRecord == null) {
            return Response.status(Status.NOT_FOUND).entity("Catalog entry with " + gnd + " not found.").build();
        }
        return Response.ok().build();
    }

    private Response renderXML(Document mcrObjectXML) {
        try {
            Element returnElement = new Element("sruobjects");
            returnElement.addContent(mcrObjectXML.getRootElement().detach());
            XMLOutputter out = new XMLOutputter();
            return Response.ok(out.outputString(returnElement), MediaType.APPLICATION_XML).build();
        } catch (Exception exc) {
            throw new WebApplicationException(new MCRException("unable to parse pica record"),
                Status.INTERNAL_SERVER_ERROR);
        }
    }

    private Document toMCRObjectDocument(PicaRecord picaRecord) {
        try {
            return GndUtil.toMCRObjectDocument(picaRecord);
        } catch (Exception exc) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(picaRecord);
            }
            throw new WebApplicationException(new MCRException("unable to parse pica record"),
                Status.INTERNAL_SERVER_ERROR);
        }
    }

    private PicaRecord getPicaRecord(String gnd) {
        PicaRecord picaRecord = null;
        try {
            picaRecord = GndUtil.retrieveFromSRU(gnd);
        } catch (Exception exc) {
            throw new WebApplicationException(exc, Response.status(Status.INTERNAL_SERVER_ERROR)
                                                           .entity("unable to retrieve pica record (" + gnd
                                                               + ") from sru interface")
                                                           .build());
        }
        if (picaRecord == null) {
            throw new WebApplicationException(
                new MCRException("unable to retrieve pica record (" + gnd + ") from sru interface"), Status.NOT_FOUND);
        }
        return picaRecord;
    }
}
