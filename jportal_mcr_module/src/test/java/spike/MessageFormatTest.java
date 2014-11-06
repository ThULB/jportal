package spike;

import java.lang.reflect.Method;
import java.text.Format;
import java.text.MessageFormat;
import java.text.ParseException;

import org.junit.Test;
import org.mycore.frontend.cli.MCRAccessCommands;
import org.mycore.frontend.cli.annotation.MCRCommand;

public class MessageFormatTest {

    @Test
    public void test() {
        String toParse = "update permission read for id default with rulefile xml/grant-all.xml described by always allowed";
        
        try {
            Method method = MCRAccessCommands.class.getMethod("permissionUpdateForID", String.class, String.class, String.class, String.class);
            String pattern = method.getAnnotation(MCRCommand.class).syntax();
            MessageFormat mf = new MessageFormat(pattern);
            for (Format format : mf.getFormats()) {
                System.out.println("formats: " + format);
            }
            for (Object object : mf.parse(toParse)) {
                if(object instanceof String){
                    System.out.println((String) object);
                }
            }
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
