package fsu.jportal.xml;

import java.util.ArrayList;
import java.util.List;

import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaInterface;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

public class MCRObjConnector {
    private MCRObject mcrObject;
    private DataManager dataManager;

    public MCRObjConnector(String journalID) {
        this(journalID, new DataManager() {
            @Override
            public MCRObject getObj(String id) {
                return MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(id));
            }

            @Override
            public void update(MCRObject obj) {
                try {
                    MCRMetadataManager.update(obj);
                } catch (MCRPersistenceException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (MCRActiveLinkException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }
    
    public MCRObjConnector(String journalID, DataManager dataManager) {
        this.dataManager = dataManager;
        mcrObject = dataManager.getObj(journalID);
        
    }

    public String getRubric(String journalID) {
        MCRMetaElement metadataElement = mcrObject.getMetadata().getMetadataElement("hidden_rubricsID");
        if(metadataElement != null){
            MCRMetaInterface element = metadataElement.getElementByName("hidden_rubricID");
            if(element instanceof MCRMetaLangText){
                MCRMetaLangText metaLangText = (MCRMetaLangText) element;
                return metaLangText.getText();
            }
        }
        
        return null;
    }

    public void addRubric(MCRCategoryID newRubricID) {
        List<MCRMetaInterface> children = new ArrayList<MCRMetaInterface>();
        MCRMetaLangText elem = new MCRMetaLangText("hidden_rubricID", null, null, 0, null, newRubricID.getRootID());
        children.add(elem);
        MCRMetaElement mcrMetaElement = new MCRMetaElement(MCRMetaLangText.class, "hidden_rubricsID", false, false, children);
        mcrObject.getMetadata().setMetadataElement(mcrMetaElement);
        dataManager.update(mcrObject);
    }
}
