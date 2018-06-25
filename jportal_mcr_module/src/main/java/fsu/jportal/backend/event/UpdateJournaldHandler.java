package fsu.jportal.backend.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.common.MCRLinkTableManager;
import org.mycore.datamodel.common.MCRMarkManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.index.MCRSolrIndexer;
import org.mycore.solr.search.MCRSolrSearchUtils;

import java.util.List;
import java.util.stream.Collectors;

import static fsu.jportal.util.ImprintUtil.getImprintID;

public class UpdateJournaldHandler extends MCREventHandlerBase {

    static Logger LOGGER = LogManager.getLogger(UpdateJournaldHandler.class);

    @Override
    protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        MCRObjectID mcrId = obj.getId();
        if (!"jpjournal".equals(mcrId.getTypeId())) {
            return;
        }
        MCRMarkManager mm = MCRMarkManager.instance();
        if (mm.isMarkedForDeletion(mcrId) || mm.isMarkedForImport(mcrId)) {
            return;
        }
        updateImprintPartnerGreeting(mcrId.toString());
        try {
            MCRSessionMgr.getCurrentSession().onCommit(new UpdateDescendantsThread(obj.getId()));
        } catch (Exception exc) {
            LOGGER.error("Unable to reindex descendents of " + mcrId, exc);
        }
    }

    private void updateImprintPartnerGreeting(String mcrIdString) {
        MCRLinkTableManager ltm = MCRLinkTableManager.instance();
        String imprintID = getImprintID(mcrIdString, "imprint");
        if (imprintID != null && !imprintID.trim().isEmpty()) {
            ltm.addReferenceLink(mcrIdString, imprintID, "imprint", null);
        }
        String partnerID = getImprintID(mcrIdString, "partner");
        if (partnerID != null && !partnerID.trim().isEmpty()) {
            ltm.addReferenceLink(mcrIdString, partnerID, "partner", null);
        }
        String greetingID = getImprintID(mcrIdString, "greeting");
        if (greetingID != null && !greetingID.trim().isEmpty()) {
            ltm.addReferenceLink(mcrIdString, greetingID, "greeting", null);
        }
    }

    private static class UpdateDescendantsThread implements Runnable {

        private MCRObjectID journalId;

        public UpdateDescendantsThread(MCRObjectID journalId) {
            this.journalId = journalId;
        }

        @Override
        public void run() {
            try {
                SolrClient client = MCRSolrClientFactory.getMainConcurrentSolrClient();
                List<String> descendants = MCRSolrSearchUtils.listIDs(client, "journalID:" + journalId.toString());
                descendants = descendants.stream()
                                         .filter(id -> !id.equals(journalId.toString()))
                                         .collect(Collectors.toList());
                MCRSolrIndexer.rebuildMetadataIndex(descendants, client);
            } catch (Exception exc) {
                LOGGER.error("Unable to update descedants for " + journalId.toString(), exc);
            }
        }
    }

}
