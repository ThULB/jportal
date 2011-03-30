package spike;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.junit.Test;

import com.sun.xml.txw2.annotation.XmlElement;

import fsu.thulb.jaxb.JaxbTools;

public class JaxbMap {
    
    @XmlRootElement
    public static class JournalListTypeMap{
        private Map<String,String> typeMap;

        public void setTypeMap(Map<String,String> typeMap) {
            this.typeMap = typeMap;
        }

        @XmlElement
        public Map<String,String> getTypeMap() {
            return typeMap;
        }
        
        public void add(String key, String val){
            if(typeMap == null){
                typeMap = new HashMap<String, String>();
            }
            typeMap.put(key, val);
        }
    }
    
    @Test
    public void testname() throws Exception {
        JournalListTypeMap obj = new JournalListTypeMap();
        obj.add("foo", "bar");
        obj.add("Hello", "Kitty");
        JaxbTools.marschall(obj, System.out);
    }
}
