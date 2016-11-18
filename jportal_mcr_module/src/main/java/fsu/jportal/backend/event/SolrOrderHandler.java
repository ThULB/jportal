package fsu.jportal.backend.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mycore.common.MCRSessionMgr;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.common.MCRMarkManager;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.solr.index.MCRSolrIndexer;

/**
 * Updates the solr order of the children. This event handler should be called after
 * the parent object was stored.
 * 
 * @author Matthias Eichner
 */
public class SolrOrderHandler extends MCREventHandlerBase {

    @Override
    protected void handleObjectCreated(MCREvent evt, MCRObject obj) {
        handleOrder(null, obj);
    }

    @Override
    protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        MCRObject oldObject = (MCRObject) evt.get(MCREvent.OBJECT_OLD_KEY);
        MCRMarkManager mm = MCRMarkManager.instance();
        if (mm.isMarkedForDeletion(obj.getId()) || mm.isMarkedForImport(obj.getId())) {
            return;
        }
        handleOrder(oldObject, obj);
    }

    private void handleOrder(MCRObject oldObject, MCRObject object) {
        List<MCRMetaLinkID> oldList = oldObject != null ? oldObject.getStructure().getChildren()
            : Collections.emptyList();
        List<MCRMetaLinkID> newList = object.getStructure().getChildren();
        List<String> reindexList = new ArrayList<>();

        // build a list of children which have a new position
        for (int index = 0; index < newList.size(); index++) {
            MCRMetaLinkID link = newList.get(index);
            MCRObjectID id = link.getXLinkHrefID();
            if (index < oldList.size()) {
                MCRObjectID oldId = oldList.get(index).getXLinkHrefID();
                if (!id.equals(oldId)) {
                    reindexList.add(id.toString());
                }
            } else {
                reindexList.add(id.toString());
            }
        }
        if (!reindexList.isEmpty()) {
            MCRSessionMgr.getCurrentSession().onCommit(new ReindexThread(reindexList));
        }
    }

    private static class ReindexThread implements Runnable {

        private List<String> ids;

        public ReindexThread(List<String> ids) {
            this.ids = ids;
        }

        @Override
        public void run() {
            MCRSolrIndexer.rebuildMetadataIndex(ids);
        }
    }

}
