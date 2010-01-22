package org.mycore.services.fieldquery;

import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRTestCase;
import org.mycore.parsers.bool.MCRCondition;

public class JPQueryEngineTest extends MCRTestCase {
    private static final String SPLITTED_QUERY_STRING = "(objectType = \"foo\") AND (objectType = \"doo\")";
    private static final String QUERY_STRING = "objectType = \"foo doo\"";

    @Override
    protected void setUp() throws Exception {
        MCRConfiguration.instance().set("MCR.IndexBrowser.jpperson_sub.Searchfield", "objectType");
        super.setUp();
    }
    
    public void testSplitQuery() throws Exception {
        MCRQuery splittedQuery = splitQuery(createQuery(QUERY_STRING));
        
        assertEquals(SPLITTED_QUERY_STRING, splittedQuery.getCondition().toString());
    }

    private MCRQuery splitQuery(MCRQuery query) {
        JPQueryEngine queryEngine = new JPQueryEngine();
        MCRQuery splittedQuery = queryEngine.splitCondition(query);
        return splittedQuery;
    }

    private MCRQuery createQuery(String queryString) {
        MCRQueryParser mcrQueryParser = new MCRQueryParser();
        MCRCondition condition = MCRQueryParser.normalizeCondition(mcrQueryParser.parse(queryString));
        assertNotNull(condition);
        
        MCRQuery query = new MCRQuery(condition);
        return query;
    }
    
    public void testSplitQueryWithoutMatchingSearchfield() throws Exception {
        MCRConfiguration.instance().set("MCR.IndexBrowser.jpperson_sub.Searchfield", "whatever");
        MCRQuery splittedQuery = splitQuery(createQuery(QUERY_STRING));
        
        assertEquals(QUERY_STRING, splittedQuery.getCondition().toString());
    }
    
    public void testSplitQueryNull() throws Exception {
        MCRQuery splittedQuery = splitQuery(null);
        
        assertNull(splittedQuery);
    }
}
