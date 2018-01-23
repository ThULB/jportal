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
 * @author Huu Chi Vu
 */
public abstract class ResolverUtil {

    private static final Logger LOGGER = LogManager.getLogger(ResolverUtil.class);

    /**
     * Returns the label of the given classification in the current language.
     * 
     * @param categoryId must be in format classificationId:categoryId or is rootID
     * @return label of the classification
     */
    public static Optional<String> getClassLabel(String categoryId) {
        return Optional.ofNullable(categoryId)
                .map(MCRCategoryID::fromString)
                .map(id -> MCRCategoryDAOFactory.getInstance().getCategory(id, 0))
                .flatMap(MCRCategory::getCurrentLabel)
                .map(MCRLabel::getText);
    }

}
