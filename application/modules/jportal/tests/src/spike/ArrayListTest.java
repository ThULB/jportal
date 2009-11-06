package spike;

import java.util.ArrayList;

import junit.framework.TestCase;

public class ArrayListTest extends TestCase {
    public void testInitialCapacity() throws Exception {
        ArrayList<String> arrayList = new ArrayList<String>(4);
        int i = 0;
        for (String string : arrayList) {
            i++;
        }
        
        assertEquals(0, i);
    }
}
