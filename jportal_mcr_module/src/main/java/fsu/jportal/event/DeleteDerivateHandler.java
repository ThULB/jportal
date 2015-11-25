package fsu.jportal.event;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
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
    private static Logger LOGGER = LogManager.getLogger(DeleteDerivateHandler.class);

    @Override
    protected void handleDerivateDeleted(MCREvent evt, MCRDerivate der) {
        try {
            DerivateLinkUtil.deleteDerivateLinks(der);
        } catch (SolrServerException sse) {
            LOGGER.error("unable to delete all derivate links");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
