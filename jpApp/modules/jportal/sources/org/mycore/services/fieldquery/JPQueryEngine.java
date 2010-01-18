package org.mycore.services.fieldquery;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.mycore.common.MCRConfiguration;
import org.mycore.parsers.bool.MCRAndCondition;
import org.mycore.parsers.bool.MCRCondition;

/**
 * @author "Huu Chi Vu"
 *
 */
public class JPQueryEngine extends MCRDefaultQueryEngine {
    @Override
    public MCRResults search(MCRQuery query, boolean comesFromRemoteHost) {
        query = splitCondition(query);
        query = addDeletedFlags(query);
        return super.search(query, comesFromRemoteHost);
    }
    
    protected MCRQuery splitCondition(MCRQuery query) {
        try {
            String fieldName = MCRConfiguration.instance().getString("MCR.IndexBrowser.jpperson_sub.Searchfield");
            Document queryXML = query.buildXML();
            Element condElem = (Element) XPath.selectSingleNode(queryXML, "//condition[@field='"+fieldName+"']");
            if (condElem != null) {
                MCRAndCondition andCondition = createSplittedCond(condElem);
                
                Element parentElement = condElem.getParentElement();
                condElem.detach();
                parentElement.addContent(andCondition.toXML());
                query = MCRQuery.parseXML(queryXML);
            }
        } catch (JDOMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return query;
    }

    private MCRAndCondition createSplittedCond(Element condElem) {
        String field = condElem.getAttributeValue("field");
        String operator = condElem.getAttributeValue("operator");
        String value = condElem.getAttributeValue("value");
        String[] splittedValue = value.split(" ");
        
        MCRFieldDef fieldDef = MCRFieldDef.getDef(field); 
        MCRAndCondition andCondition = new MCRAndCondition();
        for (int i = 0; i < splittedValue.length; i++) {
            MCRQueryCondition mcrQueryCondition = new MCRQueryCondition(fieldDef, operator, splittedValue[i]);
            andCondition.addChild(mcrQueryCondition);
        }
        return andCondition;
    }

    protected boolean isConditionFlagSet(MCRCondition cond, String flag) {
        if(cond.toString().contains(flag))
            return true;
        return false;
    }

    /**
     * changes the condition in a form like: ((old cond) and (deletedFlag = false)) or fileDeleted = false
     * @param doc the document to change
     * @return 
     */
    protected MCRQuery addDeletedFlags(MCRQuery query) {
        MCRCondition cond = query.getCondition();
        if(!isConditionFlagSet(cond, "deletedFlag")) {
            // create deletedFlag condition
            MCRFieldDef fieldDef = MCRFieldDef.getDef("deletedFlag");
            String op = "=";
            String value = "false";
            MCRQueryCondition deletedFlagCond = new MCRQueryCondition(fieldDef, op, value);
            cond = new MCRAndCondition(cond, deletedFlagCond);
            return new MCRQuery(cond);
        }
        
        return query;
    }
}
