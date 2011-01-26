package org.mycore.frontend.util;

import org.jdom.JDOMException;
import org.mycore.backend.ifs.MCRJPortalLink;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.metadata.MCRObjectID;

public abstract class DerivateLinkUtil {

    public static final String IMAGE_BOOKMARK_DERIVATE_ID = "image_bookmark_derivateId";
    public static final String IMAGE_BOOKMARK_FILE = "image_bookmark_file";

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

    public static void setLink(MCRObjectID mcrObjId, String pathOfImage) {
        MCRJPortalLink link = new MCRJPortalLink(mcrObjId, pathOfImage);
        link.set();
    }

    public static void removeLink(MCRObjectID mcrObjId, String pathOfImage) throws JDOMException {
        MCRJPortalLink link = new MCRJPortalLink(mcrObjId, pathOfImage);
        link.remove();
    }
}
