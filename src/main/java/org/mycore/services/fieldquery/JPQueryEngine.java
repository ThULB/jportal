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
        return super.search(splitCondition(query), comesFromRemoteHost);
    }

    /**
     * Splits the string of a query field into an and-condition.
     * E.g. anyname="Max Mueller" --> (anyname="Max") and (anyname=Mueller) 
     * @param query
     * @return
     */
    protected MCRQuery splitCondition(MCRQuery query) {
        if (query != null) {
            try {
                String fieldName = MCRConfiguration.instance().getString("MCR.IndexBrowser.jpperson_sub.Searchfield");
                Document queryXML = query.buildXML();
                Element condElem = (Element) XPath.selectSingleNode(queryXML, "//condition[@field='" + fieldName + "']");
                if (condElem != null) {
                    MCRAndCondition andCondition = createSplittedCond(condElem);

                    Element parentElement = condElem.getParentElement();
                    condElem.detach();
                    parentElement.addContent(andCondition.toXML());
                    query = MCRQuery.parseXML(queryXML);
                }
            } catch (JDOMException e) {
                e.printStackTrace();
            }
        }
        
        return query;
    }

    private MCRAndCondition createSplittedCond(Element condElem) {
        String field = condElem.getAttributeValue("field");
        String operator = condElem.getAttributeValue("operator");
        String value = condElem.getAttributeValue("value");
        value = value.replaceAll(",", "");
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
}
