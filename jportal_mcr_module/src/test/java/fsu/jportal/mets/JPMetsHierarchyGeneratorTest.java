package fsu.jportal.mets;

import org.junit.Assert;
import org.junit.Test;

public class JPMetsHierarchyGeneratorTest {

    @Test
    public void interpolateOrderLabel() {
        JPMetsHierarchyGenerator generator = new JPMetsHierarchyGenerator();
        Assert.assertEquals("6", generator.interpolateOrderLabel("5", 1));
        Assert.assertEquals("7", generator.interpolateOrderLabel("5", 2));

        Assert.assertEquals("7v", generator.interpolateOrderLabel("7v", 0));
        Assert.assertEquals("7r", generator.interpolateOrderLabel("7v", 1));
        Assert.assertEquals("8v", generator.interpolateOrderLabel("7v", 2));
        Assert.assertEquals("8r", generator.interpolateOrderLabel("7v", 3));
        Assert.assertEquals("9v", generator.interpolateOrderLabel("7v", 4));

        Assert.assertEquals("7r", generator.interpolateOrderLabel("7r", 0));
        Assert.assertEquals("8v", generator.interpolateOrderLabel("7r", 1));
        Assert.assertEquals("8r", generator.interpolateOrderLabel("7r", 2));
        Assert.assertEquals("9v", generator.interpolateOrderLabel("7r", 3));
        Assert.assertEquals("9r", generator.interpolateOrderLabel("7r", 4));
    }

}
