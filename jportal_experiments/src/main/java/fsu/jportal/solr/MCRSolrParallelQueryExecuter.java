package fsu.jportal.solr;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.solr.client.solrj.response.QueryResponse;

public class MCRSolrParallelQueryExecuter extends MCRSolrQueryExecuterBase {

    protected ExecutorService threadPool;

    public MCRSolrParallelQueryExecuter(MCRSolrQueryAction action) {
        super(action);
    }

    public void execute() throws InterruptedException, ExecutionException {
        threadPool = Executors.newFixedThreadPool(4);
        threadPool.submit(new MCRSolrInitialQueryTask(getAction(), getStart()));
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }

    protected class MCRSolrQueryTask implements Callable<QueryResponse> {

        private MCRSolrQueryAction action;

        private int start;

        public MCRSolrQueryTask(MCRSolrQueryAction action, int start) {
            this.action = action;
            this.start = start;
        }

        @Override
        public QueryResponse call() throws Exception {
            QueryResponse response = getServer().query(getParams(start));
            action.execute(response);
            return response;
        }

    }

    protected class MCRSolrInitialQueryTask extends MCRSolrQueryTask {

        public MCRSolrInitialQueryTask(MCRSolrQueryAction action, int start) {
            super(action, start);
        }

        @Override
        public QueryResponse call() throws Exception {
            QueryResponse response = super.call();
            // 

            // shutdown the thread pool after all task are completed
            getThreadPool().shutdown();
            return response;
        }

    }

}
