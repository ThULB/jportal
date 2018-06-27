package fsu.jportal.backend.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.mycore.common.MCRSessionMgr;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.common.MCRLinkTableManager;
import org.mycore.datamodel.common.MCRMarkManager;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.index.MCRSolrIndexer;

/**
 * Handles solr for jportal on update/create/delete.
 *
 * <ul>
 *     <li>Updates the solr order of the children. This event handler should be called after
 *     the parent object was stored.</li>
 *     <li>reindex all linked objects</li>
 * </ul>
 *
 * @author Matthias Eichner
 */
public class SolrHandler extends MCREventHandlerBase {

    @Override
    protected void handleObjectCreated(MCREvent evt, MCRObject obj) {
        handle(null, obj);
    }

    @Override
    protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        MCRMarkManager mm = MCRMarkManager.instance();
        if (mm.isMarkedForDeletion(obj.getId()) || mm.isMarkedForImport(obj.getId())) {
            return;
        }
        handle((MCRObject) evt.get(MCREvent.OBJECT_OLD_KEY), obj);
    }

    private void handle(MCRObject oldObject, MCRObject object) {
        List<MCRObjectID> reindexList = handleOrder(oldObject, object);
        reindexList.addAll(handleLinkedObjects(object));
        if (reindexList.isEmpty()) {
            return;
        }
        MCRSessionMgr.getCurrentSession().onCommit(new ReindexThread(reindexList));
    }

    private List<MCRObjectID> handleOrder(MCRObject oldObject, MCRObject object) {
        List<MCRMetaLinkID> oldList = oldObject != null ? oldObject.getStructure().getChildren()
            : Collections.emptyList();
        List<MCRMetaLinkID> newList = object.getStructure().getChildren();
        return calcIndexList(newList, oldList);
    }

    private List<MCRObjectID> handleLinkedObjects(MCRObject object) {
        return MCRLinkTableManager.instance().getSourceOf(object.getId(),
            MCRLinkTableManager.ENTRY_TYPE_REFERENCE).stream().map(MCRObjectID::getInstance)
            .collect(Collectors.toList());
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
            if (newIndex > oldIndex) {
                reindexList.add(newId);
                return;
            }
            // same position
            if (newIndex == oldIndex) {
                return;
            }
            // add elements on different positions
            for (oldIndex--; oldIndex >= 0; oldIndex--) {
                MCRObjectID oldId = oldList.get(oldIndex);
                int compareIndex = newList.indexOf(oldId);
                if (compareIndex > newIndex) {
                    reindexList.add(newId);
                    break;
                }
            }
        });
        return reindexList;
    }

    private static class ReindexThread implements Runnable {

        private Collection<MCRObjectID> ids;

        public ReindexThread(Collection<MCRObjectID> ids) {
            this.ids = ids;
        }

        @Override
        public void run() {
            MCRMarkManager mm = MCRMarkManager.instance();
            List<String> reindexList = ids.stream()
                .filter(id -> MCRMetadataManager.exists(id) && !mm.isMarkedForDeletion(id)).map(MCRObjectID::toString)
                .collect(Collectors.toList());
            MCRSolrIndexer.rebuildMetadataIndex(reindexList, MCRSolrClientFactory.getMainConcurrentSolrClient());
        }

    }

}
