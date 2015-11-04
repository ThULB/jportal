package fsu.jportal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Text;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectUtils;

/**
 * Utility class for all URIResolver.
 * 
 * @author Matthias Eichner
 */
public abstract class ResolverUtil {

    private static final Logger LOGGER = LogManager.getLogger(ResolverUtil.class);

    /**
     * Returns the label of the given classification in the current language.
     * 
     * @param classID classification identifier
     * @return label of the classification
     */
    public static Optional<String> getClassLabel(String classID) {
        Optional<MCRCategory> rootCategory = Optional
            .ofNullable(MCRCategoryDAOFactory.getInstance().getRootCategory(MCRCategoryID.rootID(classID), 0));
        if (!rootCategory.isPresent()) {
            LOGGER.warn("Could not find ROOT Category <" + classID + ">");
        }
        return rootCategory.flatMap(MCRCategory::getCurrentLabel).map(MCRLabel::getText);
    }

    public static <T> T getParents(String childID, ParentsList<T> parentsList) {
        MCRObjectID mcrChildID = MCRObjectID.getInstance(childID);
        if (MCRXMLMetadataManager.instance().exists(mcrChildID)) {
            XPathExpression<Text> titleXpath = XPathFactory.instance()
                .compile("/mycoreobject/metadata/maintitles/maintitle[@inherited='0']/text()", Filters.text());
            List<MCRObject> parents;
            if (mcrChildID.getTypeId().equals("derivate")) {
                parents = new ArrayList<>();
                String prentID = MCRMetadataManager.retrieveMCRDerivate(mcrChildID).getDerivate().getMetaLink()
                    .getXLinkHref();
                parents.add(MCRMetadataManager.retrieveMCRObject(prentID));
                List<MCRObject> secList = MCRObjectUtils.getAncestors(MCRMetadataManager.retrieveMCRObject(prentID));
                parents.addAll(secList);
            } else {
                parents = MCRObjectUtils.getAncestors(MCRMetadataManager.retrieveMCRObject(mcrChildID));
            }
            String referer = childID;
            for (int i = 0; i < parents.size(); i++) {
                MCRObject parent = parents.get(i);
                Document parentXML = parent.createXML();
                String title = titleXpath.evaluateFirst(parentXML).getText();
                String id = parent.getId().toString();
                String inherited = String.valueOf(i + 1);
                parentsList.addParent(title, id, inherited, referer);
                referer = parent.getId().toString();
            }
        }

        return parentsList.getParents();
    }

}
