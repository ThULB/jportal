package fsu.jportal.backend.marc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Marc21UtilTest {

    @Test
    public void getMaintitle() {
        assertEquals("Mein Hauptitel", Marc21Util.getMaintitle("Mein Hauptitel"));
        assertEquals("Mein Hauptitel", Marc21Util.getMaintitle("Mein Hauptitel : Nebentitel"));
        assertEquals("Mein Hauptitel", Marc21Util.getMaintitle("Mein Hauptitel / Goethe"));
        assertEquals("Mein Hauptitel", Marc21Util.getMaintitle("Mein Hauptitel / Goethe : Nebentitel"));
    }

    @Test
    public void getRemainderTitle() {
        assertEquals(null, Marc21Util.getRemainderTitle("Mein Hauptitel").orElse(null));
        assertEquals("Nebentitel", Marc21Util.getRemainderTitle("Mein Hauptitel : Nebentitel").orElse(null));
        assertEquals(null, Marc21Util.getRemainderTitle("Mein Hauptitel / Goethe").orElse(null));
        assertEquals("Nebentitel", Marc21Util.getRemainderTitle("Mein Hauptitel / Goethe : Nebentitel").orElse(null));
        assertEquals("Nebentitel", Marc21Util.getRemainderTitle("Mein Hauptitel : Nebentitel / Goethe").orElse(null));
    }

    @Test
    public void getStatementOfResponsibility() {
        assertEquals(null, Marc21Util.getStatementOfResponsibility("Mein Hauptitel").orElse(null));
        assertEquals(null, Marc21Util.getStatementOfResponsibility("Mein Hauptitel : Nebentitel").orElse(null));
        assertEquals("Goethe", Marc21Util.getStatementOfResponsibility("Mein Hauptitel / Goethe").orElse(null));
        assertEquals("Goethe", Marc21Util.getStatementOfResponsibility("Mein Hauptitel / Goethe : Nebentitel").orElse(null));
        assertEquals("Goethe", Marc21Util.getStatementOfResponsibility("Mein Hauptitel : Nebentitel / Goethe").orElse(null));
    }

}
