package spike;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonSpike {
    private class KeyValue {
        private String key1;
        String key2;
        public void setKey1(String key1) {
            this.key1 = key1;
        }
        public String getKey1() {
            return key1;
        }
    }
    @Test
    public void JsonToMap() throws Exception {
        String json = "{'key1': 'val1', 'key2':'val2'}";
        Gson gson = new GsonBuilder().create();
        KeyValue fromJson = gson.fromJson(json, KeyValue.class);
        System.out.println(fromJson.key1);
    }
}
