package fsu.jportal.access;

import java.util.Calendar;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.mycore.datamodel.common.MCRISO8601Date;
import org.mycore.solr.MCRSolrServerFactory;

import fsu.jportal.pref.JournalConfig;

public class DerivateAccess {
    public static boolean checkPermission(String id, String date){
        JournalConfig journalConfig = new JournalConfig(id, "fsu.jportal.derivate.access");
        String accessClassName = journalConfig.getKey("accessClass");
        
        if(accessClassName != null && "klostermann".equals(accessClassName) && !date.equals("")){
            try {
                MCRISO8601Date objDate = new MCRISO8601Date(date);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(objDate.getDate());
                int year = calendar.get(Calendar.YEAR) + 1;
                
                String sorlQuery = "+journalID:" + id +" +objectType:jparticle +published_sort:[" + year + "-01-01T00:00:00.000Z TO "+year+"-12-31T23:59:59.999Z]";
                ModifiableSolrParams solrParams = new ModifiableSolrParams(); 
                solrParams.set("q", sorlQuery).set("rows", 1);
                QueryResponse response = MCRSolrServerFactory.getSolrServer().query(solrParams);
                
                if(response.getResults().isEmpty()){
                    return false;
                }
                
            } catch (SolrServerException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return true;
    }
}
