package fsu.jportal.mets;

import fsu.archiv.mycore.sru.impex.pica.model.PicaRecord;
import fsu.jportal.util.GndUtil;
import org.apache.solr.client.solrj.SolrServerException;
import org.jdom2.*;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRConstants;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRPathContent;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.mets.model.IMetsElement;
import org.mycore.mets.validator.ValidatorUtil;
import org.mycore.mets.validator.validators.SchemaValidator;
import org.mycore.mets.validator.validators.ValidationException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class LLZMetsUtils {

    private static final ArrayList<Namespace> NS_LIST;

    private static final XPathExpression<Attribute> BIB_LABEL_EXP;

    private static final XPathExpression<Attribute> DMDID_EXP;

    private static final XPathExpression<Element> MODS_EXPRESSION;

    private static final XPathExpression<Text> TITLE_EXPRESSION;

    static {
        NS_LIST = new ArrayList<Namespace>();
        NS_LIST.add(MCRConstants.METS_NAMESPACE);
        NS_LIST.add(MCRConstants.MODS_NAMESPACE);
        NS_LIST.add(MCRConstants.XLINK_NAMESPACE);
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("id", null);
        BIB_LABEL_EXP = XPathFactory.instance().compile("mets:div[@TYPE='Bibliographischer Eintrag']/@LABEL",
            Filters.attribute(), null, IMetsElement.METS);
        DMDID_EXP = XPathFactory.instance().compile("mets:div[@TYPE='Bibliographischer Eintrag']/@DMDID",
            Filters.attribute(), null, IMetsElement.METS);
        MODS_EXPRESSION = XPathFactory.instance().compile("mets:dmdSec[@ID=$id]/mets:mdWrap/mets:xmlData/mods:mods",
                Filters.element(), vars, NS_LIST);
        TITLE_EXPRESSION = XPathFactory.instance().compile("mods:recordInfo/mods:recordOrigin/text()", Filters.text(),
                null, NS_LIST);
    }

    /**
     * Just checks the if the root element name is equals 'mets' and the
     * agents name is 'UIBK'. Throws an validation exception when the
     * check went wrong.
     * 
     * @param metsDocument the document to check
     * @throws ValidationException when something is invalid
     */
    public static void fastCheck(Document metsDocument) throws ValidationException {
        // check root element
        Element rootElement = metsDocument.getRootElement();
        if (!rootElement.getName().toLowerCase().equals("mets")) {
            ValidatorUtil.throwException(rootElement, "Invalid root element name. It should be 'mets'.");
        }
        // check profile
        String profile = rootElement.getAttributeValue("PROFILE");
        if (!profile.equals("ENMAP")) {
            ValidatorUtil.throwException(rootElement, "Invalid PROFILE. Its '" + profile
                + "' but should be 'ENMAP'.");
        }
//        if (!profile.equals("DEA_METSALTO_WITH_IDREFS")) {
//            ValidatorUtil.throwException(rootElement, "Invalid PROFILE. Its '" + profile
//                + "' but should be 'DEA_METSALTO_WITH_IDREFS'.");
//        }
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
    public static void deepCheck(Document metsDocument) throws ValidationException {
        // root element, profile and agent check
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
    public static InputStream getMetsXMLasStream(String derivateId) throws IOException {
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
    public static Document getMetsXMLasDocument(String derivateId) throws IOException,
        JDOMException {
        InputStream metsXMLStream = getMetsXMLasStream(derivateId);
        SAXBuilder builder = new SAXBuilder();
        try {
            return builder.build(metsXMLStream);
        } catch (JDOMException exc) {
            throw new JDOMException("Error while parsing mets.xml of " + derivateId, exc);
        }
    }

    /**
     * Returns the label attribute of an bibliographical entry.
     * 
     * @param parentDiv
     * @return
     */
    public static String getBibLabel(Element parentDiv) {
        Attribute labelAttribute = BIB_LABEL_EXP.evaluateFirst(parentDiv);
        if (labelAttribute != null) {
            return labelAttribute.getValue();
        }
        return null;
    }

    /**
     * Returns the full label of an bibliographical entry.
     *
     * @param dmdID
     * @return
     */
    public static String getFullLabel(String dmdID, Element metsDocument) {
        MODS_EXPRESSION.setVariable("id", dmdID);
        Element mods = MODS_EXPRESSION.evaluateFirst(metsDocument);
        Text title = TITLE_EXPRESSION.evaluateFirst(mods);
        if (title != null) {
            return title.getText();
        }
        return null;
    }

    /**
     * Returns the dmd id of an bibliographical entry.
     * 
     * @param parentDiv
     * @return
     */
    public static String getDmDId(Element parentDiv) {
        Attribute dmdId = DMDID_EXP.evaluateFirst(parentDiv);
        if (dmdId != null) {
            return dmdId.getValue();
        }
        return null;
    }

    /**
     * Gets or creates the person by the given mods:name element. This is done by checking
     * if the gnd is already in the system. If not, the person is created via the sru
     * interface.
     * 
     * @param name mods:name element
     * @return the id of the existing or new imported person
     * @throws SolrServerException is thrown when the person check failed
     * @throws ConnectException when no connection to the sru interface could be established (in this case you
     * can assume that the person does not exist in the system)
     */
    public static MCRObjectID getOrCreatePerson(Element name)
            throws SolrServerException, IOException, MCRAccessException {
        String authorityURI = name.getAttributeValue("authorityURI");
        if (!"http://d-nb.info/gnd/".equals(authorityURI)) {
            return null;
        }
        String valueURI = name.getAttributeValue("valueURI");
        if (valueURI == null) {
            return null;
        }
        String gndId = valueURI.substring(valueURI.lastIndexOf("/") + 1);
        // check if participant already exists in mycore
        String mcrId = GndUtil.getMCRId(gndId);
        if (mcrId == null) {
            // create from sru
            PicaRecord picaRecord = GndUtil.retrieveFromSRU(gndId);
            Document mcrXML = GndUtil.toMCRObjectDocument(picaRecord);
            MCRObject participantObject = new MCRObject(mcrXML);
            MCRMetadataManager.create(participantObject);
            return participantObject.getId();
        } else {
            return MCRObjectID.getInstance(mcrId);
        }
    }

    /**
     * Strategy to convert a llz @xlink:href to a mycore one.
     */
    static interface FileHrefStrategy {
        public String get(String href) throws URISyntaxException;
    }

    /**
     * Converts an absolute *.jpg file href to a relative *.tif.
     */
    static class TiffHrefStrategy implements FileHrefStrategy {
        private static Pattern JPG_PATTERN = Pattern.compile("\\.jpg", Pattern.CASE_INSENSITIVE);

        @Override
        public String get(String href) throws URISyntaxException {
            URI uri = new URI(href);
            return JPG_PATTERN.matcher(uri.getPath().replaceAll("^[^\\w]*", "")).replaceAll(".tif");
        }
    }

    /**
     * Alto files should be in an alto folder containing all *xml's.
     */
    static class AltoHrefStrategy implements FileHrefStrategy {
        @Override
        public String get(String href) throws URISyntaxException {
            URI uri = new URI(href);
            return uri.getPath().replaceAll("^[^\\w]*", "").replaceFirst("idx_alto", "alto");
        }
    }

}
