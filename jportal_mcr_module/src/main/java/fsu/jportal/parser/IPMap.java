package fsu.jportal.parser;

import java.util.HashMap;
import java.util.Map;

public class IPMap implements IPAddressList<Map<String, IPAddress>> {
    private HashMap<String, IPAddress> ipMap = new HashMap<String, IPAddress>();
    @Override
    
    public Map<String, IPAddress> getList() {
        return ipMap;
    }

    @Override
    public void add(IPAddress address) {
        ipMap.put(address.getIP(), address);
    }

}
