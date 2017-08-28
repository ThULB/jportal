package spike;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;


public class DateFormatSpike {
    @SuppressWarnings("deprecation")
    @Test
    public void dateFormat() throws Exception {
        Date date = new Date();
        System.out.println(date.toGMTString());
        String formatedDate = toGTMString(date);
        System.out.println(formatedDate);
    }

    private String toGTMString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        dateFormat.setTimeZone(new SimpleTimeZone(0, "GTM"));
        dateFormat.applyPattern("dd MMM yyyy HH:mm:ss z");
        String formatedDate = dateFormat.format(date);
        return formatedDate;
    }
}
