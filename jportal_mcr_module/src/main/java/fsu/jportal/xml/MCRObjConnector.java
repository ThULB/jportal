package fsu.jportal.xml;

import java.util.ArrayList;
import java.util.List;

import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaInterface;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

import fsu.jportal.backend.JPComponent;

/**
 * TODO: move some of this code to {@link JPComponent} API.
 * 
 * @author Chi
 */
public class MCRObjConnector {

    private MCRObject mcrObject;

    public MCRObjConnector(String journalID) {
        mcrObject = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(journalID));
    }

    public String getRubric() {
        return getRubric("hidden_rubricsID", "hidden_rubricID");
    }

    public List<String> getRubrics() {
        List<String> rubricIDs = new ArrayList<>();

        for (int i = 1; i <= 6; i++) {
            String tag = "hidden_classiVol" + i;
            addRubric(rubricIDs, getRubric(tag, tag));
        }

        addRubric(rubricIDs, getRubric("hidden_pubTypesID", "hidden_pubTypeID"));

        for (int i = 1; i <= 4; i++) {
            String mainTag = "hidden_classispub";
            String subTag = "hidden_classipub";

            if (i != 1) {
                mainTag = mainTag + i;
                subTag = subTag + i;
            }

            addRubric(rubricIDs, getRubric(mainTag, subTag));
        }

        addRubric(rubricIDs, getRubric("hidden_rubricsID", "hidden_rubricID"));
        return rubricIDs;
    }

    private void addRubric(List<String> rubricIDs, String rubric) {
        if (rubric != null) {
            rubricIDs.add(rubric);
        }
    }

    private String getRubric(String mainTag, String subTag) {
        MCRMetaElement metadataElement = mcrObject.getMetadata().getMetadataElement(mainTag);
        if (metadataElement != null) {
            MCRMetaInterface element = metadataElement.getElementByName(subTag);
            if (element instanceof MCRMetaLangText) {
                MCRMetaLangText metaLangText = (MCRMetaLangText) element;
                return metaLangText.getText();
            }
        }
        return null;
    }

    public void addRubric(MCRCategoryID newRubricID)
        throws MCRPersistenceException, MCRActiveLinkException, MCRAccessException {
        List<MCRMetaInterface> children = new ArrayList<MCRMetaInterface>();
        MCRMetaLangText elem = new MCRMetaLangText("hidden_rubricID", null, null, 0, null, newRubricID.getRootID());
        children.add(elem);
        MCRMetaElement mcrMetaElement = new MCRMetaElement(MCRMetaLangText.class, "hidden_rubricsID", false, false,
            children);
        mcrObject.getMetadata().setMetadataElement(mcrMetaElement);
        MCRMetadataManager.update(mcrObject);
    }

}
