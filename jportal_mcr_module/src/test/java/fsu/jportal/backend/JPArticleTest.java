package fsu.jportal.backend;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mycore.common.MCRJPATestCase;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.metadata.MCRMetaElement;

public class JPArticleTest extends MCRJPATestCase {

    @Test
    public void setKeyword() throws MCRPersistenceException {
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
