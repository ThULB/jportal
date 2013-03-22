package fsu.jportal.jersey.access;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mycore.access.MCRAccessManager;
import org.mycore.frontend.jersey.filter.access.MCRResourceAccessChecker;

import com.sun.jersey.spi.container.ContainerRequest;

import fsu.jportal.resources.JournalConfig;

public class IPRuleAccess implements MCRResourceAccessChecker{

    @Override
    public boolean isPermitted(ContainerRequest request) {
        String path = request.getPath();
        String id = extractId(path);
        JournalConfig journalConfig = new JournalConfig(id);
        String aclObjId = journalConfig.getJournalConfKeys().get("aclObjId");
        String aclPerm = journalConfig.getJournalConfKeys().get("aclPerm");
        
        return MCRAccessManager.checkPermission(aclObjId,aclPerm);
    }

    private String extractId(String path) {
        Pattern idPattern = Pattern.compile("jportal_jpjournal_[0-9]{1,8}");
        Matcher idMathcher = idPattern.matcher(path);
        if(idMathcher.find()) {
            return idMathcher.group();
        }
        
        return null;
    }

}
