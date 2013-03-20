package fsu.jportal.resources;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.access.mcrimpl.MCRAccessControlSystem;
import org.mycore.access.mcrimpl.MCRAccessRule;
import org.mycore.access.mcrimpl.MCRRuleStore;
import org.mycore.common.MCRCache;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.xml.MCRLayoutService;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import fsu.jportal.resolver.JournalFilesResolver;

@Path("IPRule/{objID}")
public class IPRuleResource {

    static Logger LOGGER = Logger.getLogger(IPRuleResource.class);
    
    static MCRCache<String, MCRAccessRule> accessCache;

    static {
        MCRAccessControlSystem.instance();
        accessCache = MCRAccessControlSystem.getCache();
    }
    
    @Context HttpServletRequest request;
    @Context HttpServletResponse response;
    @PathParam("objID") String objID;
    
    @GET
    @Path("start")
    public void start() throws IOException, JDOMException{
        InputStream guiXML = getClass().getResourceAsStream("/jportal_acl_ip_editor_module/gui/xml/webpage.xml");
        SAXBuilder saxBuilder = new SAXBuilder();
        Document webPage = saxBuilder.build(guiXML);
        XPathExpression<Object> xpath = XPathFactory.instance().compile("/MyCoReWebPage/section/div[@id='jportal_acl_ip_editor_module']");
        Object node = xpath.evaluateFirst(webPage);
        if(node != null){
            Element mainDiv = (Element) node;
            mainDiv.setAttribute("objID", objID);
        }
        MCRLayoutService.instance().doLayout(request, response, new MCRJDOMContent(webPage));
    }

    @GET
    public String list() throws TransformerException, JDOMException, IOException {
        String ruleid = getJournalConfKeys().get("ruleId");
        //get the ruleString
        MCRRuleStore ruleStore = MCRRuleStore.getInstance();
        MCRAccessRule accessRule = ruleStore.getRule(ruleid);
        String ruleString = accessRule.getRuleString();
        
        JsonArray jsonA = new JsonArray();
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
                JsonObject jsonO = new JsonObject();
                jsonO.addProperty("ip", ip.getIpWithMask());
                jsonA.add(jsonO);
            }
        }
        return jsonA.toString();
    }

    private HashMap<String,String> getJournalConfKeys(){
        Document journalConfigXML;
        try {
            journalConfigXML = createXML(getJournalConf(objID));
            return extractConfKeys(journalConfigXML);
        } catch (JDOMException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }

    private HashMap<String, String> extractConfKeys(Document journalConfigXML) {
        XPathExpression<Object> xpath = XPathFactory.instance().compile("/journalConf/conf[@id='jportal_acl_ip_editor_module']/key");
        HashMap<String, String> confKeys = new HashMap<String, String>();
        
        List<Object> nodes = xpath.evaluate(journalConfigXML);
        
        for (Object node : nodes) {
            if(node instanceof Element){
                Element key = (Element) node;
                String name = key.getAttributeValue("name");
                String value = key.getAttributeValue("value");
                confKeys.put(name, value);
            }
        }
        return confKeys;
    }

    private Document createXML(FileInputStream fileIS) throws JDOMException, IOException, FileNotFoundException {
        SAXBuilder saxBuilder = new SAXBuilder();
        Document journalConfigXML = saxBuilder.build(fileIS);
        return journalConfigXML;
    }

    private FileInputStream getJournalConf(String objid) throws FileNotFoundException {
        JournalFilesResolver journalFilesResolver = new JournalFilesResolver();
        FileInputStream journalConfig = journalFilesResolver.getJournalFile(objid+"/config.xml");
        return journalConfig;
    }

    @POST
    public Response add(String ip) {
        HashMap<String, String> journalConfKeys = getJournalConfKeys();
        String ruleId = journalConfKeys.get("ruleId");
        String defRule = journalConfKeys.get("defRule");
        if (addIp(ruleId, ip, defRule)){
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
    public String removeList(JsonObject json) {
        JsonArray ipsAsJSONArray = json.getAsJsonArray("ips");
        for(int i = 0; i < ipsAsJSONArray.size(); i++){
            String ip = ipsAsJSONArray.get(i).getAsJsonObject().getAsJsonPrimitive("ip").getAsString();
            if(removeIp(json.getAsJsonPrimitive("ruleid").getAsString(), ip)){
                ipsAsJSONArray.get(i).getAsJsonObject().addProperty("success", "1");
            }
            else{
                ipsAsJSONArray.get(i).getAsJsonObject().addProperty("success", "0");
            }
        }
        return ipsAsJSONArray.toString();
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

