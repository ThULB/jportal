package fsu.jportal.resources;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.apache.log4j.Logger;
//import org.mycore.access.mcrimpl.MCRAccessControlSystem;
import org.mycore.access.mcrimpl.MCRAccessRule;
import org.mycore.access.mcrimpl.MCRRuleStore;
import org.mycore.common.MCRCache;
import org.mycore.frontend.jersey.filter.access.MCRRestrictedAccess;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fsu.jportal.http.HttpStatus;
import fsu.jportal.jersey.access.IPRuleAccess;
import fsu.jportal.parser.IPAddress;
import fsu.jportal.parser.IPAddress.IPFormatException;
import fsu.jportal.parser.IPAddressList;
import fsu.jportal.parser.IPJsonArray;
import fsu.jportal.parser.IPMap;
import fsu.jportal.parser.IPRuleParser;
import fsu.jportal.parser.IPRuleParser.IPRuleParseException;
import fsu.jportal.pref.JournalConfig;

@Path("IPRule/{objID}")
@MCRRestrictedAccess(IPRuleAccess.class)
public class IPRuleResource {
    static Logger LOGGER = Logger.getLogger(IPRuleResource.class);

    private String defRule;

    private MCRRuleStore ruleStore;

    private MCRAccessRule rule;

    private  Map<String, IPAddress> ipAddressMap;

    @PathParam("objID") String objID;

    JournalConfig journalConf = null;

    private String ruleStr;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String listJSON() {
        String ruleid = getJournalConfKeys().getKey("ruleId");
        MCRRuleStore ruleStore = MCRRuleStore.getInstance();
        MCRAccessRule rule = ruleStore.getRule(ruleid);
        String ruleString = rule.getRuleString();

        try {
            JsonArray ipAddressListJSON = IPRuleParser.parseRule(ruleString, new IPJsonArray());
            return ipAddressListJSON.toString();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return "";
    }

    private MCRAccessRule getRule(String ruleid) {
        MCRRuleStore ruleStore = MCRRuleStore.getInstance();
        return ruleStore.getRule(ruleid);
    }

    private JournalConfig getJournalConfKeys() {
        if (journalConf == null) {
            journalConf = new JournalConfig(objID, "jportal_acl_ip_editor_module");
        }

        return journalConf;
    }

    @POST
    public Response add(String ip) {
        StatusType status;
        try {
            IPAddress ipAddress = IPAddress.getFromString(ip);
            if (getIpAddressMap().containsKey(ipAddress.getIP())) {
                return response(HttpStatus.ALLREADY_EXIST);
            }

            getIpAddressMap().put(ipAddress.getIP(), ipAddress);

            save();
            return response(Status.CREATED);
        } catch (IPFormatException e) {
            e.printStackTrace();
            return response(HttpStatus.INPUT_ERR);
        } catch (IPRuleParseException e) {
            e.printStackTrace();
            return response(HttpStatus.RULE_DB_ERR);
        }
    }

    private Response response(StatusType statusType) {
        return Response.status(statusType).build();
    }

    @DELETE
    public Response remove(String ipStr) {
        try {
            IPAddress toRemoveIpAddress = IPAddress.getFromString(ipStr);
            if (getIpAddressMap().remove(toRemoveIpAddress.getIP()) == null) {
                return response(Status.NOT_FOUND);
            }

            save();
            return response(Status.OK);
        } catch (IPFormatException e) {
            return response(HttpStatus.INPUT_ERR);
        } catch (IPRuleParseException e) {
            e.printStackTrace();
            return response(HttpStatus.RULE_DB_ERR);
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(String data) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(data).getAsJsonObject();
        String oldIp = jsonObject.get("oldIp").getAsString();
        String newIp = jsonObject.get("newIp").getAsString();

        try {
            IPAddress oldIpAddress = IPAddress.getFromString(oldIp);
            IPAddress newIpAddress = IPAddress.getFromString(newIp);
            if (getIpAddressMap().remove(oldIpAddress.getIP()) == null) {
                return response(Status.NOT_FOUND);
            }

            ipAddressMap.put(newIpAddress.getIP(), newIpAddress);

            save();
            return response(Status.OK);
        } catch (IPFormatException e) {
            e.printStackTrace();
            return response(HttpStatus.INPUT_ERR);
        } catch (IPRuleParseException e) {
            e.printStackTrace();
            return response(HttpStatus.RULE_DB_ERR);
        }
    }

    private Map<String, IPAddress> getIpRules() throws IPRuleParseException {
        String ruleId = getJournalConfKeys().getKey("ruleId");
        MCRRuleStore ruleStore = MCRRuleStore.getInstance();
        MCRAccessRule rule = ruleStore.getRule(ruleId);
        String ruleStr = rule.getRuleString();
        return IPRuleParser.parseRule(ruleStr, new IPMap());
    }

    private void saveIpRules(Map<String, IPAddress> ipRules) {
        String defRule = getJournalConfKeys().getKey("defRule");
    }

    public Map<String, IPAddress> getIpAddressMap() throws IPRuleParseException {
        if(ipAddressMap == null){
            String ruleId = getJournalConfKeys().getKey("ruleId");
            defRule = getJournalConfKeys().getKey("defRule");
            ruleStore = MCRRuleStore.getInstance();
            rule = ruleStore.getRule(ruleId);
            ruleStr = rule.getRuleString();
            ipAddressMap = IPRuleParser.parseRule(ruleStr, new IPMap());
        }

        return ipAddressMap;
    }

    private void save() {
        rule.setRule(buildRule(ipAddressMap.values(), defRule));
        ruleStore.updateRule(rule);
    }

    private String buildRule(Collection<IPAddress> values, String defRule) {
        StringBuffer newRuleStr = new StringBuffer();
        for (Iterator iterator = values.iterator(); iterator.hasNext(); ) {
            IPAddress ipAddress = (IPAddress) iterator.next();
            newRuleStr.append("(ip " + ipAddress.toString() + ")");

            if (iterator.hasNext()) {
                newRuleStr.append(" OR ");
            }
        }

        if (!values.isEmpty()) {
            newRuleStr.append(" OR ");
        }
        newRuleStr.append(defRule);

        return newRuleStr.toString();
    }
}
