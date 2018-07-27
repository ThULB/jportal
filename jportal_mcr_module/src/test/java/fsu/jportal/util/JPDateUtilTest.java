package fsu.jportal.util;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;

import org.junit.Assert;
import org.junit.Test;

public class JPDateUtilTest {

    @Test
    public void parse() {
        Assert.assertEquals(Year.of(2000), JPDateUtil.parse("2000"));
        Assert.assertEquals(Year.of(-25), JPDateUtil.parse("-0025"));

        Assert.assertEquals(YearMonth.of(2000, 10), JPDateUtil.parse("2000-10"));
        Assert.assertEquals(YearMonth.of(-25, 10), JPDateUtil.parse("-0025-10"));

        Assert.assertEquals(LocalDate.of(2000, 10, 5), JPDateUtil.parse("2000-10-05"));
        Assert.assertEquals(LocalDate.of(-25, 10, 5), JPDateUtil.parse("-0025-10-05"));
    }

}
