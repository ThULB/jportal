package fsu.jportal.resources;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRPathContent;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.files.FileSec;
import org.mycore.mets.validator.ValidatorUtil;
import org.mycore.mets.validator.validators.SchemaValidator;
import org.mycore.mets.validator.validators.ValidationException;

import com.google.gson.JsonObject;

import fsu.jportal.mets.ConvertException;
import fsu.jportal.mets.UIBKtoMCRConverter;

@Path("mets/uibk/import")
public class METSImportResource {

    //    private static Logger LOGGER = Logger.getLogger(METSImportResource.class);

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
        JsonObject returnObject = new JsonObject();
        try {
            Document doc = getMetsXMLasDocument(derivateId);
            fastCheck(doc);
        } catch (Exception ve) {
            returnObject.addProperty("error", ve.getMessage());
        }
        returnObject.addProperty("check", !returnObject.has("error"));
        return returnObject.toString();
    }

    @GET
    @Path("deepCheck/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String deepCheck(@PathParam("id") String derivateId) {
        JsonObject returnObject = new JsonObject();
        try {
            Document doc = getMetsXMLasDocument(derivateId);
            deepCheck(doc);
        } catch (Exception ve) {
            returnObject.addProperty("error", ve.getMessage());
        }
        returnObject.addProperty("check", !returnObject.has("error"));
        return returnObject.toString();
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
        // load mets
        Document uibkMets = getMetsXMLasDocument(derivateId);
        // convert
        UIBKtoMCRConverter converter = new UIBKtoMCRConverter();
        Document mcrMets = converter.convert(uibkMets);
        // output
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        StringWriter writer = new StringWriter();
        out.output(mcrMets, writer);
        return writer.toString();
    }

    /**
     * Just checks the if the root element name is equals 'mets' and the
     * agents name is 'UIBK'. Throws an validation exception when the
     * check went wrong.
     * 
     * @param metsDocument the document to check
     * @throws ValidationException when something is invalid
     */
    private void fastCheck(Document metsDocument) throws ValidationException {
        // check root element
        Element rootElement = metsDocument.getRootElement();
        if (!rootElement.getName().toLowerCase().equals("mets")) {
            ValidatorUtil.throwException(rootElement, "Invalid root element name. It should be 'mets'.");
        }
        // check agent
        String agent = ValidatorUtil
            .checkXPath(rootElement, "mets:metsHdr/mets:agent/mets:name/text()", Filters.text()).getText();
        if (!"UIBK".equals(agent.toUpperCase())) {
            ValidatorUtil.throwException(rootElement, "Invalid agent. Its '" + agent + "' but should be 'UIBK'.");
        }
    }

    /**
     * Validates against the mets.xsd and checks if the agent is 'UIBK'.
     * 
     * @param metsDocument the document to check
     * @throws ValidationException when something is invalid
     */
    private void deepCheck(Document metsDocument) throws ValidationException {
        // root element and agent check
        fastCheck(metsDocument);
        // schema check
        SchemaValidator schemaValidator = new SchemaValidator();
        schemaValidator.validate(metsDocument);
    }

    /**
     * Returns the mets.xml as an input stream.
     * 
     * @throws FileNotFoundException when there is no mets.xml
     * @throws IOException when the mets.xml couldn't be read by the io
     * @param derivateId the derivate where to get the mets.xml
     * @return the mets.xml as {@link InputStream}
     */
    private InputStream getMetsXMLasStream(String derivateId) throws FileNotFoundException, IOException {
        MCRPath metsXML = MCRPath.getPath(derivateId, "/mets.xml");
        if (!Files.exists(metsXML)) {
            throw new FileNotFoundException("No mets.xml in this derivate " + derivateId);
        }
        try {
            MCRContent content = new MCRPathContent(metsXML);
            return content.getInputStream();
        } catch (Exception exc) {
            throw new IOException("Error while reading " + metsXML.toAbsolutePath().toString(), exc);
        }
    }

    /**
     * Returns the mets.xml as a jdom document object.
     *
     * @param derivateId the derivate where to get the mets.xml
     * @throws FileNotFoundException when there is no mets.xml
     * @throws IOException when the mets.xml couldn't be read by the io
     * @throws JDOMException when the file exists but couldn't be parsed with jdom
     * @return the mets.xml as jdom document
     */
    private Document getMetsXMLasDocument(String derivateId) throws FileNotFoundException, IOException, JDOMException {
        InputStream metsXMLStream = getMetsXMLasStream(derivateId);
        SAXBuilder builder = new SAXBuilder();
        try {
            return builder.build(metsXMLStream);
        } catch (JDOMException exc) {
            throw new JDOMException("Error while parsing mets.xml of " + derivateId, exc);
        }
    }

}
