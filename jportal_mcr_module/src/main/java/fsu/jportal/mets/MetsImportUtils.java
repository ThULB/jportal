package fsu.jportal.mets;

import java.io.IOException;
import java.nio.file.Files;
import java.time.temporal.ChronoField;
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
import org.mycore.mets.model.struct.PhysicalDiv;
import org.mycore.mets.model.struct.PhysicalStructMap;
import org.mycore.mets.model.struct.SmLink;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import fsu.jportal.backend.JPComponent;
import fsu.jportal.backend.JPContainer;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.backend.JPVolume;
import fsu.jportal.util.MetsUtil;

/**
 * Some utility methods for the mets import.
 * 
 * @author Matthias Eichner
 */
public class MetsImportUtils {

    public enum MetsType {
        unknown, enmap, llz, jvb, perthes
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
        parent.getDate(JPPeriodicalComponent.DateType.published).ifPresent(jpDate -> {
            int year = jpDate.getDateOrFrom().get(ChronoField.YEAR);
            String yearAsString = String.format("%04d", year);
            String monthAsString = String.format("%02d", monthIndex);
            volume.setDate(yearAsString + "-" + monthAsString, JPPeriodicalComponent.DateType.published.name());
        });
    }

    /**
     * Returns the page number of the given logical div.
     * The page number starts at zero.
     *
     * @param mets the mets
     * @param div the logical div
     * @return the page number of the logical div
     */
    public static int getPageNumber(Mets mets, LogicalDiv div) {
        // get physical container
        PhysicalStructMap physicalStructMap = (PhysicalStructMap) mets.getStructMap(PhysicalStructMap.TYPE);
        PhysicalDiv divContainer = physicalStructMap.getDivContainer();
        // get logical id
        String logicalId = div.getId();
        List<SmLink> links = mets.getStructLink().getSmLinkByFrom(logicalId);
        // map phyisical div's and get the lowest by order
        return links.stream()
                .map(link -> divContainer.get(link.getTo()).getPositionInParent().orElse(0))
                .min(Integer::compareTo)
                .orElse(0);
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
     * @param message the error message
     * @param ids list of logical div identifiers
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
     * TODO: be aware that the llz type will not be detected!
     * 
     * @param doc mets.xml document
     * @return enum of 'enmap' | 'jvb' | 'perthes' | 'unknown'
     */
    public static MetsType determineType(Document doc) {
        MetsType type = MetsType.unknown;
        if (MetsUtil.isENMAP(doc)) {
            Element mets = doc.getRootElement();
            XPathExpression<Text> titleExp = XPathFactory.instance().compile(
                "mets:dmdSec/mets:mdWrap/mets:xmlData/mods:mods/mods:titleInfo/mods:title/text()", Filters.text(), null,
                MetsUtil.METS_NS_LIST);
            Text title = titleExp.evaluateFirst(mets);
            if (title != null && title.getText().equals("Jenaer Volksblatt")) {
                type = MetsType.jvb;
            } else if (title != null && title.getText().endsWith("_Perthes")) {
                type = MetsType.perthes;
            } else {
                type = MetsType.enmap;
            }
        }
        return type;
    }

    /**
     * Returns the appropriate converter for the given derivate.
     * 
     * @param metsXML xml mets document
     * @return instance of the converter
     * 
     * @throws ConvertException converter type couldn't be determined
     */
    public static ENMAPConverter getConverter(Document metsXML) throws ConvertException {
        // load mets
        MetsType type = MetsImportUtils.determineType(metsXML);
        // convert
        ENMAPConverter converter;
        if (type.equals(MetsType.llz)) {
            converter = new LLZMetsConverter();
        } else if (type.equals(MetsType.jvb)) {
            converter = new JVBMetsConverter();
        } else if (type.equals(MetsType.perthes)) {
            converter = new PerthesMetsConverter();
        }else if (type.equals(MetsType.enmap)) {
            converter = new ENMAPConverter();
        } else {
            throw new ConvertException("Unknown type. It should be either 'llz', 'jvb' or 'perthes'.");
        }
        return converter;
    }

}
