package fsu.jportal.http;

import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;

public enum HttpStatus implements StatusType{
    INPUT_ERR(418, "Wrong Formatted Input"), 
    ALLREADY_EXIST(419, "Allready Exist"), 
    RULE_DB_ERR(506, "Database Error/Inconsistency"); 
    
    private int code;
    private String reason;
    private Family family;
    
    HttpStatus (int statusCode, String reasonPhrase){
        this.code = statusCode;
        this.reason = reasonPhrase;
        switch(code/100) {
            case 1: this.family = Family.INFORMATIONAL; break;
            case 2: this.family = Family.SUCCESSFUL; break;
            case 3: this.family = Family.REDIRECTION; break;
            case 4: this.family = Family.CLIENT_ERROR; break;
            case 5: this.family = Family.SERVER_ERROR; break;
            default: this.family = Family.OTHER; break;
        }
    }

    @Override
    public int getStatusCode() {
        return code;
    }

    @Override
    public Family getFamily() {
        return family;
    }

    @Override
    public String getReasonPhrase() {
        return reason;
    }
}