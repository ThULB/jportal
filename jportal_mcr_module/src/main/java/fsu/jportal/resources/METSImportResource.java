package fsu.jportal.resources;

import com.google.gson.JsonObject;
import fsu.jportal.mets.*;
import fsu.jportal.mets.MetsImportUtils.METS_TYPE;
import fsu.jportal.util.MetsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.mets.model.Mets;
import org.mycore.mets.validator.METSValidator;
import org.mycore.mets.validator.validators.ValidationException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

@Path("mets/import")
public class METSImportResource {

    private static Logger LOGGER = LogManager.getLogger(METSImportResource.class);

    /**
     * Checks if the given derivate has a mets.xml which is importable.
     * Also checks if the mets.xml is valid.
     * 
     * @param derivateId the derivate to check
     * @return a json object in the form of
     * {
     *   type: llz|jvb|unknown,
     *   error: if some validation error occur
     * }
     */
    @GET
    @Path("check/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String check(@PathParam("id") String derivateId) {
        MetsImportUtils.checkPermission(derivateId);
        JsonObject returnObject = new JsonObject();
        Document doc;
        try {
            doc = MetsUtil.getMetsXMLasDocument(derivateId);
        } catch (Exception exc) {
            LOGGER.error("Unable to check mets.xml for derivate " + derivateId);
            throw new WebApplicationException(exc, Status.INTERNAL_SERVER_ERROR);
        }
        METS_TYPE type = MetsImportUtils.determineType(doc);
        if (!type.equals(METS_TYPE.unknown)) {
            try {
                Mets mets = convert(derivateId, doc, true);
                validate(derivateId, mets);
            } catch (BlockReferenceException bre) {
                LOGGER.error("Unable to convert mets.xml", bre);
                returnObject.add("error",
                    MetsImportUtils.buildBlockReferenceError(bre.getMessage(), bre.getDivIds(), doc));
            } catch (Exception exc) {
                LOGGER.error("Unable to convert mets.xml", exc);
                returnObject.addProperty("error", exc.getMessage());
            }
        }
        returnObject.addProperty("type", type.name());
        return returnObject.toString();
    }

    @POST
    @Path("import/{id}")
    public void convertAndImport(@PathParam("id") String derivateId)
        throws IOException, JDOMException, ConvertException, MetsImportException {
        // check permissions
        MetsImportUtils.checkPermission(derivateId);

        // build document stuff
        Document enmapMetsXML = MetsUtil.getMetsXMLasDocument(derivateId);
        Mets mcrMets = convert(derivateId, enmapMetsXML, false);
        METS_TYPE type = MetsImportUtils.determineType(enmapMetsXML);

        // import mets
        MetsImporter importer;
        if (type.equals(METS_TYPE.jvb)) {
            importer = new JVBMetsImporter();
        } else if (type.equals(METS_TYPE.llz)) {
            importer = new LLZMetsImporter();
        } else if(type.equals(METS_TYPE.perthes)) {
            importer = new PerthesMetsImporter();
        } else {
            throw new MetsImportException("Unable to get importer class due type is not supported: " + type);
        }
        MetsImportUtils.importMets(derivateId, mcrMets, importer);
    }

    /**
     * Converts the given mets.xml and prints it. This method is for testing purpose only.
     * 
     * @param derivateId the derivate where the mets.xml is stored
     * @return mets.xml in mycore format
     *  
     * @throws IOException
     * @throws JDOMException
     * @throws ConvertException the converting process went wrong
     */
    @GET
    @Path("print/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public String print(@PathParam("id") String derivateId) throws IOException, JDOMException, ConvertException {
        MetsImportUtils.checkPermission(derivateId);
        Document metsXML = MetsUtil.getMetsXMLasDocument(derivateId);
        Mets mets = convert(derivateId, metsXML, false);
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        StringWriter writer = new StringWriter();
        out.output(mets.asDocument(), writer);
        return writer.toString();
    }

    /**
     * Converts the given enmap mets to an mcr one.
     * 
     * @param derivateId derivateId to convert
     * @param enmapMetsXML mets.xml (ENMAP format)
     * @param failEasy should the convert process fail easy (break on small bugs or not)
     * 
     * @return the converted mycore mets.xml as java object
     */
    private Mets convert(String derivateId, Document enmapMetsXML, boolean failEasy) throws ConvertException {
        ENMAPConverter converter = MetsImportUtils.getConverter(enmapMetsXML);
        converter.setFailEasyOnStructLinkGeneration(failEasy);
        converter.setFailOnEmptyAreas(failEasy);
        return converter.convert(enmapMetsXML, MCRPath.getPath(derivateId, "/"));
    }

    /**
     * Validates the given mets document.
     * 
     * @param derivateId
     * @param mets
     * @throws JDOMException
     * @throws IOException
     * @throws TransformerException
     * @throws ValidationException
     */
    private void validate(String derivateId, Mets mets)
        throws JDOMException, IOException, TransformerException, ValidationException {
        METSValidator validator = new METSValidator(mets.asDocument());
        List<ValidationException> exceptionList = validator.validate();
        if (!exceptionList.isEmpty()) {
            for (Exception exc : exceptionList) {
                LOGGER.error("while validating mets.xml of derivate " + derivateId, exc);
            }
            throw exceptionList.get(0);
        }
    }

}
