package fsu.jportal.access;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrDocument;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRException;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.search.MCRSolrSearchUtils;

import fsu.jportal.backend.JPObjectConfiguration;
import fsu.jportal.util.JPComponentUtil;

public class DerivateAccess {

    private static final Logger LOGGER = LogManager.getLogger();

    public static boolean checkPermission(String id) {
        try {
            MCRObjectID objectID = MCRObjectID.getInstance(id);
            String journalID = JPComponentUtil.getJournalID(objectID);
            String accessClassName = getAccessClassName(id, journalID);

            if ("klostermann".equals(accessClassName)) {
                String query = "+id:" + id + " +published:[NOW-1YEAR TO NOW]";
                SolrClient solrClient = MCRSolrClientFactory.getMainSolrClient();
                SolrDocument solrArticle = MCRSolrSearchUtils.first(solrClient, query);
                return solrArticle == null || checkPerm(id, journalID);
            }
            return checkPerm(id, journalID);
        } catch (Exception exc) {
            LOGGER.error("Error while checking derivate access for " + id, exc);
            return false;
        }
    }

    private static String getAccessClassName(String id, String journalID) {
        try {
            JPObjectConfiguration journalConfig = new JPObjectConfiguration(journalID, "fsu.jportal.derivate.access");
            return journalConfig.get("accessClass");
        } catch (Exception exc) {
            throw new MCRException("Unable to check permission of " + id + " and journal " + journalID
                + " because the journal config couldn't be loaded.", exc);
        }
    }

    private static boolean checkPerm(String id, String journalID) {
        try {
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
        } catch (Exception exc) {
            LOGGER.error("Unable to check permission of " + id + " and journal " + journalID, exc);
            return false;
        }
    }

}
