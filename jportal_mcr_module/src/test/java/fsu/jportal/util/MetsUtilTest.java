package fsu.jportal.util;

import org.junit.Assert;
import org.junit.Test;

public class MetsUtilTest {

    @Test
    public void interpolateOrderLabel() {
        Assert.assertEquals("6", MetsUtil.interpolateOrderLabel("5", 1));
        Assert.assertEquals("7", MetsUtil.interpolateOrderLabel("5", 2));

        Assert.assertEquals("7v", MetsUtil.interpolateOrderLabel("7v", 0));
        Assert.assertEquals("7r", MetsUtil.interpolateOrderLabel("7v", 1));
        Assert.assertEquals("8v", MetsUtil.interpolateOrderLabel("7v", 2));
        Assert.assertEquals("8r", MetsUtil.interpolateOrderLabel("7v", 3));
        Assert.assertEquals("9v", MetsUtil.interpolateOrderLabel("7v", 4));

        Assert.assertEquals("7r", MetsUtil.interpolateOrderLabel("7r", 0));
        Assert.assertEquals("8v", MetsUtil.interpolateOrderLabel("7r", 1));
        Assert.assertEquals("8r", MetsUtil.interpolateOrderLabel("7r", 2));
        Assert.assertEquals("9v", MetsUtil.interpolateOrderLabel("7r", 3));
        Assert.assertEquals("9r", MetsUtil.interpolateOrderLabel("7r", 4));
    }

}
