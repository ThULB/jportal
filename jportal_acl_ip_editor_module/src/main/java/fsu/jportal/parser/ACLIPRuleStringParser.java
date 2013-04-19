package fsu.jportal.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;

public class ACLIPRuleStringParser {
    /* 
     * Using java.util.regex Pattern and Matcher to manipulate IPs
     * Using matcher group to gain access to certain number
     */
    private String ipNum = "([0-9]{1,3})";
    private String ipRange = ipNum + "(-" + ipNum + ")?";
    private String ip = createIpPattern(ipRange);
    private String mask = "(/" + createIpPattern(ipNum) + ")?";
    private Pattern ipPattern = Pattern.compile(ip + mask);
    
    private Pattern getPattern(){
        return ipPattern;
    }
    
    private String createIpPattern(String ipNum) {
        return "(" + ipNum + "\\." + ipNum + "\\." + ipNum + "\\." + ipNum + ")";
    }
    
    private List<String> getIPNumAsList() {
        return ipExtract(2);
    }
    
    private List<String> getRangeEndAsList() {
        return ipExtract(4);
    }
    
    private List<String> ipExtract(int indexStart) {
        List<String> ipArray = new ArrayList<String>();
        for (int i = 0; i < 4; i++) {
            int index = indexStart + i*3;
            ipArray.add(matcher.group(index));
        }
        return ipArray;
    }
    
    private String getIPAndMask() {
        return matcher.group(0);
    }
    
    private String getIP() {
        return getRawIP().replaceAll("-" + ipNum, "");
    }

    private String getRawIP() {
        return matcher.group(1);
    }
    
    private String getRangeEnd() {
        return getRawIP().replaceAll(ipNum + "-", "");
    }
    
    private long getIpAsLong(List<String> ipAsList) {
        StringBuffer ipAsNum = new StringBuffer();
        for (int i = 0; i < ipAsList.size(); i++) {
            String ipNum = ipAsList.get(i);
            if(i > 0) {
                ipNum = String.format("%03d", Integer.parseInt(ipNum));
            }
            
            ipAsNum.append(ipNum);
        }
        return Long.parseLong(ipAsNum.toString());
    }

    private Matcher getMatcher(String ipRuleStr) {
        return ipPattern.matcher(ipRuleStr);
    }

    private long getIpAsLong() {
        return getIpAsLong(getIPNumAsList());
    }
    
    private String getMask() {
        return matcher.group(15);
    }
    
    private Matcher matcher;
    
    public ACLIPRuleStringParser(String ipRuleStr) {
        matcher = ipPattern.matcher(ipRuleStr);
    }
    
    public SortedMap<Long, String> getSortedIpMap() {
        SortedMap<Long, String> sortedIPs = new TreeMap<Long, String>();
        while (matcher.find()) {
            long ipAsLong = getIpAsLong();
            String ipStr = getIPAndMask();
            sortedIPs.put(ipAsLong, ipStr );
        }
        return sortedIPs;
    }

    public JsonArray getSortedIpMapJSON() {
        SortedMap<Long, String> sortedIPs = new TreeMap<Long, String>();
        JsonArray sortedIPsJSON = new JsonArray();
        while (matcher.find()) {
            long ipAsLong = getIpAsLong();
            String ipStr = getRawIP();
            getMask();
            sortedIPs.put(ipAsLong, ipStr );
        }
        return sortedIPsJSON;
    }
}
