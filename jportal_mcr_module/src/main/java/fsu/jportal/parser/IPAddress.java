package fsu.jportal.parser;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPAddress {
    // ipParts[0] = abo 
    // ipParts[1] = mask 
    // ipParts[2] = endIP 
    // ipParts[3] = startIP 
    private String[] ipParts = new String[4];

    private String[] delim = { " ", "/", "-" };

    private String origStr = null;

    private String ipRange = null;

    private InetAddress startInetAddress;

    private InetAddress endInetAddress;

    private InetAddress inetMask;

    private IPAddress() {
    }
    
    public static class IPFormatException extends Exception{}

    public static IPAddress getFromString(String ipStr) throws IPFormatException {
        IPFormatException wrongFormat = new IPFormatException();
        
        if(ipStr != null){
            if("".equals(ipStr.trim())){
                throw wrongFormat;
            }
            
            IPAddress ipAddress = new IPAddress();
            
            ipAddress.origStr = ipAddress.ipParts[3] = ipStr;
            
            for (int i = 0; i < 3; i++) {
                int delimPos = ipAddress.ipParts[3].indexOf(ipAddress.delim[i]);
                if (delimPos != -1) {
                    if (i == 2) {
                        ipAddress.ipRange = ipAddress.ipParts[3];
                        ipAddress.ipParts[i] = ipAddress.ipParts[3].replaceAll("\\d{1,3}-", "");
                        ipAddress.ipParts[3] = ipAddress.ipParts[3].replaceAll("-\\d{1,3}", "");
                    } else {
                        ipAddress.ipParts[i] = ipAddress.ipParts[3].substring(delimPos + 1);
                        ipAddress.ipParts[3] = ipAddress.ipParts[3].substring(0, delimPos);
                    }
                }
            }
            
            if(ipAddress.hasRange() && ipAddress.hasMask()){
                throw wrongFormat;
            }
            
            try {
                ipAddress.startInetAddress = InetAddress.getByName(ipAddress.getStartIP());
                
                if(ipAddress.hasRange()){
                    ipAddress.endInetAddress = InetAddress.getByName(ipAddress.getEndIP());
                }
                
                if(ipAddress.hasMask()){
                    ipAddress.inetMask = InetAddress.getByName(ipAddress.getMask());
                }
            } catch (UnknownHostException e) {
                throw wrongFormat;
            }
            
            return ipAddress;
            
        }
        
        throw wrongFormat;
    }

    public String getAbo() {
        return ipParts[0];
    }

    public String getMask() {
        return ipParts[1];
    }

    public String getEndIP() {
        return ipParts[2];
    }

    public String getStartIP() {
        return ipParts[3];
    }
    
    public InetAddress getStartInetAddress(){
        return startInetAddress;
    }
    
    public InetAddress getEndInetAddress(){
        return endInetAddress;
    }
    
    public InetAddress getInetMask(){
        return inetMask;
    }

    public boolean hasRange() {
        return getEndIP() != null;
    }

    public boolean hasMask() {
        return getMask() != null;
    }

    public String getIP() {
        if (hasRange()) {
            return ipRange;
        } else if (hasMask()) {
            return getStartIP() + "/" + getMask();
        } else {
            return getStartIP();
        }
    }

    public String info() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("startIP: ");
        stringBuilder.append(getStartIP());
        stringBuilder.append(" endIP: ");
        stringBuilder.append(getEndIP());
        stringBuilder.append(" mask: ");
        stringBuilder.append(getMask());
        stringBuilder.append(" abo: ");
        stringBuilder.append(getAbo());
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return origStr;
    }
}