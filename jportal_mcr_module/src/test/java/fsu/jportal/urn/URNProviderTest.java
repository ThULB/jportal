package fsu.jportal.urn;

import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.urn.services.MCRIURNProvider;
import org.mycore.urn.services.MCRURN;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Created by chi on 27.11.15.
 * @author Huu Chi Vu
 */
@RunWith(JMockit.class)
public class URNProviderTest {

    public static final String[] NS_IDENT = new String[] { "nbn", "de" };

    public static final String NISS = "urmel";

    MCRIURNProvider urmelURNProvider;

    private String NS_SPEC;

    @Before
    public void init() {
        new MockUp<MCRConfiguration>() {
            @Mock
            public String getString(String name, String defaultValue) {
                return defaultValue;
            }
        };

        urmelURNProvider = new UrmelURNProvider();
        NS_SPEC = NISS + "-" + UUID.randomUUID().toString();
    }

    @Test
    public void testGenerateURN() throws Exception {
        MCRURN urmelurn = urmelURNProvider.generateURN();

        assertURN(urmelurn);
    }

    @Test
    public void testGenerateURNAmount() throws Exception {
        MCRURN[] mcrurns = urmelURNProvider.generateURN(10);
        assertTrue(mcrurns.length == 10);

        int counter = 1;
        for (MCRURN mcrurn : mcrurns) {
            assertURN(mcrurn, counter++);
        }

    }

    @Test
    public void testGenerateURNAmountBase() throws Exception {
        MCRURN base = new MCRURN(NS_IDENT, NS_SPEC);
        MCRURN[] mcrurns = urmelURNProvider.generateURN(10, base);
        assertTrue(mcrurns.length == 10);

        int counter = 1;
        for (MCRURN mcrurn : mcrurns) {
            assertURN(mcrurn, counter++, base.getNamespaceIdentfiersSpecificPart());
        }
    }

    @Test
    public void testGenerateURNAmountBaseSetId() throws Exception {
        MCRURN base = new MCRURN(NS_IDENT, NS_SPEC);
        String setId = "0004";
        MCRURN[] mcrurns = urmelURNProvider.generateURN(10, base, setId);
        assertTrue(mcrurns.length == 10);

        int counter = 1;
        for (MCRURN mcrurn : mcrurns) {
            assertURN(mcrurn, counter++, base.getNamespaceIdentfiersSpecificPart(), setId);
        }
    }

    private void assertURN(MCRURN urmelurn) {
        assertURN(urmelurn, 0);
    }

    private void assertURN(MCRURN urmelurn, int counter) {
        assertURN(urmelurn, counter, null, null);
    }

    private void assertURN(MCRURN urmelurn, int counter, String baseNSIdSpec) {
        assertURN(urmelurn, counter, baseNSIdSpec, null);
    }

    private void assertURN(MCRURN urmelurn, int counter, String baseNSIdSpec, String setId) {
        String urn = urmelurn.toString();
        assertEquals("Wrong Schema for urn \"" + urn + "\": ", "urn", urmelurn.getSchema());
        assertArrayEquals("Wrong name space identifiers for urn \" " + urn + " \": ", NS_IDENT, urmelurn.getNamespaceIdentfiers());
        String namespaceIdentfiersSpecificPart = urmelurn.getNamespaceIdentfiersSpecificPart();

        if (baseNSIdSpec != null) {
            String errMsg = errMsgShouldStart(baseNSIdSpec, namespaceIdentfiersSpecificPart);
            assertTrue(errMsg, namespaceIdentfiersSpecificPart.startsWith(baseNSIdSpec));
        } else {
            assertTrue("Specific part for urn \"" + urn + "\" should start with '" + NISS + "': ",
                    namespaceIdentfiersSpecificPart.startsWith(NISS));
            String uuid = namespaceIdentfiersSpecificPart.substring(6, 42);
            UUID.fromString(uuid);
        }

        if (namespaceIdentfiersSpecificPart.length() >= 43) {
            String remainder = namespaceIdentfiersSpecificPart.substring(43);
            if(setId != null){
                assertTrue(errMsgShouldStart(setId, remainder), remainder.startsWith(setId));
                remainder = remainder.replaceAll(setId + "-", "");
            }

            assertEquals("Wrong counter for urn \"" + urn + "\": ", counter, Integer.parseInt(remainder));
        }

    }

    private String errMsgShouldStart(String expected, String actual) {
        return "Specific part should start with '" + expected + "' but was: '"
                + actual + "'";
    }

}
