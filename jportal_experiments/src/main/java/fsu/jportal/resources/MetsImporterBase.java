package fsu.jportal.resources;

import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRConstants;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.xml.MCRLayoutTransformerFactory;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaDerivateLink;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

import com.google.common.collect.BiMap;

import fsu.archiv.mycore.sru.impex.pica.model.PicaRecord;
import fsu.jportal.util.GndUtil;

public abstract class MetsImporterBase {

    private static List<Namespace> NS_LIST;

    /**
     * gets the title of a mods entry
     */
    final static XPathExpression<Text> TITLE_EXPRESSION;

    /**
     * get all valid mods entries
     */
    final static XPathExpression<Element> MODS_EXPRESSION;

    final static XPathExpression<Text> HEADING_EXPRESSION;

    private Element mets;

    private MCRDerivate derivate;

    private Map<String, Element> logicalStructMap;

    private Map<String, String> imageFiles;

    private Map<String, String> altoFiles;

    private BiMap<String, String> linkedImageAltoMap;

    static {
        NS_LIST = new ArrayList<>();
        NS_LIST.add(MCRConstants.METS_NAMESPACE);
        NS_LIST.add(MCRConstants.MODS_NAMESPACE);
        TITLE_EXPRESSION = XPathFactory.instance().compile("mods:titleInfo/mods:title/text()", Filters.text(), null,
            NS_LIST);
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("id", null);
        MODS_EXPRESSION = XPathFactory.instance().compile("mets:dmdSec[@ID=$id]/mets:mdWrap/mets:xmlData/mods:mods",
            Filters.element(), vars, NS_LIST);
        HEADING_EXPRESSION = XPathFactory.instance().compile("/add/doc/field[@name='heading_base']/text()",
            Filters.text());
    }

    public MetsImporterBase(Document metsDocument, MCRDerivate derivate) {
        this.mets = metsDocument.getRootElement();
        this.derivate = derivate;
        this.logicalStructMap = parseLogicalStructMap(mets);
        this.altoFiles = parseALTOFiles(mets);
        this.imageFiles = parseImageFiles(mets);
        this.linkedImageAltoMap = parseImageALTOMap(mets);
    }

    /**
     * Returns a list of required namespaces to parse the mets document.
     * 
     * @return
     */
    protected static List<Namespace> getNameSpaceList() {
        return NS_LIST;
    }

    public Element getMets() {
        return mets;
    }

    public MCRDerivate getDerivate() {
        return derivate;
    }

    /**
     * Returns all DMD_IDs which are referenced in the logical structmap. This assures
     * that only dmd sections will be used which are explicit set.
     * 
     * @param mets
     * @return
     */
    protected abstract Map<String, Element> parseLogicalStructMap(Element mets);

    public Map<String, Element> getLogicalStructMap() {
        return logicalStructMap;
    }

    /**
     * Returns a map containing all alto files. key = METS_ID, value = path to image.
     * 
     * @param mets
     * @return
     */
    protected abstract Map<String, String> parseALTOFiles(Element mets);

    public Map<String, String> getAltoFiles() {
        return altoFiles;
    }

    /**
     * Returns a map containing all images. key = METS_ID, value = path to image.
     * 
     * @param mets
     * @return
     */
    protected abstract Map<String, String> parseImageFiles(Element mets);

    public Map<String, String> getImageFiles() {
        return imageFiles;
    }

    /**
     * Returns a bi map containing all images and alto files which are linked.
     * 
     * @param mets
     * @return
     */
    protected abstract BiMap<String, String> parseImageALTOMap(Element mets);

    /**
     * Returns the first alto file id of a logical div section.
     * <pre>
     * {@code
     *  <mets:div DMDID="DMD_LS2" ID="LS2" ORDER="1" TYPE="section">
     *   <mets:div ID="LS3" ORDER="1" TYPE="articleHeading">
     *    <mets:fptr>
     *      <mets:area FILEID="_jportal_derivate_00216170-00000009-INDEXALTO" />
     * }
     * </pre>
     * 
     * @param div
     * @return first FILEID or null
     */
    protected abstract String getFirstALTOIdOfLogicalDiv(Element div);

    public BiMap<String, String> getLinkedImageAltoMap() {
        return linkedImageAltoMap;
    }

    public void importMets() {
        Set<String> dmdIds = getLogicalStructMap().keySet();

        for (String dmdId : dmdIds) {
            MODS_EXPRESSION.setVariable("id", dmdId);
            Element mods = MODS_EXPRESSION.evaluateFirst(getMets());
            if (mods == null) {
                continue;
            }
            MCRObject article = buildArticle(mods, dmdId);
            MCRMetadataManager.create(article);
        }
    }

