package fsu.jportal.solr;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.mycore.datamodel.metadata.MCRDerivate;

public class DeleteLinkAction implements MCRSolrQueryAction {

    protected MCRDerivate derivate;

    public DeleteLinkAction(MCRDerivate derivate) {
        this.derivate = derivate;
    }

    @Override
    public boolean execute(QueryResponse response) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public SolrParams getParams() {
        ModifiableSolrParams p = new ModifiableSolrParams();
        p.add("q", "derivateLink:" + derivate.getId() + "*");
        return p;
    }

}
