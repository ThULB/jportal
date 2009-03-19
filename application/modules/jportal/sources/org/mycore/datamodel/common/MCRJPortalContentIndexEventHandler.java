package org.mycore.datamodel.common;

import org.mycore.common.events.MCREvent;
import org.mycore.datamodel.ifs.MCRContentIndexEventHandler;
import org.mycore.datamodel.metadata.MCRDerivate;


public class MCRJPortalContentIndexEventHandler extends MCRContentIndexEventHandler {

    @Override
    protected void handleDerivateUpdated(MCREvent evt, MCRDerivate der) {
        // call repair method of MCRContentIndexEventHandler
        handleDerivateRepaired(evt, der);
    }

}