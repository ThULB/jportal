package fsu.jportal.backend.event;

import org.mycore.common.MCRSessionMgr;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.common.MCRMarkManager;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.MCRSolrUtils;
import org.mycore.solr.index.MCRSolrIndexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
        List<MCRMetaLinkID> oldList =
            oldObject != null ? oldObject.getStructure().getChildren() : Collections.emptyList();
        List<MCRMetaLinkID> newList = object.getStructure().getChildren();
        List<MCRObjectID> reindexList = calcIndexList(newList, oldList);
        if (!reindexList.isEmpty()) {
            MCRSessionMgr.getCurrentSession().onCommit(new ReindexThread(reindexList));
        }
    }

    protected List<MCRObjectID> calcIndexList(List<MCRMetaLinkID> newLinkList, List<MCRMetaLinkID> oldLinkList) {
        List<MCRObjectID> reindexList = new ArrayList<>();
        List<MCRObjectID> newList = newLinkList.stream().map(MCRMetaLinkID::getXLinkHrefID)
            .collect(Collectors.toList());
        List<MCRObjectID> oldList = oldLinkList.stream().map(MCRMetaLinkID::getXLinkHrefID)
            .collect(Collectors.toList());
        newList.forEach(newId -> {
            int newIndex = newList.indexOf(newId);
            int oldIndex = oldList.indexOf(newId);
            // add new elements
            if(newIndex > oldIndex) {
                reindexList.add(newId);
                return;
            }
            // same position
            if(newIndex == oldIndex) {
                return;
            }
            // add elements on different positions
            for (oldIndex--; oldIndex >= 0; oldIndex--) {
                MCRObjectID oldId = oldList.get(oldIndex);
                int compareIndex = newList.indexOf(oldId);
                if(compareIndex > newIndex) {
                    reindexList.add(newId);
                    break;
                }
            }
        });
        return reindexList;
    }

    private static class ReindexThread implements Runnable {

        private List<MCRObjectID> ids;

        public ReindexThread(List<MCRObjectID> ids) {
            this.ids = ids;
        }

        @Override
        public void run() {
            MCRMarkManager mm = MCRMarkManager.instance();
            List<String> reindexList = ids.stream().filter(id -> {
                return MCRMetadataManager.exists(id) && !mm.isMarkedForDeletion(id);
            }).map(MCRObjectID::toString).collect(Collectors.toList());
            MCRSolrIndexer.rebuildMetadataIndex(reindexList, MCRSolrClientFactory.getMainConcurrentSolrClient());
        }

    }

}
