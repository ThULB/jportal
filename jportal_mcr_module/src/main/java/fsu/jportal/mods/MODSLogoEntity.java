package fsu.jportal.mods;

public class MODSLogoEntity {
    private String name;
    private String role;
    private String siteURL;
    private String logoPlainURL;
    private String logoPlusTextURL;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public void setSiteURL(String siteURL) {
        this.siteURL = siteURL;
    }
    public String getSiteURL() {
        return siteURL;
    }
    public void setLogoPlainURL(String logoPlainURL) {
        this.logoPlainURL = logoPlainURL;
    }
    public String getLogoPlainURL() {
        return logoPlainURL;
    }
    public void setLogoPlusTextURL(String logoPlusTextURL) {
        this.logoPlusTextURL = logoPlusTextURL;
    }
    public String getLogoPlusTextURL() {
        return logoPlusTextURL;
    }
    
    public void setLogoUrl(String type, String url){
        if(type.equals("logoPlain")){
            setLogoPlainURL(url);
        }
        
        if(type.equals("logoPlusText")){
            setLogoPlusTextURL(url);
        }
    }
}