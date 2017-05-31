package fsu.jportal.mets;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Text;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRConstants;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.frontend.jersey.MCRJerseyUtil;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.SmLink;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import fsu.jportal.backend.JPComponent;
import fsu.jportal.backend.JPContainer;
import fsu.jportal.backend.JPVolume;
import fsu.jportal.util.MetsUtil;

/**
 * Some utility methods for the mets import.
 * 
 * @author Matthias Eichner
 */
public class MetsImportUtils {

    public static BiMap<Integer, String> MONTH_NAMES;

    static {
        MONTH_NAMES = ImmutableBiMap.<Integer, String> builder()
                                    .put(1, "Januar")
                                    .put(2, "Februar")
                                    .put(3, "März")
                                    .put(4, "April")
                                    .put(5, "Mai")
                                    .put(6, "Juni")
                                    .put(7, "Juli")
                                    .put(8, "August")
                                    .put(9, "September")
                                    .put(10, "Oktober")
                                    .put(11, "November")
                                    .put(12, "Dezember")
                                    .build();
    }

    static enum MONTH {
        Januar, Februar, März, April, Mai, Juni, Juli, August, September, Oktober, November, Dezember;

        MONTH() {

        }
    }

    public static enum METS_TYPE {
        unknown, llz, jvb, perthes
    }

    /**
     * Does a mets import for the derivate containing the mets.xml with the
     * given importer.
     * 
     * @param derivateId the derivate to import
     * @param mets the java mets representation which should be a part of the derivate
     * @param importer the mets importer
     * @throws MetsImportException the import went wrong
     * @throws IOException the mets.xml update went wrong
     */
    public static void importMets(String derivateId, Mets mets, MetsImporter importer)
        throws MetsImportException, IOException {
        Map<LogicalDiv, JPComponent> logicalComponentMap = importer.importMets(mets,
            MCRObjectID.getInstance(derivateId));

        // update logical id's
        updateLogicalIds(mets, logicalComponentMap);
        // write mets.xml
        Document mcrDoc = mets.asDocument();
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
    private static void updateLogicalIds(Mets mets, Map<LogicalDiv, JPComponent> logicalComponentMap) {
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

    public static void setPublishedDate(int monthIndex, JPVolume volume, JPContainer parent) {
        parent.getPublishedDate().ifPresent(date -> {
            volume.setDate(date.getYear() + "-" + String.format("%02d", monthIndex), null);
        });
    }

    /**
     * Checks if the user has the permissions to perform the task.
     * 
     * @param derivateId derivate to check
     */
    public static void checkPermission(String derivateId) {
        MCRJerseyUtil.checkPermission(derivateId, "readdb");
        MCRObjectID mcrDerivateId = MCRObjectID.getInstance(derivateId);
        MCRDerivate derivate = MCRMetadataManager.retrieveMCRDerivate(mcrDerivateId);
        MCRObjectID ownerID = derivate.getOwnerID();
        MCRJerseyUtil.checkPermission(ownerID, "writedb");
    }

    /**
     * Creates the json error object for a block reference exception (It was not
     * possible to resolve the coordinates for one or more logical div's). 
     * 
     * @param bre the exception
     * @param doc the mets.xml
     * @return the error object as json
     */
    public static JsonObject buildBlockReferenceError(String message, List<String> ids, Document doc) {
        Element rootElement = doc.getRootElement();
        Map<String, Object> variables = new HashMap<>();
        variables.put("id", null);
        XPathExpression<Element> logicalDivExp = XPathFactory.instance().compile(
            "mets:structMap[@TYPE='logical_structmap']//mets:div[@ID=$id]", Filters.element(), variables,
            MetsUtil.METS_NS_LIST);
        XPathExpression<Element> metsFileExp = XPathFactory.instance().compile("mets:fileSec//mets:file[@ID=$id]",
            Filters.element(), variables, MetsUtil.METS_NS_LIST);

        JsonObject error = new JsonObject();
        error.addProperty("message", message);
        JsonArray refArray = new JsonArray();
        error.add("appearance", refArray);
        for (String id : ids) {
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

    /**
     * Returns the type of the document.
     * 
     * @param doc mets.xml document
     * @return enum of 'llz' | 'jvb' | 'perthes' | 'unknown' 
     */
    public static METS_TYPE determineType(Document doc) {
        METS_TYPE type = METS_TYPE.unknown;
        if (MetsUtil.isENMAP(doc)) {
            Element mets = doc.getRootElement();
            XPathExpression<Text> titleExp = XPathFactory.instance().compile(
                "mets:dmdSec/mets:mdWrap/mets:xmlData/mods:mods/mods:titleInfo/mods:title/text()", Filters.text(), null,
                MetsUtil.METS_NS_LIST);
            Text title = titleExp.evaluateFirst(mets);
            if (title != null && title.getText().equals("Jenaer Volksblatt")) {
                type = METS_TYPE.jvb;
            } else if (title != null && title.getText().endsWith("_Perthes")) {
                type = METS_TYPE.perthes;
            } else {
                type = METS_TYPE.llz;
            }
        }
        return type;
    }

    /**
     * Returns the appropriate converter for the given derivate.
     * 
     * @param type of the mets.xml jvb|llz
     * @return instance of java mets
     * 
     * @throws ConvertException converter type couldn't be determined
     */
    public static ENMAPConverter getConverter(Document metsXML) throws ConvertException {
        // load mets
        METS_TYPE type = MetsImportUtils.determineType(metsXML);
        // convert
        ENMAPConverter converter = null;
        if (type.equals(METS_TYPE.llz)) {
            converter = new LLZMetsConverter();
        } else if (type.equals(METS_TYPE.jvb)) {
            converter = new JVBMetsConverter();
        } else if (type.equals(METS_TYPE.perthes)) {
            converter = new PerthesMetsConverter();
        } else {
            throw new ConvertException("Unknown type. It should be either 'llz', 'jvb' or 'perthes'.");
        }
        return converter;
    }

}
