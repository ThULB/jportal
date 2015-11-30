package fsu.jportal.urn;

import junit.framework.Assert;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.urn.services.MCRURN;

import static org.junit.Assert.*;

/**
 * Created by chi on 27.11.15.
 * @author Huu Chi Vu
 */
@RunWith(JMockit.class)
public class URNProviderTest {
    @Before
    public void init() {
        new MockUp<MCRConfiguration>() {
            @Mock
            public String getString(String name, String defaultValue) {
                return defaultValue;
            }
        };
    }

    @Test
    public void testGenerateURN() throws Exception {
        UrmelURNProvider urmelURNProvider = new UrmelURNProvider();
        MCRURN urmelurn = urmelURNProvider.generateURN();

        assertURN(urmelurn);
    }

    @Test
    public void testGenerateURNAmount() throws Exception {
        UrmelURNProvider urmelURNProvider = new UrmelURNProvider();
        MCRURN[] mcrurns = urmelURNProvider.generateURN(10);
        assertTrue(mcrurns.length == 10);

        for (MCRURN mcrurn : mcrurns) {
            assertURN(mcrurn);
        }

    }

    private void assertURN(MCRURN urmelurn) {
        assertEquals("Wrong Schema: ", "urn", urmelurn.getSchema());
        assertArrayEquals("Wrong name space identifiers: ", new String[] { "nbn", "de" },
                urmelurn.getNamespaceIdentfiers());
        String namespaceIdentfiersSpecificPart = urmelurn.getNamespaceIdentfiersSpecificPart();
        assertTrue("Specific part should start with 'urmel': ",
                namespaceIdentfiersSpecificPart.startsWith("urmel"));
        String uuid = namespaceIdentfiersSpecificPart.substring(6);
        System.out.println("UUID: " + uuid);
    }

}
