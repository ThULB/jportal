package fsu.jportal.resources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.common.MCRUtils;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.frontend.jersey.MCRJerseyUtil;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.SmLink;
import org.mycore.mets.validator.METSValidator;
import org.mycore.mets.validator.validators.ValidationException;

import com.google.gson.JsonObject;

import fsu.jportal.backend.JPComponent;
import fsu.jportal.mets.ConvertException;
import fsu.jportal.mets.LLZMetsConverter;
import fsu.jportal.mets.LLZMetsImporter;
import fsu.jportal.mets.LLZMetsUtils;

@Path("mets/llz")
public class METSImportResource {

    private static Logger LOGGER = LogManager.getLogger(METSImportResource.class);

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

//    @POST
//    @Path("import/{id}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public String importMets(@PathParam("id") String derivateId) throws FileNotFoundException, IOException,
//        JDOMException, ConvertException {
////        checkPermission(derivateId);
////        // load mets
////        Document uibkMets = LLZMetsUtils.getMetsXMLasDocument(derivateId);
////        // import
////        LLZMetsImporter importer = new LLZMetsImporter();
//////        importer.importMets(uibkMets, MCRObjectID.getInstance(derivateId));
//        Gson gson = MCRJSONManager.instance().createGson();
////        return gson.toJson(importer.getErrorList()).toString();
//        return gson.toString();
//    }

    @POST
    @Path("convert/{id}")
    public void convertAndImport(@PathParam("id") String derivateId) throws TransformerException, IOException, JDOMException,
        ConvertException, ValidationException {
        Mets mcrMets = convertMets(derivateId);
        // validate
        METSValidator validator = new METSValidator(mcrMets.asDocument());
        List<ValidationException> exceptionList = validator.validate();
        if (!exceptionList.isEmpty()) {
            for(Exception exc : exceptionList) {
                LOGGER.error("while validating mets.xml of derivate " + derivateId, exc);
            }
            throw exceptionList.get(0);
        }
        // import mets
        LLZMetsImporter importer = new LLZMetsImporter();
        Map<LogicalDiv, JPComponent> logicalComponentMap = importer.importMets(mcrMets, MCRObjectID.getInstance(derivateId));
        // update logical id's
        updateLogicalIds(mcrMets, logicalComponentMap);
        // write mets.xml
        Document mcrDoc = mcrMets.asDocument();
        byte[] bytes = new MCRJDOMContent(mcrDoc).asByteArray();
        MCRPath path = MCRPath.getPath(derivateId, "/mets.xml");
        Files.write(path, bytes);
    }

    /**
     * This method updates the divs of the logical struct map and the struct link section with mycore ids.
     * This is important because then we know which logical div is assigned to which mycore object.
     * 
     * @param mets the mets to update
     * @param logicalComponentMap a map of logical divs and their corresponding <code>JPComponent's<code>
     */
    private void updateLogicalIds(Mets mets, Map<LogicalDiv, JPComponent> logicalComponentMap) {
        // the import is done -> now update the mets document with the mycore id's
        for (Entry<LogicalDiv, JPComponent> entry : logicalComponentMap.entrySet()) {
            LogicalDiv logicalDiv = entry.getKey();
            String mycoreId = entry.getValue().getObject().getId().toString();
            String oldId = logicalDiv.getId();
            // update logical div
            logicalDiv.setId(mycoreId);
            // update logical struct map
            List<SmLink> links = mets.getStructLink().getSmLinkByFrom(oldId);
            for(SmLink link : links) {
                link.setFrom(mycoreId);
            }
        }
    }

    @GET
    @Path("print/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public String print(@PathParam("id") String derivateId) throws IOException, JDOMException, ConvertException {
        Document mcrMets = convertMets(derivateId).asDocument();
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        StringWriter writer = new StringWriter();
        out.output(mcrMets, writer);
        return writer.toString();
    }

    private Mets convertMets(String derivateId) throws IOException, JDOMException,
        ConvertException {
        checkPermission(derivateId);
        MCRJerseyUtil.checkPermission(derivateId, "writedb");
        // load mets
        Document uibkMets = LLZMetsUtils.getMetsXMLasDocument(derivateId);
        // convert
        LLZMetsConverter converter = new LLZMetsConverter();
        return converter.convert(uibkMets);
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
