package fsu.jportal.access;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.xpath.XPath;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRException;
import org.mycore.datamodel.metadata.MCRObjectID;

public class ParentCheck extends AbstractStrategyStep {
    public ParentCheck(AccessStrategyConfig accessStrategyConfig) {
        super(accessStrategyConfig);
    }

    @Override
    public boolean checkPermission(String id, String permission) {
        MCRObjectID objId = getObjId(id);
        
        if(objId == null ){
             return getAlternative().checkPermission(id, permission);
        }
        
        MCRObjectID parentID = getParentID(objId);
        if(parentID == null || parentID.equals(objId)){
            return getAlternative().checkPermission(id, permission);
        }
        
        if (getAccessStrategyConfig().getAccessInterface().hasRule(parentID.toString(), permission)) {
            return getAccessStrategyConfig().getAccessInterface().checkPermission(parentID.toString(), permission);
        } else {
            return checkPermission(parentID.toString(), permission);
        }
    }

    private MCRObjectID getObjId(String id) {
        try {
            MCRObjectID objID = MCRObjectID.getInstance(id);
            return objID;
        } catch (MCRException e) {
            return null;
        }
    }
    
    private MCRObjectID getParentID(MCRObjectID objID) {
        //LOGGER.info("getParentId: " + objID + " exists " + getAccessConfig().getXMLMetadataMgr().exists(objID));
        if (!getAccessStrategyConfig().getXMLMetadataMgr().exists(objID)) {
            return null;
        }

        Document objXML = getAccessStrategyConfig().getXMLMetadataMgr().retrieveXML(objID);
        try {
            String path = "/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID/text()";
            if (objID.getTypeId().equals("derivate")) {
                path = "/mycorederivate/derivate/linkmetas/linkmeta/@xlink:href";
            }
            XPath pathToJournalID = XPath.newInstance(path);
            pathToJournalID.addNamespace(MCRConstants.XLINK_NAMESPACE);
            Object idTextNode = pathToJournalID.selectSingleNode(objXML);

            if (idTextNode instanceof Text) {
                return MCRObjectID.getInstance(((Text) idTextNode).getText());
            } else if (idTextNode instanceof Attribute) {
                return MCRObjectID.getInstance(((Attribute) idTextNode).getValue());
            }
        } catch (JDOMException e) {
            e.printStackTrace();
        }

        return null;
    }
}
