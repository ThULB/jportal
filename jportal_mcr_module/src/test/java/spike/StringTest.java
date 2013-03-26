package spike;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;


public class StringTest {
	@Test
	public void testString() throws Exception {
		String string = "jportal_jpjournal_00000002";
		
		String pattern = "journal";
		assertTrue(string + " contains " + pattern, string.contains(pattern));
	}
	
	@Test
    public void replace() throws Exception {
        String testStr = "(ip 141.35.23.103) OR (ip 192.34.23.1/255.255.255.0) OR (group = klostermann)";
        System.out.println(testStr.replaceFirst("(OR )?\\(ip 192.34.23.1/255.255.255.0\\)", ""));
        System.out.println(testStr.replaceAll("(OR )?\\(ip 141.35.23.103\\)", "").replaceAll("^( OR )", ""));
    }
	
	@Test
    public void matcher() throws Exception {
        String path = "/rsc/IPRule/jportal_jpjournal_00000001/start";
        Pattern idPattern = Pattern.compile("jportal_jpjournal_[0-9]{1,8}");
        Matcher idMathcher = idPattern.matcher(path);
        
//        while (idMathcher.find()) {
        idMathcher.find();
            System.out.println(idMathcher.group());
//        }
    }
}
