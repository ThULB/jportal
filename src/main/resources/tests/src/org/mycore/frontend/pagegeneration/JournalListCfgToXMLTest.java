package org.mycore.frontend.pagegeneration;

import java.util.ArrayList;
import java.util.List;

import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.mycore.common.MCRTestCase;
import org.mycore.frontend.pagegeneration.JournalListCfg.JournalListDef;
import org.mycore.parsers.bool.MCRAndCondition;
import org.mycore.parsers.bool.MCRCondition;
import org.mycore.services.fieldquery.MCRFieldDef;
import org.mycore.services.fieldquery.MCRQuery;
import org.mycore.services.fieldquery.MCRQueryCondition;
import org.mycore.services.fieldquery.MCRSortBy;

public class JournalListCfgToXMLTest extends MCRTestCase {
    public void testJournalListCfgToXML() throws Exception {
     // search qry
        MCRFieldDef def1 = MCRFieldDef.getDef("objectType");
        MCRCondition cond1 = new MCRQueryCondition(def1, "=", "jpjournal");
        MCRFieldDef def2 = MCRFieldDef.getDef("deletedFlag");
        MCRCondition cond2 = new MCRQueryCondition(def2, "=", "false");
        // sortBy the maintitle of the journal
        MCRSortBy sortBy = new MCRSortBy(MCRFieldDef.getDef("maintitles"), MCRSortBy.ASCENDING);
        List<MCRSortBy> sortByList = new ArrayList<MCRSortBy>();
        sortByList.add(sortBy);
        MCRQuery query = new MCRQuery(new MCRAndCondition(cond1, cond2), sortByList, 0);
        
        JournalListCfg journalListCfg = new JournalListCfg();
        JournalListDef journalListDef = new JournalListDef("journalList.xml", "journal", query);
        
        assertTrue(journalListCfg.addListDef(journalListDef));
        
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        outputter.output(new JournalListCfgToXML(journalListCfg), System.out);
    }
}
