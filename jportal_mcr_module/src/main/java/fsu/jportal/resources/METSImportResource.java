package fsu.jportal.resources;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Text;
import org.jdom2.filter.Filters;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.mets.model.Mets;
import org.mycore.mets.validator.METSValidator;
import org.mycore.mets.validator.validators.ValidationException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import fsu.jportal.mets.BlockReferenceException;
import fsu.jportal.mets.ConvertException;
import fsu.jportal.mets.ENMAPConverter;
import fsu.jportal.mets.JVBMetsConverter;
import fsu.jportal.mets.JVBMetsImporter;
import fsu.jportal.mets.LLZMetsConverter;
import fsu.jportal.mets.LLZMetsImporter;
import fsu.jportal.mets.MetsImportException;
import fsu.jportal.mets.MetsImportUtils;
import fsu.jportal.mets.MetsImporter;
import fsu.jportal.util.MetsUtil;

@Path("mets/import")
public class METSImportResource {

    private static Logger LOGGER = LogManager.getLogger(METSImportResource.class);

    private static enum METS_TYPE {
        unknown, llz, jvb
    }

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
        METS_TYPE type = getType(doc);
        if (!type.equals(METS_TYPE.unknown)) {
            try {
                Mets mets = convert(derivateId, doc);
                validate(derivateId, mets);
            } catch (BlockReferenceException bre) {
                LOGGER.error("Unable to convert mets.xml", bre);
                returnObject.add("error", buildBlockReferenceError(bre, doc));
            } catch (Exception exc) {
                LOGGER.error("Unable to convert mets.xml", exc);
                returnObject.addProperty("error", exc.getMessage());
            }
        }
        returnObject.addProperty("type", type.name());
        return returnObject.toString();
    }

    /**
     * Creates the json error object for a block reference exception (It was not
     * possible to resolve the coordinates for one or more logical div's). 
     * 
     * @param bre the exception
     * @param doc the mets.xml
     * @return the error object as json
     */
    private JsonObject buildBlockReferenceError(BlockReferenceException bre, Document doc) {
        Element rootElement = doc.getRootElement();
        Map<String, Object> variables = new HashMap<>();
        variables.put("id", null);
        XPathExpression<Element> logicalDivExp = XPathFactory.instance().compile(
            "mets:structMap[@TYPE='logical_structmap']//mets:div[@ID=$id]", Filters.element(), variables,
            MetsUtil.METS_NS_LIST);
        XPathExpression<Element> metsFileExp = XPathFactory.instance().compile("mets:fileSec//mets:file[@ID=$id]",
            Filters.element(), variables, MetsUtil.METS_NS_LIST);

        JsonObject error = new JsonObject();
        error.addProperty("message", bre.getMessage());
        JsonArray refArray = new JsonArray();
        error.add("appearance", refArray);
        for (String id : bre.getDivIds()) {
            logicalDivExp.setVariable("id", id);
            JsonObject refError = new JsonObject();
            // get label and order
            Element logicalDiv = logicalDivExp.evaluateFirst(rootElement);
            refError.addProperty("label", logicalDiv.getParentElement().getAttributeValue("LABEL"));
            refError.addProperty("paragraph", Integer.valueOf(logicalDiv.getAttributeValue("ORDER")));
            // get image number
            String fileID = logicalDiv.getChild("fptr", MCRConstants.METS_NAMESPACE)
                                      .getChild("area", MCRConstants.METS_NAMESPACE)
                                      .getAttributeValue("FILEID");
            metsFileExp.setVariable("id", fileID);
            Element metsFile = metsFileExp.evaluateFirst(rootElement);
            String imageNumber = Optional.ofNullable(metsFile.getAttributeValue("SEQ"))
                                         .orElse(metsFile.getAttributeValue("ORDER"));
            if (imageNumber != null) {
                refError.addProperty("image", Integer.valueOf(imageNumber));
            }
            refArray.add(refError);
        }
        return error;
    }

    @POST
    @Path("import/{id}")
    public void convertAndImport(@PathParam("id") String derivateId)
        throws IOException, JDOMException, ConvertException, MetsImportException {
        // check permissions
        MetsImportUtils.checkPermission(derivateId);

        // build document stuff
        Document enmapMetsXML = MetsUtil.getMetsXMLasDocument(derivateId);
        Mets mcrMets = convert(derivateId, enmapMetsXML);
        METS_TYPE type = getType(enmapMetsXML);

        // import mets
        MetsImporter importer = null;
        if (type.equals(METS_TYPE.jvb)) {
            importer = new JVBMetsImporter();
        } else if (type.equals(METS_TYPE.llz)) {
            importer = new LLZMetsImporter();
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
        Mets mets = convert(derivateId, metsXML);
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        StringWriter writer = new StringWriter();
        out.output(mets.asDocument(), writer);
        return writer.toString();
    }

    /**
     * Converts the given enmap mets to an mcr one.
     * 
     * @param derivateId
     * @param enmapMetsXML
     * @return
     * @throws IOException
     * @throws JDOMException
     * @throws ConvertException
     */
    private Mets convert(String derivateId, Document enmapMetsXML) throws IOException, JDOMException, ConvertException {
        ENMAPConverter converter = getConverter(enmapMetsXML, derivateId);
        return converter.convert(enmapMetsXML, MCRPath.getPath(derivateId, "/"));
    }

    /**
     * Returns the appropriate converter for the given derivate.
     * 
     * @param type of the mets.xml jvb|llz
     * @param derivateId the derivate where the mets.xml
     * @return instance of java mets
     * 
     * @throws IOException
     * @throws JDOMException
     * @throws ConvertException
     */
    private ENMAPConverter getConverter(Document metsXML, String derivateId)
        throws IOException, JDOMException, ConvertException {
        // load mets
        METS_TYPE type = getType(metsXML);
        // convert
        ENMAPConverter converter = null;
        if (type.equals(METS_TYPE.llz)) {
            converter = new LLZMetsConverter();
        } else if (type.equals(METS_TYPE.jvb)) {
            converter = new JVBMetsConverter();
        } else {
            throw new ConvertException("Unknown type. It should be either 'llz' or 'jvb'.");
        }
        return converter;
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

    /**
     * Returns the type of the document.
     * 
     * @param doc mets.xml document
     * @return 'llz' | 'jvb' | 'unknown' 
     */
    private METS_TYPE getType(Document doc) {
        METS_TYPE type = METS_TYPE.unknown;
        if (MetsUtil.isENMAP(doc)) {
            // only difference between llz and jvb is the dmd title
            Element mets = doc.getRootElement();
            XPathExpression<Text> titleExp = XPathFactory.instance().compile(
                "mets:dmdSec/mets:mdWrap/mets:xmlData/mods:mods/mods:titleInfo/mods:title/text()", Filters.text(), null,
                MetsUtil.METS_NS_LIST);
            Text title = titleExp.evaluateFirst(mets);
            if (title != null && title.getText().equals("Jenaer Volksblatt")) {
                type = METS_TYPE.jvb;
            } else {
                type = METS_TYPE.llz;
            }
        }
        return type;
    }

}
