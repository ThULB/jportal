package fsu.jportal.parser;

import java.util.ArrayList;

public class IPArrayList implements IPAddressList<ArrayList<IPAddress>>{
    ArrayList<IPAddress> ipAddressList = new ArrayList<IPAddress>();
    
    @Override
    public ArrayList<IPAddress> getList() {
        return ipAddressList;
    }

    @Override
    public void add(IPAddress address) {
        ipAddressList.add(address);
    }
}