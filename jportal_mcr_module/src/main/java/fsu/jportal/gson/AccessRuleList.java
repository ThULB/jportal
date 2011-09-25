package fsu.jportal.gson;

import java.net.URI;
import java.util.List;

import org.mycore.backend.hibernate.tables.MCRACCESSRULE;

public class AccessRuleList {
    private List<MCRACCESSRULE> ruleList;
    private URI uri;
    
    public AccessRuleList(List<MCRACCESSRULE> ruleList, URI uri) {
        this.setRuleList(ruleList);
        this.setUri(uri);
    }

    private void setRuleList(List<MCRACCESSRULE> ruleList) {
        this.ruleList = ruleList;
    }

    public List<MCRACCESSRULE> getRuleList() {
        return ruleList;
    }

    private void setUri(URI uri) {
        this.uri = uri;
    }

    public URI getUri() {
        return uri;
    }

}