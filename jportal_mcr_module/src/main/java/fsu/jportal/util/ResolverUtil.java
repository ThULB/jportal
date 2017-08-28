package fsu.jportal.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;

import java.util.Optional;

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

}
