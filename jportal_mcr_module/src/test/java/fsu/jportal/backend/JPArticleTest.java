package fsu.jportal.backend;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.MCRTestCase;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRMetaElement;

public class JPArticleTest extends MCRTestCase {

    @Test
    public void setKeyword() throws MCRPersistenceException, MCRActiveLinkException {
        JPArticle a = new JPArticle();
        a.addKeyword("hallo");
        assertEquals(1, a.getKeywords().size());
        assertEquals("hallo", a.getKeywords().get(0));

        a.addKeyword("welt");
        assertEquals(2, a.getKeywords().size());
        assertEquals(true, a.getKeywords().contains("hallo"));
        assertEquals(true, a.getKeywords().contains("welt"));
        assertEquals(false, a.getKeywords().contains("obama"));

        MCRMetaElement me = a.getObject().getMetadata().getMetadataElement("keywords");
        assertEquals(2, me.size());
    }

}
