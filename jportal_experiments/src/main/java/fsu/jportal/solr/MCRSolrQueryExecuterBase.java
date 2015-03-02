package fsu.jportal.solr;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.mycore.solr.MCRSolrClientFactory;

public abstract class MCRSolrQueryExecuterBase {

    protected MCRSolrQueryAction action;

    protected int rows;

    protected int start;

    public MCRSolrQueryExecuterBase(MCRSolrQueryAction action) {
        this.action = action;
        Integer rows = this.action.getParams().getInt("rows");
        this.rows = rows != null ? rows : 10;
        Integer start = this.action.getParams().getInt("start");
        this.start = start != null ? start : 0;
    }

    public MCRSolrQueryAction getAction() {
        return action;
    }

    public int getRows() {
        return rows;
    }

    public int getStart() {
        return start;
    }

    public SolrClient getClient() {
        return MCRSolrClientFactory.getSolrClient();
    }

    public SolrParams getParams(int start) {
        ModifiableSolrParams params = new ModifiableSolrParams(this.action.getParams());
        params.set("start", start);
        params.set("rows", getRows());
        return params;
    }

}
