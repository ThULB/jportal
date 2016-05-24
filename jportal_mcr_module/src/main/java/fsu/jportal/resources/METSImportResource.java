package fsu.jportal.resources;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
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
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Text;
import org.jdom2.filter.Filters;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
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
import fsu.jportal.mets.ENMAPConverter;
import fsu.jportal.mets.JVBMetsConverter;
import fsu.jportal.mets.JVBMetsImporter;
import fsu.jportal.mets.LLZMetsConverter;
import fsu.jportal.mets.LLZMetsImporter;
import fsu.jportal.mets.MetsImportException;
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
        checkPermission(derivateId);
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
            } catch (Exception exc) {
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
        checkPermission(derivateId);

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
        Map<LogicalDiv, JPComponent> logicalComponentMap = importer.importMets(mcrMets,
            MCRObjectID.getInstance(derivateId));

        // update logical id's
        updateLogicalIds(mcrMets, logicalComponentMap);
        // write mets.xml
        Document mcrDoc = mcrMets.asDocument();
        byte[] bytes = new MCRJDOMContent(mcrDoc).asByteArray();
        MCRPath path = MCRPath.getPath(derivateId, "/mets.xml");
        Files.write(path, bytes);
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
        checkPermission(derivateId);
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
            for (SmLink link : links) {
                link.setFrom(mycoreId);
            }
        }
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
