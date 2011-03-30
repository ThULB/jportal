package spike;

import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class StringTest {
	@Test
	public void testString() throws Exception {
		String string = "jportal_jpjournal_00000002";
		
		String pattern = "journal";
		assertTrue(string + " contains " + pattern, string.contains(pattern));
	}
}
