package fsu.jportal.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class IPJsonArray implements IPAddressList<JsonArray>{
    JsonArray ipAddressListJSON = new JsonArray();
    
    @Override
    public JsonArray getList() {
        return ipAddressListJSON;
    }

    @Override
    public void add(IPAddress address) {
        JsonObject ipAddressJSON = new JsonObject();
        ipAddressJSON.addProperty("ip", address.getIP());
        String abo = address.getAbo();
        ipAddressJSON.addProperty("abo", abo == null? "-" : abo);
        ipAddressListJSON.add(ipAddressJSON);
    }
    
}