package fsu.jportal.jersey.access;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessManager;
import org.mycore.frontend.jersey.filter.access.MCRResourceAccessChecker;

import com.sun.jersey.spi.container.ContainerRequest;

import fsu.jportal.backend.JPObjectConfiguration;

public class IPRuleAccess implements MCRResourceAccessChecker {

    private static final Logger LOGGER = LogManager.getLogger(IPRuleAccess.class);

    @Override
    public boolean isPermitted(ContainerRequest request) {
        String path = request.getPath();
        String id = extractId(path);
        try {
            JPObjectConfiguration journalConfig = new JPObjectConfiguration(id, "jportal_acl_ip_editor_module");
            String aclObjId = journalConfig.get("aclObjId");
            String aclPerm = journalConfig.get("aclPerm");
            if (aclObjId == null || aclPerm == null) {
                return false;
            }
            return MCRAccessManager.checkPermission(aclObjId, aclPerm);
        } catch (Exception exc) {
            LOGGER.error("Unable to get permission due journal config loading failed for " + id, exc);
            return false;
        }
    }

    private String extractId(String path) {
        Pattern idPattern = Pattern.compile("jportal_jpjournal_[0-9]{1,8}");
        Matcher idMathcher = idPattern.matcher(path);
        if (idMathcher.find()) {
            return idMathcher.group();
        }
        return null;
    }

}
