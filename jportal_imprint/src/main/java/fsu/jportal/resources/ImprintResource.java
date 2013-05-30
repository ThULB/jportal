package fsu.jportal.resources;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMResult;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.xml.MCRLayoutService;
import org.mycore.datamodel.common.MCRLinkTableManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.MCRWebpage;

import com.google.gson.Gson;

import fsu.jportal.xml.XMLTools;

@Path("imprint")
//@MCRRestrictedAccess(IPRuleAccess.class)
public class ImprintResource {

    private static final Logger LOGGER = Logger.getLogger(ImprintResource.class);

    public static final String IMPRINT_TYPE = "imprint";

    @GET
    @Path("retrieve/{imprintID}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response retrieve(@PathParam("imprintID") String imprintID) {
        JDOMSource xmlSource = null;
        try {
            xmlSource = ImprintFS.receive(imprintID);
        } catch(JDOMException jdomExc) {
            LOGGER.error("unable to parse imprint webpage of " + imprintID, jdomExc);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        } catch (Exception exc) {
            LOGGER.error("while retrieving imprint " + imprintID, exc);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
        Element section = xmlSource.getDocument().getRootElement().getChild("section");
        if (section == null) {
            LOGGER.error("unable to get section of imprint " + imprintID);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
        XMLOutputter xout = new XMLOutputter(Format.getCompactFormat());
        return Response.ok(xout.outputString(section.getContent())).build(); 
    }

    @POST
    @Path("save/{imprintID}")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response save(@PathParam("imprintID") String imprintID, String content) {
        try {
            MCRWebpage mcrWebpage = new MCRWebpage();
            mcrWebpage.addSection(new MCRWebpage.Section().addContent(content));
            ImprintFS.store(imprintID, new MCRJDOMContent(mcrWebpage.toXML()));
        } catch (Exception exc) {
            LOGGER.error("unable to store imprint content '" + imprintID + "'", exc);
            throw new WebApplicationException(exc, Status.INTERNAL_SERVER_ERROR);
        }
        return Response.ok().build();
    }

    @DELETE
    @Path("delete/{imprintID}")
    public Response delete(@PathParam("imprintID") String imprintID) {
        // delete file
        try {
            ImprintFS.delete(imprintID);
        } catch(Exception exc) {
            LOGGER.error("unable to delete imprint file '" + imprintID + "'", exc);
            throw new WebApplicationException(exc, Status.INTERNAL_SERVER_ERROR);
        }
        // remove links
        MCRLinkTableManager ltm = MCRLinkTableManager.instance();
        Collection<String> references = ltm.getSourceOf(imprintID);
        for(String reference : references) {
            ltm.deleteReferenceLink(reference, imprintID, IMPRINT_TYPE);    
        }
        return Response.ok().build();
    }

    /**
     * Renders a imprint as webpage.
     * 
     * @param objID
     * @return
     */
    @GET
    @Path("webpage/{objID}")
    @Produces(MediaType.TEXT_HTML)
    public Response webpage(@PathParam("objID") String objID, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        String imprintID = getImprintID(objID);
        Source xmlSource = null;
        try {
            xmlSource = ImprintFS.receive(imprintID);
        } catch (Exception exc) {
            LOGGER.error("while retrieving imprint " + imprintID, exc);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
        InputStream guiXSL = getClass().getResourceAsStream("/jportal_imprint/gui/xsl/webpage.xsl");
        JDOMResult result = new JDOMResult();
        Map<String, Object> params = new HashMap<>();
        params.put("journalID", objID);
        try {
            new XMLTools().transform(xmlSource, new StreamSource(guiXSL), params, result);
            MCRLayoutService.instance().doLayout(request, response, new MCRJDOMContent(result.getDocument()));
        } catch (Exception exc) {
            LOGGER.error("while transform imprint " + objID, exc);
            throw new WebApplicationException(exc, Status.INTERNAL_SERVER_ERROR);
        }
        return Response.ok().build();
    }

    /**
     * Returns a list of all imprint id's.
     * 
     * @return
     */
    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() {
        List<String> idList;
        try {
            idList = ImprintFS.list();
        } catch (Exception exc) {
            LOGGER.error("while retrieving imprint list", exc);
            throw new WebApplicationException(exc, Status.INTERNAL_SERVER_ERROR);
        }
        return Response.ok(new Gson().toJson(idList)).build();
    }

    /**
     * Returns the imprint id by the given object id.
     * 
     * @param objID
     * @return
     */
    @GET
    @Path("get/{objID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("objID") String objID) {
        String imprintID = getImprintID(objID);
        if (imprintID == null) {
            return Response.status(Status.NOT_FOUND).entity("no imprint id found").build();
        }
        return Response.ok(imprintID).build();
    }

    /**
     * Sets the link between the mycore object and the imprint.
     * If the imprintID is equals "null" the link will be removed.
     * 
     * @param objID
     * @param imprintID
     */
    @POST
    @Path("set")
    public void set(@QueryParam("objID") String objID, @QueryParam("imprintID") String imprintID) {
        MCRLinkTableManager ltm = MCRLinkTableManager.instance();
        String oldImprintID = getImprintID(objID);
        if (oldImprintID != null && oldImprintID.equals(imprintID)) {
            return;
        } else if (oldImprintID != null) {
            ltm.deleteReferenceLink(objID, oldImprintID, IMPRINT_TYPE);
        }
        if(!imprintID.equals("null")) {
            ltm.addReferenceLink(objID, imprintID, IMPRINT_TYPE, null);
        }
    }

    /**
     * Returns the imprint of the given object id or throws a 404 not
     * found web application exception.
     * 
     * @param objID mycore object id
     * @return id of imprint
     */
    protected String getImprintID(String objID) {
        MCRObjectID mcrObjID = MCRObjectID.getInstance(objID);
        Collection<String> c = MCRLinkTableManager.instance().getDestinationOf(mcrObjID, IMPRINT_TYPE);
        if (c.isEmpty()) {
            return null;
        }
        return c.iterator().next();
    }

}
