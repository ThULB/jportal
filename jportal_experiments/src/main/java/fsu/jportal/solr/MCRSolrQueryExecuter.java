package fsu.jportal.solr;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;

public class MCRSolrQueryExecuter extends MCRSolrQueryExecuterBase {

    public MCRSolrQueryExecuter(MCRSolrQueryAction action) {
        super(action);
    }

    public void execute() throws SolrServerException {
        SolrClient solrServer = getClient();
        boolean execute = true;
        long numFound = Long.MAX_VALUE;
        int position = start;
        while (execute && position < numFound) {
            QueryResponse response = solrServer.query(getParams(position));
            execute = getAction().execute(response);
            numFound = response.getResults().getNumFound();
            position += getRows();
        }
    }

}
