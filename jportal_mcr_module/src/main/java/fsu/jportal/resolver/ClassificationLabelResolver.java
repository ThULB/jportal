package fsu.jportal.resolver;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;

import fsu.jportal.annotation.URIResolverSchema;

@URIResolverSchema(schema = "xClassificationLabel")
public class ClassificationLabelResolver implements URIResolver {

    private static final Logger LOGGER = LogManager.getLogger(ClassificationLabelResolver.class);

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        String[] uriParts = href.split(":");
        if (uriParts.length < 3) {
            throw new IllegalArgumentException("Invalid format of uri given to resolve: " + href);
        }
        String journalID = uriParts[1];
        String xpath = uriParts[2];
        String classID = ClassificationResolver.getClassificationID(journalID, xpath);
        String label = getClassLabel(classID);
        return new JDOMSource(new Element("dummyRoot").addContent(new Element("label").setText(label)));
    }

    private String getClassLabel(String classID) {
        MCRCategory rootCategory = MCRCategoryDAOFactory.getInstance()
            .getRootCategory(MCRCategoryID.rootID(classID), 0);
        if (rootCategory == null) {
            LOGGER.warn("Could not find ROOT Category <" + classID + ">");
        } else if (rootCategory.getLabels() != null && rootCategory.getLabels().size() > 0) {
            return rootCategory.getCurrentLabel().getText();
        }
        return "";
    }

}
