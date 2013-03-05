package fsu.jportal.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.mycore.access.mcrimpl.MCRAccessControlSystem;
import org.mycore.access.mcrimpl.MCRAccessRule;
import org.mycore.access.mcrimpl.MCRRuleStore;
import org.mycore.common.MCRCache;

@Path("IPRule")
public class IPRuleResource {

    static Logger LOGGER = Logger.getLogger(IPRuleResource.class);
    
    static MCRCache<String, MCRAccessRule> accessCache;

    static {
        MCRAccessControlSystem.instance();
        accessCache = MCRAccessControlSystem.getCache();
    }

    @GET
    @Path("list")
    public JSONArray list(@QueryParam("ruleId") String ruleid) throws JSONException {
        //get the ruleString
        MCRRuleStore ruleStore = MCRRuleStore.getInstance();
        MCRAccessRule accessRule = ruleStore.getRule(ruleid);
        String ruleString = accessRule.getRuleString();
        
        JSONArray jsonA = new JSONArray();
        if (!ruleString.equals("")){
            String[] ruleArray = ruleString.split("\\s*OR\\s*");
                   
            List<IpAdress> ipList = new ArrayList<IpAdress>(); 
            for(String rule : ruleArray){
                if(rule.contains("ip")){
                    rule = rule.replaceAll("\\(ip ", "");
                    rule = rule.replaceAll("\\)", "");
                    ipList.add(new IpAdress(rule));
                }
            }
            Collections.sort(ipList);
            for (IpAdress ip : ipList){
                JSONObject jsonO = new JSONObject();
                jsonO.put("ip", ip.getIpWithMask());
                jsonA.put(jsonO);
            }
        }        
        return jsonA;
    }
    
    @GET
    @Path("add")
    public Response add(@QueryParam("ruleId") String ruleid, @QueryParam("ip") String ip, @QueryParam("defRule") String defRule) {
        if (addIp(ruleid, ip, defRule)){
            return Response.ok().build();
        }
        else{
            return Response.status(409).build();
        }
    }
    
    @GET
    @Path("remove")
    public Response remove(@QueryParam("ruleId") String ruleid, @QueryParam("ip") String ip) {
        if (removeIp(ruleid, ip)){
            return Response.ok().build();
        }
        else{
            return Response.status(409).build();
        }
    }
    
    @POST
    @Path("removeList")
    @Consumes(MediaType.APPLICATION_JSON)
    public JSONArray removeList(JSONObject json) throws JSONException {
        JSONArray ipsAsJSONArray = json.getJSONArray("ips");
        for(int i = 0; i < ipsAsJSONArray.length(); i++){
            String ip = ipsAsJSONArray.getJSONObject(i).getString("ip");
            if(removeIp(json.getString("ruleid"), ip)){
                ipsAsJSONArray.getJSONObject(i).put("success", "1");
            }
            else{
                ipsAsJSONArray.getJSONObject(i).put("success", "0");
            }
        }
        return ipsAsJSONArray;
    }
    
    @GET
    @Path("edit")
    public Response edit(@QueryParam("ruleId") String ruleid, @QueryParam("newIp") String newIp, @QueryParam("oldIp") String oldIp, @QueryParam("defRule") String defRule) {
        if (removeIp(ruleid, oldIp)){
            if(addIp(ruleid, newIp, defRule)){
                return Response.ok().build();
            }
            else{
                addIp(ruleid, oldIp, defRule);
                return Response.status(409).build();
            }
        }
        else{
            return Response.status(409).build();
        }
    }
    
    private boolean addIp(String ruleid, String ip, String defRule) {
        MCRRuleStore ruleStore = MCRRuleStore.getInstance();
        MCRAccessRule rule = ruleStore.getRule(ruleid);
        String ruleAsString = rule.getRuleString().replaceAll("\\s+", " ");
        
        String newRuleAsString = "";
        IpAdress ipAdr = new IpAdress(ip);
        newRuleAsString = " OR (ip " + ipAdr.getIpAndMask() + ")";

        if(ruleAsString.contains("ip") || !ruleAsString.equals("")){
            if (ruleAsString.contains(newRuleAsString) || ruleAsString.contains(newRuleAsString.substring(4))){
                return false;
            }
            else{
                ruleAsString += newRuleAsString;           
            }
        }
        else{
            ruleAsString += newRuleAsString.substring(4);
        }
        if(!ruleAsString.equals("")){
            if (!ruleAsString.contains(" OR " + defRule)){
                ruleAsString += " OR " + defRule; 
            }
            rule.setRule(ruleAsString);
            ruleStore.updateRule(rule);
            getAccessCache().put(ruleid, rule);
            return true; 
        }
        else{
            return false;
        }
    }
    
