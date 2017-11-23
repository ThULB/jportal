package fsu.jportal.access;

import java.io.IOException;

import fsu.jportal.backend.JPObjectConfiguration;
import fsu.jportal.util.JPComponentUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.mycore.access.MCRAccessManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.search.MCRSolrSearchUtils;

public class DerivateAccess {

    private static final Logger LOGGER = LogManager.getLogger(DerivateAccess.class);

    public static boolean checkPermission(String id) {
        MCRObjectID objectID = MCRObjectID.getInstance(id);
        String journalID = JPComponentUtil.getJournalID(objectID);
        String accessClassName = getAccessClassName(id, journalID);

        if ("klostermann".equals(accessClassName)) {
            try {
                String query = "+id:" + id + " +published:[NOW-1YEAR TO NOW]";
                SolrClient solrClient = MCRSolrClientFactory.getSolrClient();
                SolrDocument solrArticle = MCRSolrSearchUtils.first(solrClient, query);
                if (solrArticle != null) {
                    return checkPerm(id, journalID);
                }
            } catch (SolrServerException | IOException e) {
                LOGGER.error("Error while checking derivate access for " + id, e);
                return false;
            }
            return true;
        }
        return checkPerm(id, journalID);
    }

    private static String getAccessClassName(String id, String journalID) {
        try {
            JPObjectConfiguration journalConfig = new JPObjectConfiguration(journalID, "fsu.jportal.derivate.access");
            return journalConfig.get("accessClass");
        } catch (Exception exc) {
            LOGGER.error("Unable to check permission of " + id + " and journal " + journalID
                    + " because the journal config couldn't be loaded.", exc);
            return null;
        }
    }

    private static boolean checkPerm(String id, String journalID) {
        String permission = "read-derivate";
        MCRAccessManager.invalidPermissionCache(id, permission);
        MCRAccessManager.invalidPermissionCache(journalID, permission);

        if (MCRAccessManager.hasRule(id, permission)) {
            return MCRAccessManager.checkPermission(id, permission);
        }

        if (MCRAccessManager.hasRule(journalID, permission)) {
            return MCRAccessManager.checkPermission(journalID, permission);
        }

        return MCRAccessManager.checkPermission("default", permission);
    }
}
