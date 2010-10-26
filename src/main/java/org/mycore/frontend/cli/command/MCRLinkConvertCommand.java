package org.mycore.frontend.cli.command;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.mycore.datamodel.ifs.MCRFile;
import org.mycore.datamodel.metadata.MCRMetaDerivateLink;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaInterface;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.frontend.cli.MCRJPortalCommands;
import org.mycore.parsers.bool.MCROrCondition;
import org.mycore.services.fieldquery.MCRFieldDef;
import org.mycore.services.fieldquery.MCRHit;
import org.mycore.services.fieldquery.MCRQuery;
import org.mycore.services.fieldquery.MCRQueryCondition;
import org.mycore.services.fieldquery.MCRQueryManager;
import org.mycore.services.fieldquery.MCRResults;

public class MCRLinkConvertCommand {

    private static Logger LOGGER = Logger.getLogger(MCRLinkConvertCommand.class.getName());
    
    public static List<String> convert() throws Exception {
        // build search condition
        MCROrCondition orCond = new MCROrCondition();
        MCRQueryCondition jpArticleCond = new MCRQueryCondition(MCRFieldDef.getDef("objectType"), "=", "jparticle");
        MCRQueryCondition jpVolumeCond = new MCRQueryCondition(MCRFieldDef.getDef("objectType"), "=", "jpvolume");
        orCond.addChild(jpArticleCond);
        orCond.addChild(jpVolumeCond);

        // search
        MCRQuery query = new MCRQuery(orCond);
        MCRResults results = MCRQueryManager.search(query);

        List<String> commandList =new ArrayList<String>();

        int count = 0;
        // go through all articles and volumes
        for(MCRHit hit : results) {
            // get the mcr object
            commandList.add("internal replace ifs link " + hit.getID());
            if(count % 10000 == 0)
                LOGGER.info(count + " elements checked");
            count++;
        }
        return commandList;
    }

    public static void replaceLink(String mcrObjId) throws Exception {
        MCRObject mcrObj = new MCRObject();
        mcrObj.receiveFromDatastore(mcrObjId);
        
        // remove old ifs link
        MCRMetaElement oldIFSLinks = mcrObj.getMetadata().removeMetadataElement("ifsLinks");
        if (oldIFSLinks != null) {
            // add new derivateLink
            MCRMetaElement derivateLinksElement = new MCRMetaElement();
            derivateLinksElement.setTag("derivateLinks");
            derivateLinksElement.setClassName("MCRMetaDerivateLink");
            derivateLinksElement.setHeritable(false);
            derivateLinksElement.setNotInherit(true);

            MCRMetaDerivateLink derivateLink = new MCRMetaDerivateLink();
            derivateLink.setInherited(0);
            derivateLink.setSubTag("derivateLink");
            String href = ((MCRMetaLangText) oldIFSLinks.getElement(0)).getText();
            derivateLink.setReference(href, null, null);

            derivateLinksElement.addMetaObject(derivateLink);
            mcrObj.getMetadata().setMetadataElement(derivateLinksElement, "derivateLinks");

            mcrObj.updateInDatastore();

            LOGGER.info("ifs linked replaced for object " + mcrObj.getId());
        } else {
            LOGGER.info("object " + mcrObj.getId() + " has no links.");
        }
    }
}
