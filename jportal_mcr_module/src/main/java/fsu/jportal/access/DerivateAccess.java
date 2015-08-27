package fsu.jportal.access;

import java.io.IOException;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.util.Calendar;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
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

                MCRISO8601Date objDate = new MCRISO8601Date(date);
                TemporalAccessor dt = objDate.getDt();
                int year = dt.get(ChronoField.YEAR) + 1;
                int month = dt.get(ChronoField.MONTH_OF_YEAR) + 1;
                
                String sorlQuery = "+journalID:" + id +" +objectType:jparticle +published_sort:[" + year + "-"+month+"-01T00:00:00.000Z TO "+year+"-"+month+"-31T23:59:59.999Z]";
                ModifiableSolrParams solrParams = new ModifiableSolrParams(); 
                solrParams.set("q", sorlQuery).set("rows", 1);
                QueryResponse response = MCRSolrClientFactory.getSolrClient().query(solrParams);
                
                if(!response.getResults().isEmpty()){
                    return true;
                }
                
            } catch (SolrServerException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
