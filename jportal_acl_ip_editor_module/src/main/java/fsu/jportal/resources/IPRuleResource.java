package fsu.jportal.resources;

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
import org.mycore.access.mcrimpl.MCRAccessControlSystem;
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

@Path("IPRule/{objID}")
@MCRRestrictedAccess(IPRuleAccess.class)
public class IPRuleResource {
    static Logger LOGGER = Logger.getLogger(IPRuleResource.class);
    
    static MCRCache<String, MCRAccessRule> accessCache;

    static {
        MCRAccessControlSystem.instance();
        accessCache = MCRAccessControlSystem.getCache();
    }

    @PathParam("objID")
    String objID;

    JournalConfig journalConf = null;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String listJSON() {
        String ruleid = getJournalConfKeys().get("ruleId");
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

    private HashMap<String, String> getJournalConfKeys() {
        if (journalConf == null) {
            journalConf = new JournalConfig(objID);
        }

        return journalConf.getJournalConfKeys();
    }

    @POST
    public Response add(String ip) {
        HashMap<String, String> journalConfKeys = getJournalConfKeys();
        String ruleId = journalConfKeys.get("ruleId");
        String defRule = journalConfKeys.get("defRule");
        
        return response(addIp(ruleId, ip, defRule));
    }

    private Response response(StatusType statusType) {
        return Response.status(statusType).build();
    }

    @DELETE
    public Response remove(String ipStr) {
        HashMap<String, String> journalConfKeys = getJournalConfKeys();
        String ruleId = journalConfKeys.get("ruleId");
        String defRule = journalConfKeys.get("defRule");
        return response(removeIp(ruleId, ipStr, defRule));
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public String removeList(String json) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();
        JsonArray ipsAsJSONArray = jsonObject.getAsJsonArray("ips");
        HashMap<String, String> journalConfKeys = getJournalConfKeys();
        String ruleId = journalConfKeys.get("ruleId");

        return ipsAsJSONArray.toString();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(String data) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(data).getAsJsonObject();
        String newIp = jsonObject.get("newIp").getAsString();
        String oldIp = jsonObject.get("oldIp").getAsString();

        HashMap<String, String> journalConfKeys = getJournalConfKeys();
        String ruleId = journalConfKeys.get("ruleId");
        String defRule = journalConfKeys.get("defRule");
        
        StatusType removeStatus = removeIp(ruleId, oldIp, defRule);
        
        if(removeStatus.getStatusCode() != Status.OK.getStatusCode()){
            return response(removeStatus);
        }
        
        return response(addIp(ruleId, newIp, defRule));
    }

    class IPSet implements IPAddressList<Set<String>> {
        private Set<String> ipSet = new HashSet<String>();

        @Override
        public Set<String> getList() {
            return ipSet;
        }

        @Override
        public void add(IPAddress address) {
            ipSet.add(address.getIP());
        }

    }

    private StatusType addIp(String ruleid, String ip, String defRule) {
        MCRRuleStore ruleStore = MCRRuleStore.getInstance();
        MCRAccessRule rule = ruleStore.getRule(ruleid);
        String ruleStr = rule.getRuleString();
        
        try {
            IPAddress ipAddress = IPAddress.getFromString(ip);
            Map<String, IPAddress> ipRules = IPRuleParser.parseRule(ruleStr, new IPMap());
            if(ipRules.containsKey(ipAddress.getIP())){
                return HttpStatus.ALLREADY_EXIST;
            }
            
            ipRules.put(ipAddress.getIP(), ipAddress);

            rule.setRule(buildRule(ipRules.values(), defRule));
            ruleStore.updateRule(rule);
            updateRuleCache(ruleid, rule);
            
            return Status.CREATED;
        } catch (IPFormatException e) {
            e.printStackTrace();
            return HttpStatus.INPUT_ERR;
        } catch (IPRuleParseException e) {
            return HttpStatus.RULE_DB_ERR;
        }
    }

    private void updateRuleCache(String ruleid, MCRAccessRule rule) {
        MCRCache<String, MCRAccessRule> cache = MCRAccessControlSystem.getCache();
//        cache.put(ruleid, rule);
        cache.remove(ruleid);
    }

    private String buildRule(Collection<IPAddress> values, String defRule) {
        StringBuffer newRuleStr = new StringBuffer();
        for (Iterator iterator = values.iterator(); iterator.hasNext();) {
            IPAddress ipAddress = (IPAddress) iterator.next();
            newRuleStr.append("(ip " + ipAddress.toString() +")");
            
            if(iterator.hasNext()){
                newRuleStr.append(" OR ");
            }
        }
        
        if(!values.isEmpty()){
            newRuleStr.append(" OR ");
        }
        newRuleStr.append(defRule);
        
        return newRuleStr.toString();
    }
    
    

    private StatusType removeIp(String ruleid, String ipStr, String defRule) {
        MCRRuleStore ruleStore = MCRRuleStore.getInstance();
        MCRAccessRule rule = ruleStore.getRule(ruleid);
        String ruleStr = rule.getRuleString();
        
            try {
                IPAddress toRemoveIpAddress = IPAddress.getFromString(ipStr);
                Map<String, IPAddress> ipRules = IPRuleParser.parseRule(ruleStr, new IPMap());
                if(ipRules.remove(toRemoveIpAddress.getIP()) == null){
                    return Status.NOT_FOUND;
                }
                
                rule.setRule(buildRule(ipRules.values(), defRule));
                ruleStore.updateRule(rule);
                updateRuleCache(ruleid, rule);
                
                return Status.OK;
            } catch (IPRuleParseException e) {
                return HttpStatus.RULE_DB_ERR;
            } catch (IPFormatException e) {
                return HttpStatus.INPUT_ERR;
            }
    }
    
    private StatusType updateIp(String ruleid, String oldIpStr, String newIpStr) {
        MCRRuleStore ruleStore = MCRRuleStore.getInstance();
        MCRAccessRule rule = ruleStore.getRule(ruleid);
        String ruleStr = rule.getRuleString();
        
        try {
            IPAddress oldIpAddress = IPAddress.getFromString(oldIpStr);
            IPAddress newIpAddress = IPAddress.getFromString(newIpStr);
            Map<String, IPAddress> ipRules = IPRuleParser.parseRule(ruleStr, new IPMap());
            if(ipRules.remove(oldIpAddress.getIP()) == null){
                return Status.NOT_FOUND;
            }
            
            ipRules.put(newIpAddress.getIP(), newIpAddress);
            StringBuffer newRuleStr = new StringBuffer();
            Collection<IPAddress> values = ipRules.values();
            for (Iterator iterator = values.iterator(); iterator.hasNext();) {
                IPAddress ipAddress = (IPAddress) iterator.next();
                newRuleStr.append("(ip " + ipAddress.toString() +")");
                newRuleStr.append("(ip " + ipAddress.toString() +")");
                
                if(iterator.hasNext()){
                    newRuleStr.append(" OR ");
                }
            }
            
            return Status.OK;
        } catch (IPRuleParseException e) {
            return HttpStatus.RULE_DB_ERR;
        } catch (IPFormatException e) {
            return HttpStatus.INPUT_ERR;
        }
    }

    protected static MCRCache<String, MCRAccessRule> getAccessCache() {
        return accessCache;
    }
}
