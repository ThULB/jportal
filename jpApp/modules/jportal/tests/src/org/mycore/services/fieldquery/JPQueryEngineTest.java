package org.mycore.services.fieldquery;

import junit.framework.TestCase;

import org.jdom.Element;
import org.mycore.parsers.bool.MCRCondition;

public class JPQueryEngineTest extends TestCase {
    public void testSplitQuery() throws Exception {
        MCRQueryParser mcrQueryParser = new MCRQueryParser();
        MCRCondition condition = mcrQueryParser.parse("(objectType = \"foo\")");
        
        assertNotNull(condition);
    }
}
