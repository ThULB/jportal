package fsu.jportal.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPRuleParser {
    private static Pattern ipPattern = Pattern.compile("\\(ip ([\\w\\W&&[^\\(\\)]]*)\\)");

    public static <T> T parseRule(String ruleStr, IPAddressList<T> ipAddressList) throws IPRuleParseException {
        try {
            Matcher ipRuleMatcher = ipPattern.matcher(ruleStr);
            while (ipRuleMatcher.find()) {
                IPAddress ipAddress;
                ipAddress = IPAddress.getFromString(ipRuleMatcher.group(1).trim());
                ipAddressList.add(ipAddress);
            }

            return ipAddressList.getList();
        } catch (Exception e) {
            throw new IPRuleParseException();
        }
    }

    public static class IPRuleParseException extends Exception {
    }
}