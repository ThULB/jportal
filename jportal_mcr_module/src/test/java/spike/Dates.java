package spike;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mycore.datamodel.common.MCRISO8601Date;
import org.mycore.services.fieldquery.data2fields.MCRXSLBuilder;

public class Dates {

    @Test
    public void test() {
        String sDate = "1999-11";
        System.out.println("mormalize: " + MCRXSLBuilder.normalizeDate("1-12"));
        MCRISO8601Date iDate = new MCRISO8601Date();
        iDate.setDate("");
        String isoDateString = iDate.getISOString();
        System.out.println("isoDate: " + isoDateString);
    }

}
