package fsu.jportal.urn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.urn.services.MCRIURNProvider;
import org.mycore.urn.services.MCRURN;

import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

/**
 * Created by chi on 27.11.15.
 * @author Huu Chi Vu
 */
@RunWith(JMockit.class)
public class URNProviderTest {

    public static final String NISS = "urmel";

    MCRIURNProvider urmelURNProvider;

    private String urnUUID;

    @Before
    public void init() {
        new MockUp<MCRConfiguration>() {
            @Mock
            public String getString(String name, String defaultValue) {
                return defaultValue;
            }
        };

        urmelURNProvider = new UrmelURNProvider();
        urnUUID = UUID.randomUUID().toString();
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
        MCRURN base = MCRURN.create(NISS, urnUUID);
        MCRURN[] mcrurns = urmelURNProvider.generateURN(10, base);
        assertTrue(mcrurns.length == 10);

        int counter = 1;
        for (MCRURN mcrurn : mcrurns) {
            assertURN(mcrurn, counter++, base.getSubNamespaces());
        }
    }

    @Test
    public void testGenerateURNAmountBaseSetId() throws Exception {
        MCRURN base = MCRURN.create(NISS, urnUUID);
        String setId = "0004";
        MCRURN[] mcrurns = urmelURNProvider.generateURN(10, base, setId);
        assertTrue(mcrurns.length == 10);

        int counter = 1;
        for (MCRURN mcrurn : mcrurns) {
            assertURN(mcrurn, counter++, base.getSubNamespaces(), setId);
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
        String subNamespaces = urmelurn.getSubNamespaces();

        String namespaceSpecificString = urmelurn.getNamespaceSpecificString();
        if (baseNSIdSpec != null) {
            String errMsg = errMsgShouldStart(baseNSIdSpec, subNamespaces);
            assertTrue(errMsg, subNamespaces.equals(baseNSIdSpec));
        } else {
            assertTrue("Specific part for urn \"" + urn + "\" should start with '" + NISS + "': ",
                       NISS.equals(subNamespaces));
            String uuid = namespaceSpecificString.substring(0, 36);
            UUID.fromString(uuid);
        }

        if (namespaceSpecificString.length() >= 37) {
            String remainder = namespaceSpecificString.substring(37);
            if (setId != null) {
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
