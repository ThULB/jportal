package fsu.jportal.backend.event;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.common.MCRMarkManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

import fsu.jportal.backend.JPContainer;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.backend.mcr.JPConfig;
import fsu.jportal.backend.sort.JPLevelSorting;
import fsu.jportal.backend.sort.JPLevelSorting.Level;
import fsu.jportal.backend.sort.JPSorter;
import fsu.jportal.backend.sort.JPSorter.Order;
import fsu.jportal.util.JPComponentUtil;
import fsu.jportal.util.JPLevelSortingUtil;

/**
 * Each journal or volume can have a {@link JPSorter} which enables
 * automatic ordering of their children.
 * 
 * @see JPContainer#sort()
 * 
 * @author Matthias Eichner
 */
public class AutoSortHandler extends MCREventHandlerBase {

    private static Logger LOGGER = LogManager.getLogger(AutoSortHandler.class);

    @Override
    protected void handleObjectCreated(MCREvent evt, MCRObject obj) {
        getContainer(obj).ifPresent(container -> {
            if (!container.getSortBy().isPresent()) {
                if (!addLevelSortingSorter(container)) {
                    addDefaultSorter(container);
                }
            }
            try {
                container.sort();
            } catch(Exception exc) {
                LOGGER.error("Unable to auto sort object '" + obj.getId() + "'.", exc);
            }
        });
    }

    @Override
    protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        if (MCRMarkManager.instance().isMarkedForDeletion(obj.getId())) {
            return;
        }
        try {
            getContainer(obj).ifPresent(JPContainer::sort);
        } catch(Exception exc) {
            LOGGER.error("Unable to auto sort object '" + obj.getId() + "'.", exc);
        }
    }

    /**
     * Returns an optional container if the given object is an instance of
     * jpvolume or jpjournal.
     * 
     * @param obj the object
     * @return optional container
     */
    private Optional<JPContainer> getContainer(MCRObject obj) {
        if (!JPComponentUtil.isPeriodical(obj.getId())) {
            return Optional.empty();
        }
        JPPeriodicalComponent periodical = JPComponentUtil.getPeriodical(obj);
        if (!(periodical instanceof JPContainer)) {
            return Optional.empty();
        }
        return Optional.of((JPContainer) periodical);
    }

    /**
     * Adds the level sorting sorter defined by the journal.
     * Returns true if the level sorting was applied, otherwise false.
     * 
     * @param container the container
     * @return true if the level sorting was applied
     */
    private boolean addLevelSortingSorter(JPContainer container) {
        try {
            Optional<MCRObjectID> optionalJournalId = container.getJournalId();
            if (optionalJournalId.isPresent()) {
                JPLevelSorting levelSorting = JPLevelSortingUtil.load(optionalJournalId.get());
                if (levelSorting.isEmpty()) {
                    return false;
                }
                int pos = JPLevelSortingUtil.getLevelOfObject(container.getObject());
                Level level = levelSorting.get(pos);
                if (level == null) {
                    return false;
                }
                container.setSortBy(level.getSorterClass(), level.getOrder());
                return true;
            }
        } catch (Exception exc) {
            LOGGER.error("Unable to add level sorting to object " + container.getObject().getId(), exc);
        }
        return false;
    }

    /**
     * Adds a default sorter for the given container.
     * 
     * @param container the object to add the default sorder
     */
    private void addDefaultSorter(JPContainer container) {
        try {
            String defaultSorterClassString = JPConfig
                    .getString("JP.Metadata.AutoSort.defaultClass", null);
            if (defaultSorterClassString == null) {
                return;
            }
            String defaultSorterOrder = JPConfig
                    .getString("JP.Metadata.AutoSort.defaultOrder", Order.ASCENDING.name());
            Class<? extends JPSorter> sorterClass = Class.forName(defaultSorterClassString).asSubclass(JPSorter.class);
            Order order = Order.valueOf(defaultSorterOrder);
            container.setSortBy(sorterClass, order);
        } catch (Exception exc) {
            LOGGER.error("While setting the default sorter for " + container.getObject().getId(), exc);
        }
    }

}