    private boolean removeIp(String ruleid, String ip) {
        MCRRuleStore ruleStore = MCRRuleStore.getInstance();
        MCRAccessRule rule = ruleStore.getRule(ruleid);
        String ruleAsString = rule.getRuleString().replaceAll("\\s+", " ");
        
        String deleteString = "";
        IpAdress ipAdr = new IpAdress(ip);
        deleteString = " OR (ip " + ipAdr.getIpAndMask() + ")"; 
        
        String newRuleAsString = "";
        if (!ruleAsString.contains(deleteString)){
            if(!ruleAsString.contains(deleteString.substring(4))){
                return false;
            }
            else{
                newRuleAsString = ruleAsString.replace(deleteString.substring(4), "");
                newRuleAsString = newRuleAsString.replaceFirst(" OR ", "");
            }
        }
        else{
            newRuleAsString = ruleAsString.replace(deleteString, "");
        }
        rule.setRule(newRuleAsString);
        ruleStore.updateRule(rule);
        getAccessCache().put(ruleid, rule);
        return true;
    }
        
    protected static MCRCache<String, MCRAccessRule> getAccessCache() {
        return accessCache;
    }
    
    private static class IpAdress implements Comparable<IpAdress>{
        private int[] ip;
        
        private int[] mask;
        
        public IpAdress(String ip){
            this.ip = new int[4];
            this.mask = new int[4];
            
            if(ip.contains("/")){
                String[] ipAndMask = ip.split("/");
                setIpAndMask(ipAndMask[0], ipAndMask[1]);
            }
            else{
                computateMask(ip);               
            }
        }

//        public IpAdress(String ip, String mask){
//            this.ip = new int[4];
//            this.mask = new int[4]; 
//            setIpAndMask(ip, mask);
//        }
        
        private void computateMask(String ip){
            String[] ipAsArray = ip.split("\\.");
            for(int i = 0;  i<= 3; i++){
                if(ipAsArray[i].equals("*")){
                    this.ip[i] = 1;
                    this.mask[i] = 0;
                }
                else{
                    this.ip[i] = Integer.parseInt(ipAsArray[i]);
                    this.mask[i] = 255;
                }
            }
        }
        
        public void setIpAndMask(String ip, String mask) {
            String[] ipAsStringArray = ip.split("\\.");
            String[] maskAsStringArray = mask.split("\\.");
            for (int i = 0; i <= 3; i++){
                this.ip[i] = Integer.parseInt(ipAsStringArray[i]);
                this.mask[i] = Integer.parseInt(maskAsStringArray[i]); 
            } 
        }
        
        public int[] getIp(){
            return this.ip;
        }
        
        public String getIpWithMask() {
            String ip = "";
            for(int i = 0;  i <= 3; i++){
                if(this.mask[i] == 0){
                    ip += "*";
                }
                else{
                    ip += this.ip[i];
                }
                if (i < 3) ip += ".";
            }
            return ip;
        }
        
        public String getIpAndMask() {
            String ipAndMask = "";
            for(int i = 0;  i<= 3; i++){
                ipAndMask += this.ip[i];
                if (i < 3) ipAndMask += ".";
            }
            ipAndMask += "/";
            for(int j = 0;  j<= 3; j++){
                ipAndMask += this.mask[j];
                if (j < 3) ipAndMask += ".";
            }
            return ipAndMask;
        }
        
        @Override
        public int compareTo(IpAdress ipAd){
            int[] ipAdArray = ipAd.getIp();
            int comp = Integer.compare(this.ip[0], ipAdArray[0]);
            int i = 1;
            while (comp == 0 && i <= 3){
                comp = Integer.compare(this.ip[i], ipAdArray[i]);
                i++;
            }
            return comp;
        }
        
    }
}

