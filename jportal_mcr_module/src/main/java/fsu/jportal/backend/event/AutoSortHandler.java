package fsu.jportal.backend.event;

import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.metadata.MCRObject;

import fsu.jportal.backend.JPContainer;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.backend.sort.JPSorter;
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

    @Override
    protected void handleObjectCreated(MCREvent evt, MCRObject obj) {
        sort(obj);
    }

    @Override
    protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        sort(obj);
    }

    private void sort(MCRObject obj) {
        if (!JPComponentUtil.isPeriodical(obj.getId())) {
            return;
        }
        JPPeriodicalComponent periodical = JPComponentUtil.getPeriodical(obj);
        if (!(periodical instanceof JPContainer)) {
            return;
        }
        JPContainer container = (JPContainer) periodical;
        container.sort();
    }

}
