package fsu.jportal.util;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.mycore.access.MCRAccessException;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.xml.MCRXMLFunctions;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaDerivateLink;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaInterface;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.search.MCRSolrSearchUtils;

/**
 * Collection of util methods for {@link MCRMetaDerivateLink}.
 *
 * @author Matthias Eichner
 */
public abstract class DerivateLinkUtil {

    private static Logger LOGGER = LogManager.getLogger(DerivateLinkUtil.class);

    private static final String IMAGE_BOOKMARK_DERIVATE_ID = "image_bookmark_derivateId";

    private static final String IMAGE_BOOKMARK_FILE = "image_bookmark_file";

    private static final String DERIVATE_LINK = "derivateLink";

    private static final String DERIVATE_LINKS = "derivateLinks";

    /**
     * Bookmarks the derivate and image in the user session.
     *
     * @param derivateId derivate to bookmark
     * @param imagePath image to bookmark. Be aware that the image path should be URI decoded.
     * @throws URISyntaxException when the imagePath couldn't be URI encoded
     */
    public static void bookmarkImage(String derivateId, String imagePath) throws URISyntaxException {
        MCRSession session = MCRSessionMgr.getCurrentSession();
        session.put(IMAGE_BOOKMARK_FILE, MCRXMLFunctions.encodeURIPath(imagePath));
        session.put(IMAGE_BOOKMARK_DERIVATE_ID, derivateId);
    }

    /**
     * Gets the bookmarked image as derivate path (derivateID/image_path).
     *
     * @return the bookmarked image
     */
    public static String getBookmarkedImage() {
        MCRSession session = MCRSessionMgr.getCurrentSession();
        String file = (String) session.get(IMAGE_BOOKMARK_FILE);
        String derivateId = (String) session.get(IMAGE_BOOKMARK_DERIVATE_ID);
        if (file == null || derivateId == null)
            return null;
        return derivateId + (file.startsWith("/") ? "" : "/") + file;
    }

    /**
     * Returns the linked paths of the given mycore object. The links are
     * in the form of jportal_derivate_xxxxxxx/path_to_linked_file.
     *
     * @param mcrObj mycore object
     * @return list of linked derivate paths or an empty list
     */
    public static List<String> getLinks(MCRObject mcrObj) {
        MCRMetaElement derLinks = mcrObj.getMetadata().getMetadataElement(DERIVATE_LINKS);
        if (derLinks == null) {
            return Collections.emptyList();
        }
        return StreamSupport.stream(derLinks.spliterator(), false)
                            .map(c -> (MCRMetaDerivateLink) c)
                            .map(MCRMetaDerivateLink::getXLinkHref)
                            .collect(Collectors.toList());
    }

    /**
     * Returns a list of all derivate id's which are linked with the given object.
     *
     * @param mcrObj the object
     * @return list of linked derivate id's
     */
    public static List<String> getLinkedDerivates(MCRObject mcrObj) {
        return getLinks(mcrObj).stream()
                               .map(link -> link.substring(0, link.indexOf('/')))
                               .distinct()
                               .collect(Collectors.toList());
    }

    public static void setLinks(List<MCRObjectID> idList, MCRPath pathOfImage) throws MCRAccessException {
        setLinks(idList, pathOfImage.getOwner() + pathOfImage.getOwnerRelativePath());
    }

    public static void setLinks(List<MCRObjectID> idList, String pathOfImage) throws MCRAccessException {
        for (MCRObjectID id : idList) {
            setLink(id, pathOfImage);
        }
    }

    public static void setLink(MCRObjectID mcrObjId, String pathOfImage) throws MCRAccessException {
        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(mcrObjId);
        if (!MCRAccessManager.checkPermission(mcrObjId, "writedb")) {
            return;
        }
        setLink(mcrObj, pathOfImage);
    }