    /**
     * Returns a map of files. The expression should match //mets:file. The mets:file should
     * look like:
     * <file ID="ALTO00001">
     *   <FLocat LOCTYPE="URL" xlink:href="file://./alto/032_JV_1933-02-07_001_ALTO.xml" />
     * </file>
     * 
     * Key of the map will be the ID, the value the xlink:href
     * 
     * @param expression
     * @param context
     * @return
     */
    public static Map<String, String> parseFiles(XPathExpression<Element> expression, Element context) {
        List<Element> fileElements = expression.evaluate(context);
        Map<String, String> returnMap = new HashMap<>();
        for (Element fileElement : fileElements) {
            String key = fileElement.getAttributeValue("ID");
            String value = fileElement.getChild("FLocat", MCRConstants.METS_NAMESPACE).getAttributeValue("href",
                MCRConstants.XLINK_NAMESPACE);
            returnMap.put(key, value);
        }
        return returnMap;
    }

    protected MCRObject buildArticle(Element mods, String dmdId) {
        MCRObject o = new MCRObject();
        o.setId(MCRObjectID.getNextFreeId("jportal_jparticle"));
        o.setSchema("datamodel-jparticle.xsd");

        // parent
        o.getStructure().setParent(getDerivate().getOwnerID());

        // title
        Text title = TITLE_EXPRESSION.evaluateFirst(mods);
        if (title != null && title.getTextNormalize().length() > 0) {
            MCRMetaElement maintitles = new MCRMetaElement(MCRMetaLangText.class, "maintitles", true, false, null);
            maintitles.addMetaObject(new MCRMetaLangText("maintitle", null, null, 0, null, title.getTextNormalize()));
            o.getMetadata().setMetadataElement(maintitles);
        }

        // participants - we just create participants which have a gnd id
        for (Element name : mods.getChildren("name", MCRConstants.MODS_NAMESPACE)) {
            String authorityURI = name.getAttributeValue("authorityURI");
            if (!"http://d-nb.info/gnd/".equals(authorityURI)) {
                continue;
            }
            String valueURI = name.getAttributeValue("valueURI");
            if (valueURI == null) {
                continue;
            }
            String gndId = valueURI.substring(valueURI.lastIndexOf("/") + 1);
            try {
                // check if participant already exists in mycore
                String mcrId = GndUtil.getMCRId(gndId);
                MCRObjectID participantId;
                MCRContent participantContent;
                if (mcrId == null) {
                    // create from sru
                    PicaRecord picaRecord = GndUtil.retrieveFromSRU(gndId);
                    Document mcrXML = GndUtil.toMCRObjectDocument(picaRecord);
                    MCRObject participantObject = new MCRObject(mcrXML);
                    MCRMetadataManager.create(participantObject);
                    participantId = participantObject.getId();
                    participantContent = new MCRJDOMContent(mcrXML);
                } else {
                    participantId = MCRObjectID.getInstance(mcrId);
                    participantContent = MCRXMLMetadataManager.instance().retrieveContent(participantId);
                }
                // get title
                MCRContentTransformer transformer = MCRLayoutTransformerFactory.getTransformer("mycoreobject-solr");
                MCRContent solrContent = transformer.transform(participantContent);
                Text participantTitle = HEADING_EXPRESSION.evaluateFirst(solrContent.asXML());
                // create link
                MCRMetaElement participants = o.getMetadata().getMetadataElement("participants");
                if (participants == null) {
                    participants = new MCRMetaElement(MCRMetaLinkID.class, "participants", false, false, null);
                    o.getMetadata().setMetadataElement(participants);
                }
                MCRMetaLinkID link = new MCRMetaLinkID("participant", participantId, null, participantTitle.getText());
                // we assume its the author
                link.setType("author");
                participants.addMetaObject(link);
            } catch (Exception exc) {
                // TODO: exc
                exc.printStackTrace();
                continue;
            }
        }

        // derivate link
        Element logicalDmdElement = getLogicalStructMap().get(dmdId);
        String altoId = getFirstALTOIdOfLogicalDiv(logicalDmdElement);
        if (altoId != null) {
            String imageId = getLinkedImageAltoMap().get(altoId);
            if (imageId == null) {
                imageId = getLinkedImageAltoMap().inverse().get(altoId);
                if (imageId != null) {
                    MCRMetaElement derivateLinks = new MCRMetaElement(MCRMetaDerivateLink.class, "derivateLinks",
                        false, false, null);
                    try {
                        String imageFilePath = Paths.get(new URI(getImageFiles().get(imageId))).normalize().toString();
                        MCRMetaDerivateLink link = new MCRMetaDerivateLink();
                        link.setInherited(0);
                        link.setSubTag("derivateLink");
                        link.setReference(getDerivate().getId() + imageFilePath, null, null);
                        derivateLinks.addMetaObject(link);
                        o.getMetadata().setMetadataElement(derivateLinks);
                    } catch (Exception exc) {
                        // TODO: warn here
                        exc.printStackTrace();
                    }
                } else {
                    // TODO: warn that image cannot be found
                }
            }
        }

        // position in volume (sizes)
        String order = "";
        MCRMetaElement sizes = new MCRMetaElement(MCRMetaLangText.class, "sizes", false, false, null);
        sizes.addMetaObject(new MCRMetaLangText("size", null, null, 0, "plain", order));
        o.getMetadata().setMetadataElement(sizes);

        return o;
    }

}
