/**
 * 
 */
package org.mycore.backend.ifs;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaInterface;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectMetadata;

/**
 * @author Matthias Eichner
 * 
 */
public class MCRJPortalLink {

    private static Logger LOGGER = Logger.getLogger(MCRJPortalLink.class);

    private MCRObjectID from;

    private String to;

    private static final String IFS_LINK = "ifsLink";
    private static final String IFS_LINKS = "ifsLinks";

    /**
     * @param from,
     *            ID of Mycore-Object in which the link should be added.
     * @param to,
     *            Absolute path of a MCRFile, where the link should point to.
     */
    public MCRJPortalLink(MCRObjectID from, String to) {
        this.from = from;
        this.to = to;
    }

    /**
     * Removes a link from a Mycore-Object
     * 
     * @throws MCRActiveLinkException
     */
    public void remove() throws MCRActiveLinkException {
        // get link
        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(this.from);

        MCRMetaElement ifsLinks = mcrObj.getMetadata().getMetadataElement(IFS_LINKS);
        MCRMetaLangText linkToRemove = getLink(ifsLinks, this.to);
        if(linkToRemove == null)
            return;

        ifsLinks.removeMetaObject(linkToRemove);
        LOGGER.debug("link in object " + from + " removed " + this.to);
        MCRMetadataManager.update(mcrObj);
    }

    /**
     * Creates a link in a Mycore-Object
     * 
     * @throws MCRActiveLinkException
     */
    public void set() throws MCRActiveLinkException {
        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(this.from);
        MCRObjectMetadata metadata = mcrObj.getMetadata();
        // create ifsLinks if not exist
        MCRMetaElement ifsLinks = metadata.getMetadataElement(IFS_LINKS);
        if(ifsLinks == null) {
            ifsLinks = new MCRMetaElement();
            ifsLinks.setTag(IFS_LINKS);
            ifsLinks.setClass(MCRMetaLangText.class);
            metadata.setMetadataElement(ifsLinks);
        }

        MCRMetaLangText oldLink = getLink(ifsLinks, this.to);
        if(oldLink == null) {
            // link doesn't exist -> create it
            MCRMetaLangText ifsLink = new MCRMetaLangText();
            ifsLink.setSubTag(IFS_LINK);
            ifsLink.setText(this.to);
            ifsLinks.addMetaObject(ifsLink);
            // store in ifs
            MCRMetadataManager.update(mcrObj);
            LOGGER.debug("link in object " + from + " set to " + this.to);
        }
    }

    private MCRMetaLangText getLink(MCRMetaElement ifsLinks, String linkText) {
        if(ifsLinks == null)
            return null;
        Iterator<MCRMetaInterface> it = ifsLinks.iterator();
        while(it.hasNext()) {
            MCRMetaInterface link = it.next();
            if( link.getSubTag().equals(IFS_LINK) &&
                link instanceof MCRMetaLangText)
            {
                String text = ((MCRMetaLangText)link).getText();
                if(text.equals(linkText)) {
                    return (MCRMetaLangText)link;
                }
            }
        }
        return null;
    }
}
