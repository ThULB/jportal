package fsu.jportal.backend.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.metadata.MCRDerivate;

import fsu.jportal.util.DerivateLinkUtil;

/**
 * This event handler tries to delete all corresponding derivate links
 * when a derivate is deleted. It is assumed that solr could deliver
 * all links with a simple query. It is not assured that all links could
 * be found and deleted, but better then nothing though.
 *
 * @author Matthias Eichner
 */
public class DeleteDerivateHandler extends MCREventHandlerBase {

    private static Logger LOGGER = LogManager.getLogger();

    @Override
    protected void handleDerivateDeleted(MCREvent evt, MCRDerivate der) {
        MCRSessionMgr.getCurrentSession().onCommit(() -> {
            try {
                DerivateLinkUtil.deleteDerivateLinks(der);
            } catch (Exception exc) {
                LOGGER.error("unable to delete all derivate links", exc);
            }
        });
    }

}
