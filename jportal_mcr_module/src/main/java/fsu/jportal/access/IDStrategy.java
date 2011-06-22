package fsu.jportal.access;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.xpath.XPath;
import org.mycore.access.strategies.MCRAccessCheckStrategy;
import org.mycore.common.MCRException;
import org.mycore.datamodel.metadata.MCRObjectID;

/**
 * Use object type strategy for permission "read"
 * Additionally for objects with parents check parents permission with object type strategy
 * @author chi
 *
 */
public class IDStrategy extends StrategieChain {

    private AccessStrategyConfig config;

    public IDStrategy(AccessStrategyConfig config) {
        this.config = config;
    }

    @Override
    protected boolean isReponsibleFor(String id, String permission) {
        return AccessTools.isValidID(id);
    }

    @Override
    protected boolean permissionStrategyFor(String id, String permission) {
        MCRAccessCheckStrategy objTypeStrategy = config.getAccessCheckStrategy(AccessStrategyConfig.OBJ_TYPE_STRATEGY);
        boolean objTypeStrategyPerm = objTypeStrategy.checkPermission(id, permission);
        
        if(objectWithParents(id) && !"read".equals(permission)){
            return objTypeStrategyPerm && objTypeStrategy.checkPermission(getParentID(id), permission);
        } else {
            return objTypeStrategyPerm;
        }
    }
    
    private boolean objectWithParents(String id){
        for (String idType : new String[] {"_jpvolume_","_jparticle_"}) {
            if (id.contains(idType)) {
                return true;
            }
        }
        
        return false;
    }

    private String getParentID(String id) {
        Document objXML = config.getXMLMetadataMgr().retrieveXML(MCRObjectID.getInstance(id));
        try {
            XPath pathToJournalID = XPath.newInstance("/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID/text()");
            Text idTextNode = (Text) pathToJournalID.selectSingleNode(objXML);
            
            if(idTextNode == null){
                return "";
            }
            
            return idTextNode.getText();
        } catch (JDOMException e) {
            e.printStackTrace();
        }
        
        return "";
    }
}