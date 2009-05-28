package org.mycore.frontend.indexbrowser;

import org.mycore.parsers.bool.MCRAndCondition;
import org.mycore.parsers.bool.MCRCondition;
import org.mycore.services.fieldquery.MCRFieldDef;
import org.mycore.services.fieldquery.MCRQueryCondition;

public class MCRJPortalIndexBrowserSearcher extends MCRIndexBrowserSearcher {

    public MCRJPortalIndexBrowserSearcher(MCRIndexBrowserIncomingData browseData, MCRIndexBrowserConfig indexConfig) {
        super(browseData, indexConfig);
    }

    @Override
    protected MCRCondition buildCondition() {
        MCRAndCondition cAnd = new MCRAndCondition();

        // is object type
        MCRFieldDef objectTypeDef = MCRFieldDef.getDef("objectType");
        cAnd.addChild(new MCRQueryCondition(objectTypeDef, "=", indexConfig.getTable()));
        
        // is a search string defined?
        if (browseData.getSearch() != null && browseData.getSearch().length() > 0) {
            MCRFieldDef field = MCRFieldDef.getDef(indexConfig.getBrowseField());
            String value = browseData.getSearch();
            String operator = getOperator();
            cAnd.addChild(new MCRQueryCondition(field, operator, value));
        }

        // is deleted
        MCRFieldDef deletedDef = MCRFieldDef.getDef("deletedFlag");
        cAnd.addChild(new MCRQueryCondition(deletedDef, "=", Boolean.toString(false)));

        return cAnd;
    }
    
}
