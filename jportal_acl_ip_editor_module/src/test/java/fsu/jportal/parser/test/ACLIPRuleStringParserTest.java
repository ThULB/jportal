package fsu.jportal.parser.test;

import static org.junit.Assert.*;

import java.util.SortedMap;

import org.junit.Test;

import fsu.jportal.parser.ACLIPRuleStringParser;

public class ACLIPRuleStringParserTest {
    String ipRule = "(ip 38.117.133-144.1-5/255.255.255.0) OR (ip 10.112.34.46) OR (ip 24.97.98.1/255.255.255.0) OR (ip 24.39.199.210)  OR  (ip 46.18.27.1/255.255.255.0) OR (ip 62.154.201.1/255.255.255.0) OR (ip 66.95.127.1/255.255.255.0)";

    @Test
    public void test() {
        ACLIPRuleStringParser ruleStringParser = new ACLIPRuleStringParser(ipRule);
        SortedMap<Long, String> sortedIPs = ruleStringParser.getSortedIpMap();
        System.out.println("Sorted: " + sortedIPs.values());
    }
    
    @Test
    public void netmask() throws Exception {
        String ipRange = "38.117.133-144.1-5";
        String startIP = ipRange.replaceAll("-[0-9]{1,3}", "");
        String endIP = ipRange.replaceAll("-[0-9]{1,3}", "");
        int startIpNum = getStartIpNum(172);
        System.out.println("Start ip Num: " + startIpNum);
//        for (int i = 0; i < 255; i++) {
//            System.out.println(i + " bit and: " + (172 & i));
//        }
    }

    private int getStartIpNum(int ipNum) {
        return ipNum - (ipNum % 32);
    }
}
