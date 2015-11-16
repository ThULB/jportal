package fsu.jportal.access;

import java.io.IOException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.mycore.access.MCRAccessManager;
import org.mycore.datamodel.common.MCRISO8601Date;
import org.mycore.solr.MCRSolrClientFactory;

import fsu.jportal.pref.JournalConfig;

public class DerivateAccess {

    public static boolean checkPermission(String id, String date){
        JournalConfig journalConfig = new JournalConfig(id, "fsu.jportal.derivate.access");
        String accessClassName = journalConfig.getKey("accessClass");

        String permission = "read-derivate";
        MCRAccessManager.invalidPermissionCache(id, permission);
        if (MCRAccessManager.checkPermission(id, permission)) {
            return true;
        }
        
        if(accessClassName != null && "klostermann".equals(accessClassName)){
            if(date.equals("")){
                return true;
            }
            
            try {
                String sorlQuery = "+journalID:" + id +" +objectType:jparticle +published_sort:[NOW -1YEAR TO NOW]";
                ModifiableSolrParams solrParams = new ModifiableSolrParams(); 
                solrParams.set("q", sorlQuery).set("rows", 1).set("fl", "id");
                QueryResponse response = MCRSolrClientFactory.getSolrClient().query(solrParams);

                SolrDocumentList results = response.getResults();
                for (SolrDocument result : results) {
                    String idFromResultList = (String)result.getFieldValue("id");
                    if(id.equals(idFromResultList)){
                        return false;
                    }
                }
            } catch (SolrServerException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
