package fsu.jportal.backend.sort;

import java.util.Comparator;
import java.util.List;

import org.mycore.common.MCRException;
import org.mycore.datamodel.metadata.MCRObjectID;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPContainer;
import fsu.jportal.backend.JPJournal;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.backend.JPVolume;
import fsu.jportal.backend.mcr.MetadataManager;
import fsu.jportal.util.JPComponentUtil;

/**
 * This is the default {@link JPSorter} implementation for the zs.thulb.uni-jena.de
 * journal server. Based on the given component it chooses the "best" sorting and
 * order algorithm.
 * 
 * @author Matthias Eichner
 */
public class JPMagicSorter implements JPSorter {

    @Override
    public void sort(JPContainer component, Order order) {
        if (JPComponentUtil.is(component, JPJournal.TYPE)) {
            handleJournal((JPJournal) component, order);
        } else if (JPComponentUtil.is(component, JPVolume.TYPE)) {
            handleVolume((JPVolume) component, order);
        } else {
            throw new MCRException("Unsupported component to order " + component.getObject().getId());
        }
    }

    /**
     * A journal persists of volumes. We order them by their published date.
     * If its an online journal the order is descending, if not, the order is
     * ascending.
     * 
     * @param journal the journal to order
     */
    protected void handleJournal(JPJournal journal, Order order) {
        // fallback maintitle sorter
        JPSorter sorter = new JPMaintitleSorter();
        // use hidden position sorter?
        if (canSortedByHiddenPosition(journal)) {
            sorter = new JPHiddenPositionSorter();
        } else if (canSortedByPublishedDate(journal)) {
            // the new way for an online journal
            boolean isOnlineJournal = journal.getJournalTypes().stream().anyMatch(id -> {
                return "jportal_class_00000210".equals(id.getRootID()) && "onlineJournal".equals(id.getID());
            });
            // the old way
            boolean isOldOnlineJournal = journal.getContentClassis(1).stream().anyMatch(id -> {
                return "jportal_class_00000061".equals(id.getRootID()) && "online".equals(id.getID());
            });
            // sort them
            boolean descending = isOnlineJournal || isOldOnlineJournal;
            order = descending ? Order.DESCENDING : Order.ASCENDING;
            sorter = new JPPublishedSorter();
        }
        sorter.sort(journal, order);
    }

    /**
     * Determines if the volume contains articles or volumes.
     * 
     * @param volume the volume to sort
     * @param order the order
     */
    protected void handleVolume(JPVolume volume, Order order) {
        volume.getChildren().stream().findAny().ifPresent(id -> {
            if (JPComponentUtil.is(id, JPArticle.TYPE)) {
                handleArticleChildren(volume, order, id);
            } else if (JPComponentUtil.is(id, JPVolume.TYPE)) {
                handleVolumeChildren(volume, order, id);
            }
        });
    }

    /**
     * Articles are ordered by their size. If they have no size, they are
     * ordered by their title.
     * 
     * @param volume the volume to sort
     * @param order the order
     * @param sampleArticleID one of the article of the volume
     */
    private void handleArticleChildren(JPVolume volume, Order order, MCRObjectID sampleArticleID) {
        JPArticle article = new JPArticle(sampleArticleID);
        JPSorter sorter = article.getSize().isPresent() ? new JPArticleSizeSorter() : new JPMaintitleSorter();
        sorter.sort(volume, order != null ? order : Order.ASCENDING);
    }

    /**
     * Volumes are ordered by their hidden_position. If no hidden position is given
     * they are ordered by their title.
     *
     * @param volume the volume to sort
     * @param order the order
     * @param sampleVolumeID one of the article of the volume
     */
    protected void handleVolumeChildren(JPVolume volume, Order order, MCRObjectID sampleVolumeID) {
        JPVolume child = new JPVolume(sampleVolumeID);
        JPSorter sorter = child.getHiddenPosition() != null ? new JPHiddenPositionSorter() : new JPMaintitleSorter();
        sorter.sort(volume, order != null ? order : Order.ASCENDING);
    }

    /**
     * Checks if the children of the given container can be sorted by
     * their hidden position. This returns true if ALL children have
     * a hidden positions flag.
     * 
     * @param container the container to sort
     * @return true if the children of container can be sorted by their hidden position
     */
    protected boolean canSortedByHiddenPosition(JPContainer container) {
        List<MCRObjectID> children = container.getChildren();
        long count = children.stream().map(MetadataManager::retrieveMCRObject).filter(volume -> {
            return volume.getMetadata().findFirst("hidden_positions").isPresent();
        }).count();
        return children.size() == count;
    }

    /**
     * Checks if the children of the given container can be sorted by
     * their published date. This returns true if ALL children have
     * a published date.
     * 
     * @param container the container to sort
     * @return true if the children of container can be sorted by their published date
     */
    protected boolean canSortedByPublishedDate(JPContainer container) {
        List<MCRObjectID> children = container.getChildren();
        long count = children.stream().map(MetadataManager::retrieveMCRObject).filter(volume -> {
            return volume.getMetadata().findFirst("dates", "published").isPresent();
        }).count();
        return children.size() == count;
    }

    /**
     * Is not required due the delegate call.
     */
    @Override
    public Comparator<? super JPPeriodicalComponent> getSortComparator(Order order) {
        return null;
    }

}
