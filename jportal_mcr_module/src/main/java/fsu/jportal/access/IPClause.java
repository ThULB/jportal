package fsu.jportal.access;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.mycore.access.mcrimpl.MCRAccessData;
import org.mycore.access.mcrimpl.MCRIPClause;
import org.mycore.parsers.bool.MCRIPCondition;

import fsu.jportal.parser.IPAddress;

public class IPClause implements MCRIPCondition {
    static Logger LOGGER = Logger.getLogger(IPClause.class);

    private IPAddress ipAddress;

    @Override
    public boolean evaluate(MCRAccessData data) {
        if (ipAddress.hasRange()) {
            return isInRange(data);
        } else {
            String ipWithMask = ipAddress.getStartIP() + (ipAddress.getMask() == null ? "" : "/" + ipAddress.getMask());
            return new MCRIPClause(ipWithMask).evaluate(data);
        }
    }

    private boolean isInRange(MCRAccessData data) {
        try {
            byte[] startAddress = InetAddress.getByName(ipAddress.getStartIP()).getAddress();
            byte[] endAddress = InetAddress.getByName(ipAddress.getEndIP()).getAddress();
            byte[] address = data.getIp().getAddress();
            if (startAddress.length == address.length) {
                long _startAddress = ipToLong(startAddress);
                long _address = ipToLong(address);
                long _endAddress = ipToLong(endAddress);

                if (!(_startAddress <= _address && _address <= _endAddress)) {
                    return false;
                }

                return true;
            }
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public static long ipToLong(byte[] octets) {
        long result = 0;
        for (byte octet : octets) {
            result <<= 8;
            result |= octet & 0xff;
        }
        return result;
    }

    @Override
    public Element toXML() {
        Element cond = new Element("condition");
        cond.setAttribute("field", "ip");
        cond.setAttribute("operator", "=");
        cond.setAttribute("value", ipAddress.toString());
        return cond;
    }

    @Override
    public void set(String ipRuleStr) {
        try {
            ipAddress = IPAddress.getFromString(ipRuleStr);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "ip " + ipAddress.toString() + " ";
    }

}
