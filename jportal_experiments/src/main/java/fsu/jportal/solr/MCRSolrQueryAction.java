package fsu.jportal.solr;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;

public interface MCRSolrQueryAction {

    public SolrParams getParams();

    public boolean execute(QueryResponse response);

}
