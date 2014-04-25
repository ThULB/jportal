package spike;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class PatternMatcherTest {
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

    @Test
    public void ownerIDPath() throws Exception {
        String path = "/jportal_derivate_00000001/foo/test.jpg";
        String path2 = "/test.jpg";
        Pattern idPattern = Pattern.compile("/(jportal_\\w*_[0-9]{1,8})*((/.*)*/(.*)$)?");
        //	    Pattern idPattern = Pattern.compile("/(jportal_\\w*_[0-9]{1,8})((/.*)*/(.*)$)?");
        Matcher idMathcher = idPattern.matcher(path);

        System.out.println("Matcher 1");
        while (idMathcher.find()) {
            //	    idMathcher.find();
            System.out.println(idMathcher.group(1));
            System.out.println(idMathcher.group(2));
            System.out.println(idMathcher.group(3));
            System.out.println(idMathcher.group(4));
        }

        Matcher idMathcher2 = idPattern.matcher(path2);

        System.out.println("Matcher 2");
        while (idMathcher2.find()) {
            //	    idMathcher.find();
            System.out.println(idMathcher2.group(1));
            System.out.println(idMathcher2.group(2));
            System.out.println(idMathcher2.group(3));
            System.out.println(idMathcher2.group(4));
        }
    }
}
