package org.mycore.frontend.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.mycore.access.MCRAccessManager;
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

    /**
     * Bookmarks the derivate and image in the user session.
     * 
     * @param derivateId derivate to bookmark
     * @param image image to bookmark
     */
    public static void bookmarkImage(String derivateId, String image) {
        MCRSession session = MCRSessionMgr.getCurrentSession();
        session.put(IMAGE_BOOKMARK_FILE, image);
        session.put(IMAGE_BOOKMARK_DERIVATE_ID, derivateId);
    }

    /**
     * Gets the bookmarked image as derivate path (derivateID/image_path).
     * 
     * @return
     */
    public static String getBookmarkedImage() {
        MCRSession session = MCRSessionMgr.getCurrentSession();
        String file = (String) session.get(IMAGE_BOOKMARK_FILE);
        String derivateId = (String) session.get(IMAGE_BOOKMARK_DERIVATE_ID);
        if (file == null || derivateId == null)
            return null;
        return new StringBuffer(derivateId).append(file.startsWith("/") ? "" : "/").append(file).toString();
    }

    public static void setLink(MCRObjectID mcrObjId, String pathOfImage) throws MCRActiveLinkException {
        // create derivateLinks
        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(mcrObjId);
        if (!MCRAccessManager.checkPermission(mcrObjId, "writedb")) {
            return;
        }
        MCRMetaElement derLinks = mcrObj.getMetadata().getMetadataElement(DERIVATE_LINKS);
        if (derLinks == null) {
            derLinks = new MCRMetaElement();
            derLinks.setTag(DERIVATE_LINKS);
            derLinks.setClass(MCRMetaDerivateLink.class);
            mcrObj.getMetadata().setMetadataElement(derLinks);
        }

        MCRMetaDerivateLink oldLink = getLink(derLinks, pathOfImage);
        if (oldLink == null) {
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
        if (linkToRemove == null) {
            return;
        }
        // remove link
        derLinks.removeMetaObject(linkToRemove);
        if (derLinks.size() <= 0) {
            mcrObj.getMetadata().removeMetadataElement(DERIVATE_LINKS);
        }
        LOGGER.debug("link in object " + mcrObjId + " removed " + pathOfImage);
        MCRMetadataManager.update(mcrObj);
    }

    public static void removeLinks(MCRObjectID mcrObjId, MCRObjectID derivateId) throws MCRActiveLinkException {
        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(mcrObjId);
        MCRMetaElement derLinks = mcrObj.getMetadata().getMetadataElement(DERIVATE_LINKS);
        List<MCRMetaDerivateLink> linkList = getLinks(derLinks, derivateId.toString());
        for (MCRMetaDerivateLink link : linkList) {
            derLinks.removeMetaObject(link);
        }
        if (derLinks.size() <= 0) {
            mcrObj.getMetadata().removeMetadataElement(DERIVATE_LINKS);
        }
        MCRMetadataManager.update(mcrObj);
    }

    private static MCRMetaDerivateLink getLink(MCRMetaElement derLinks, String pathOfImage) {
        if (derLinks == null) {
            return null;
        }
        Iterator<MCRMetaInterface> it = derLinks.iterator();
        while (it.hasNext()) {
            MCRMetaInterface link = it.next();
            if (link.getSubTag().equals(DERIVATE_LINK) && link instanceof MCRMetaDerivateLink) {
                String href = ((MCRMetaDerivateLink) link).getXLinkHref();
                if (href.equals(pathOfImage)) {
                    return (MCRMetaDerivateLink) link;
                }
            }
        }
        return null;
    }

    private static List<MCRMetaDerivateLink> getLinks(MCRMetaElement derLinks, String derivateId) {
        List<MCRMetaDerivateLink> linkList = new ArrayList<>();
        if (derLinks != null) {
            Iterator<MCRMetaInterface> it = derLinks.iterator();
            while (it.hasNext()) {
                MCRMetaInterface link = it.next();
                if (link.getSubTag().equals(DERIVATE_LINK) && link instanceof MCRMetaDerivateLink) {
                    String href = ((MCRMetaDerivateLink) link).getXLinkHref();
                    if (href.startsWith(derivateId)) {
                        linkList.add((MCRMetaDerivateLink) link);
                    }
                }
            }
        }
        return linkList;
    }

}
