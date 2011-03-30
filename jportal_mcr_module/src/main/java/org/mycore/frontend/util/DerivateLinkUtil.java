package org.mycore.frontend.util;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRMetaDerivateLink;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaInterface;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

public abstract class DerivateLinkUtil {

    private static Logger LOGGER = Logger.getLogger(DerivateLinkUtil.class);
    
    public static final String IMAGE_BOOKMARK_DERIVATE_ID = "image_bookmark_derivateId";
    public static final String IMAGE_BOOKMARK_FILE = "image_bookmark_file";

    private static final String DERIVATE_LINK = "derivateLink";
    private static final String DERIVATE_LINKS = "derivateLinks";

    public static void bookmarkImage(String derivateId, String file) {
        MCRSession session = MCRSessionMgr.getCurrentSession();
        session.put(IMAGE_BOOKMARK_FILE, file);
        session.put(IMAGE_BOOKMARK_DERIVATE_ID, derivateId);
    }

    public static String getBookmarkedImage() {
        MCRSession session = MCRSessionMgr.getCurrentSession();
        String file = (String)session.get(IMAGE_BOOKMARK_FILE);
        String derivateId = (String)session.get(IMAGE_BOOKMARK_DERIVATE_ID);
        if(file == null || derivateId == null)
            return null;
        return new StringBuffer(derivateId).append("/").append(file).toString();
    }

    public static void setLink(MCRObjectID mcrObjId, String pathOfImage) throws MCRActiveLinkException {
        // create derivateLinks
        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(mcrObjId);
        MCRMetaElement derLinks = mcrObj.getMetadata().getMetadataElement(DERIVATE_LINKS);
        if(derLinks == null) {
            derLinks = new MCRMetaElement();
            derLinks.setTag(DERIVATE_LINKS);
            derLinks.setClass(MCRMetaDerivateLink.class);
            mcrObj.getMetadata().setMetadataElement(derLinks);
        }

        MCRMetaDerivateLink oldLink = getLink(derLinks, pathOfImage);
        if(oldLink == null) {
            // add link
            MCRMetaDerivateLink link = new MCRMetaDerivateLink();
            link.setInherited(0);
            link.setSubTag(DERIVATE_LINK);
            link.setReference(pathOfImage, null, null);
            derLinks.addMetaObject(link);
            MCRMetadataManager.update(mcrObj);
        }
    }

    public static void removeLink(MCRObjectID mcrObjId, String pathOfImage) throws MCRActiveLinkException {
        // get link
        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(mcrObjId);
        MCRMetaElement derLinks = mcrObj.getMetadata().getMetadataElement(DERIVATE_LINKS);
        MCRMetaDerivateLink linkToRemove = getLink(derLinks, pathOfImage);
        if(linkToRemove == null)
            return;

        // remove link
        derLinks.removeMetaObject(linkToRemove);
        LOGGER.debug("link in object " + mcrObjId + " removed " + pathOfImage);
        MCRMetadataManager.update(mcrObj);
    }

    private static MCRMetaDerivateLink getLink(MCRMetaElement derLinks, String pathOfImage) {
        if(derLinks == null)
            return null;
        Iterator<MCRMetaInterface> it = derLinks.iterator();
        while(it.hasNext()) {
            MCRMetaInterface link = it.next();
            if( link.getSubTag().equals(DERIVATE_LINK) &&
                link instanceof MCRMetaDerivateLink)
            {
                String href = ((MCRMetaDerivateLink)link).getXLinkHref();
                if(href.equals(pathOfImage)) {
                    return (MCRMetaDerivateLink)link;
                }
            }
        }
        return null;
    }
}
