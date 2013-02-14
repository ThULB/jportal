package fsu.jportal.access;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Text;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
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

        if (objId == null) {
            return false;
        }

        MCRObjectID parentID = getParentID(objId);
        if (objId == null || parentID == null) {
            return false;
        }

        if (parentID.equals(objId)) {
            if (getAccessStrategyConfig().getAccessInterface().hasRule(parentID.toString(), permission)) {
                return getAccessStrategyConfig().getAccessInterface().checkPermission(parentID.toString(), permission);
            } else {
                return false;
            }
        }

        return getAlternative().checkPermission(parentID.toString(), permission);
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
        if (!getAccessStrategyConfig().getXMLMetadataMgr().exists(objID)) {
            return null;
        }
        Document objXML = getAccessStrategyConfig().getXMLMetadataMgr().retrieveXML(objID);
        if (objID.getTypeId().equals("derivate")) {
            XPathExpression<Attribute> xpath = XPathFactory.instance().compile("/mycorederivate/derivate/linkmetas/linkmeta/@xlink:href",
                    Filters.attribute(), null, MCRConstants.XLINK_NAMESPACE);
            Attribute attr = xpath.evaluateFirst(objXML);
            if (attr != null) {
                return MCRObjectID.getInstance(attr.getValue());
            }
        } else {
            XPathExpression<Text> xpath = XPathFactory.instance().compile(
                    "/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID/text()", Filters.text());
            Text text = xpath.evaluateFirst(objXML);
            if (text != null) {
                return MCRObjectID.getInstance(text.getText());
            }
        }
        return null;
    }

}