    /**
     * Creates a new derivate link for the given mycore object.
     *
     * @param mcrObj the mycore object
     * @param pathOfImage path of the image including the derivate
     * @throws MCRPersistenceException cannot set link due I/O error
     * @throws MCRAccessException if the write permission is missing
     */
    public static void setLink(MCRObject mcrObj, String pathOfImage)
            throws MCRPersistenceException, MCRAccessException {
        /*
         * This is for debugging purposes only. There is a bug where mets.xml derivate links are randomly
         * added to objects. Check if the path ends with mets.xml and then throw an exception.
         */
        if (pathOfImage.endsWith("mets.xml")) {
            // THIS SHOULD NEVER HAPPEN!
            throw new MCRException(
                    "try to add a mets.xml derivate link to " + mcrObj.getId() + " which is not supposed to happen!");
        }

        // set the link
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
            if (MCRMetadataManager.exists(mcrObj.getId())) {
                MCRMetadataManager.update(mcrObj);
            }
        }
    }

    public static void removeLink(MCRObjectID mcrObjId, String pathOfImage) throws MCRAccessException {
        // get link
        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(mcrObjId);
        MCRMetaElement derLinks = mcrObj.getMetadata().getMetadataElement(DERIVATE_LINKS);
        MCRMetaDerivateLink linkToRemove = getLink(derLinks, pathOfImage);
        if (linkToRemove == null) {
            LOGGER.warn("Couldn't remove link of " + mcrObjId + " with image " + pathOfImage
                    + ". The link couldn't be found.", new NullPointerException());
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

    public static void removeLinks(MCRObjectID mcrObjId, MCRObjectID derivateId) throws MCRAccessException {
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

    /**
     * Deletes all corresponding derivate links of a derivate. Be aware that this 
     * method uses solr! It cannot be guaranteed that all links are found!
     *
     * @param der where to delete the derivate links
     */
    public static void deleteDerivateLinks(MCRDerivate der) {
        MCRSolrSearchUtils.listIDs(MCRSolrClientFactory.getMainSolrClient(), "derivateLink:" + der.getId() + "*")
                          .stream()
                          .map(MCRObjectID::getInstance)
                          .forEach(id -> {
                              try {
                                  DerivateLinkUtil.removeLinks(id, der.getId());
                              } catch (Exception exc) {
                                  LOGGER.error("unable to delete derivate link of object " + id + " and derivate " + der
                                          .getId(), exc);
                              }
                          });
    }

    public static void deleteFileLinks(List<MCRObjectID> idList, MCRPath pathOfImg) throws MCRAccessException {
        for (MCRObjectID id : idList) {
            removeLink(id, pathOfImg.getOwner() + pathOfImg.getOwnerRelativePath());
        }
    }

    public static void deleteFileLink(MCRPath pathOfImg) throws MCRAccessException {
        deleteFileLink(pathOfImg.getOwner() + pathOfImg.getOwnerRelativePath());
    }

    public static void deleteFileLink(String pathOfImg) throws MCRAccessException {
        List<MCRObjectID> idList = getLinks(pathOfImg);
        for (MCRObjectID id : idList) {
            try {
                DerivateLinkUtil.removeLink(id, pathOfImg);
            } catch (MCRException exc) {
                LOGGER.error("unable to delete derivate link of object " + id + " and file " + pathOfImg, exc);
            }
        }
    }

    public static List<MCRObjectID> getLinks(MCRPath pathOfImg) {
        return getLinks(pathOfImg.getOwner() + pathOfImg.getOwnerRelativePath());
    }

    public static List<MCRObjectID> getLinks(String pathOfImg) {
        SolrClient solrClient = MCRSolrClientFactory.getMainSolrClient();
        ModifiableSolrParams params = new ModifiableSolrParams();
        params.add("q", "derivateLink:\"" + pathOfImg + "\"");
        params.set("rows", 100);
        params.set("fl", "id");
        return MCRSolrSearchUtils.stream(solrClient, params)
                                 .map(doc -> doc.getFieldValue("id").toString())
                                 .filter(id -> JPComponentUtil.getValidID(id).isPresent())
                                 .map(MCRObjectID::getInstance)
                                 .collect(Collectors.toList());
    }

    private static MCRMetaDerivateLink getLink(MCRMetaElement derLinks, String pathOfImage) {
        if (derLinks == null) {
            return null;
        }
        for (MCRMetaInterface link : derLinks) {
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
            for (MCRMetaInterface link : derLinks) {
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
