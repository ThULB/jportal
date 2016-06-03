package fsu.jportal.backend.event;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.metadata.MCRObject;

import fsu.jportal.backend.JPContainer;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.backend.sort.JPSorter;
import fsu.jportal.backend.sort.JPSorter.Order;
import fsu.jportal.util.JPComponentUtil;

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
                addDefaultSorter(container);
            }
            container.sort();
        });
    }

    @Override
    protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        getContainer(obj).ifPresent(container -> {
            container.sort();
        });
    }

    /**
     * Returns an optional container if the given object is an instance of
     * jpvolume or jpjournal 
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
     * Adds a default sorter for the given container.
     * 
     * @param container the object to add the default sorder
     */
    private void addDefaultSorter(JPContainer container) {
        try {
            MCRConfiguration config = MCRConfiguration.instance();
            String defaultSorterClassString = config.getString("JP.Metadata.AutoSort.defaultClass", null);
            if (defaultSorterClassString == null) {
                return;
            }
            String defaultSorterOrder = config.getString("JP.Metadata.AutoSort.defaultOrder", Order.ASCENDING.name());
            Class<? extends JPSorter> sorterClass = Class.forName(defaultSorterClassString).asSubclass(JPSorter.class);
            Order order = Order.valueOf(defaultSorterOrder);
            container.setSortBy(sorterClass, order);
        } catch (Exception exc) {
            LOGGER.error("While setting the default sorter for " + container.getObject().getId(), exc);
        }
    }

}
