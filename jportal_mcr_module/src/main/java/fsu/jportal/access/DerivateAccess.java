package fsu.jportal.access;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.mycore.access.MCRAccessManager;
import org.mycore.solr.MCRSolrClientFactory;

import fsu.jportal.backend.JPObjectConfiguration;

public class DerivateAccess {

    private static final Logger LOGGER = LogManager.getLogger(DerivateAccess.class);

    public static boolean checkPermission(String id, String journalID, String date) {
        String accessClassName = null;
        try {
            JPObjectConfiguration journalConfig = new JPObjectConfiguration(journalID, "fsu.jportal.derivate.access");
            accessClassName = journalConfig.get("accessClass");
        } catch (Exception exc) {
            LOGGER.error("Unable to check permission of " + id + " and journal " + journalID
                + " because the journal config couldn't be loaded.", exc);
        }

        if (accessClassName != null && "klostermann".equals(accessClassName)) {
            if (date.equals("")) {
                return true;
            }

            try {
                String sorlQuery = "+journalID:" + journalID + " +objectType:jparticle +published:[NOW-1YEAR TO NOW]";
                ModifiableSolrParams solrParams = new ModifiableSolrParams();
                solrParams.set("q", sorlQuery).set("fl", "id");
                SolrClient solrClient = MCRSolrClientFactory.getSolrClient();
                QueryResponse response = solrClient.query(solrParams);

                SolrDocumentList results = response.getResults();
                long numFound = results.getNumFound();

                solrParams.set("rows", (int) numFound);
                response = solrClient.query(solrParams);
                results = response.getResults();

                for (SolrDocument result : results) {
                    String idFromResultList = (String) result.getFieldValue("id");
                    if (id.equals(idFromResultList)) {
                        return checkPerm(id, journalID);
                    }
                }
            } catch (SolrServerException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }

            return true;
        }

        return checkPerm(id, journalID);
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
