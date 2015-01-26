package fsu.jportal.resources;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.jersey.MCRJerseyUtil;
import org.mycore.mets.validator.validators.ValidationException;

import com.google.gson.JsonObject;

import fsu.jportal.mets.ConvertException;
import fsu.jportal.mets.LLZMetsConverter;
import fsu.jportal.mets.LLZMetsImporter;
import fsu.jportal.mets.LLZMetsUtils;

@Path("mets/llz")
public class METSImportResource {

    private static Logger LOGGER = Logger.getLogger(METSImportResource.class);

    /**
     * Checks if the given derivate has a mets.xml which is importable.
     * 
     * @param derivateId the derivate to check
     * @return a json object in the form of
     * {
     *   check: true|false,
     *   error: "When check == false, the cause is given here"
     * }
     */
    @GET
    @Path("check/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String check(@PathParam("id") String derivateId) {
        checkPermission(derivateId);
        JsonObject returnObject = new JsonObject();
        try {
            Document doc = LLZMetsUtils.getMetsXMLasDocument(derivateId);
            LLZMetsUtils.deepCheck(doc);
        } catch (ValidationException ve) {
            returnObject.addProperty("error", ve.getMessage());
        } catch (Exception exc) {
            LOGGER.error("Unable to check mets.xml for derivate " + derivateId);
            throw new WebApplicationException(exc, Status.INTERNAL_SERVER_ERROR);
        }
        returnObject.addProperty("check", !returnObject.has("error"));
        return returnObject.toString();
    }

    @POST
    @Path("import/{id}")
    public void importMets(@PathParam("id") String derivateId) throws FileNotFoundException, IOException,
        JDOMException, ConvertException {
        checkPermission(derivateId);
        // load mets
        Document uibkMets = LLZMetsUtils.getMetsXMLasDocument(derivateId);
        // import
        LLZMetsImporter importer = new LLZMetsImporter();
        importer.importMets(uibkMets, MCRObjectID.getInstance(derivateId));
    }

    /**
     * 
     * @param derivateId
     * @return
     * @throws JDOMException 
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws ConvertException 
     */
    @GET
    @Path("convert/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public String convert(@PathParam("id") String derivateId) throws FileNotFoundException, IOException, JDOMException,
        ConvertException {
        checkPermission(derivateId);
        // load mets
        Document uibkMets = LLZMetsUtils.getMetsXMLasDocument(derivateId);
        // convert
        LLZMetsConverter converter = new LLZMetsConverter();
        Document mcrMets = converter.convert(uibkMets).asDocument();
        // output
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        StringWriter writer = new StringWriter();
        out.output(mcrMets, writer);
        return writer.toString();
    }

    /**
     * Checks if the user has the permissions to perform the task.
     * 
     * @param derivateId
     */
    protected void checkPermission(String derivateId) {
        MCRJerseyUtil.checkPermission(derivateId, "readdb");
        MCRObjectID mcrDerivateId = MCRObjectID.getInstance(derivateId);
        MCRDerivate derivate = MCRMetadataManager.retrieveMCRDerivate(mcrDerivateId);
        MCRObjectID ownerID = derivate.getOwnerID();
        MCRJerseyUtil.checkPermission(ownerID, "writedb");
    }

}
