package fsu.jportal.frontend.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaDerivateLink;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaInterface;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.frontend.jersey.MCRJerseyUtil;
import org.mycore.solr.MCRSolrClientFactory;

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
    
    public static void setLinks(List<MCRObjectID> idList, MCRPath pathOfImage) {
        setLinks(idList, pathOfImage.getOwner() + pathOfImage.getOwnerRelativePath());
    }
    
    public static void setLinks(List<MCRObjectID> idList, String pathOfImage) {
        for (MCRObjectID id : idList) {
            try {
                setLink(id, pathOfImage);
            } catch (MCRActiveLinkException exc) {
                LOGGER.error("unable to set derivate link of object " + id + " and file " + pathOfImage, exc);
            }
        }
    }

    public static void setLink(MCRObjectID mcrObjId, String pathOfImage) throws MCRActiveLinkException {
        // create derivateLinks
        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(mcrObjId);
        if (!MCRAccessManager.checkPermission(mcrObjId, "writedb")) {
            return;
        }
        setLink(mcrObj, pathOfImage);
    }

    public static void setLink(MCRObject mcrObj, String pathOfImage) throws MCRActiveLinkException {
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
            if(MCRMetadataManager.exists(mcrObj.getId())) {
                MCRMetadataManager.update(mcrObj);
            }
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

    /**
     * Deletes all corresponding derivate links of a derivate. Be aware that this 
     * method uses solr! It cannot be guaranteed that all links are found!
     * 
     * @param der 
     * @throws SolrServerException
     */
    public static void deleteDerivateLinks(MCRDerivate der) throws SolrServerException, IOException {
        MCRObjectID derivateId = der.getId();
        List<MCRObjectID> idList = getLinks(derivateId + "*");
        for (MCRObjectID id : idList) {
            try {
                DerivateLinkUtil.removeLinks(id, derivateId);
              } catch (MCRException | MCRActiveLinkException exc) {
              LOGGER.error("unable to delete derivate link of object " + id + " and derivate " + derivateId,
                  exc);
          }
        }
    }
    
    public static void deleteFileLinks(List<MCRObjectID> idList, MCRPath pathOfImg) {
        for (MCRObjectID id : idList) {
            try {
                removeLink(id, pathOfImg.getOwner() + pathOfImg.getOwnerRelativePath());
            } catch (MCRActiveLinkException exc) {
                LOGGER.error("unable to delete derivate link of object " + id + " and file " + pathOfImg, exc);
            }
        }
    }
    
    public static void deleteFileLink(MCRPath pathOfImg) throws SolrServerException, IOException {
        deleteFileLink(pathOfImg.getOwner() + pathOfImg.getOwnerRelativePath());
    }
    
    public static void deleteFileLink(String pathOfImg) throws SolrServerException, IOException {
        List<MCRObjectID> idList = getLinks(pathOfImg);
        for (MCRObjectID id : idList) {
            try {
                DerivateLinkUtil.removeLink(id, pathOfImg);
              } catch (MCRException | MCRActiveLinkException exc) {
              LOGGER.error("unable to delete derivate link of object " + id + " and file " + pathOfImg,
                  exc);
          }
        }
    }
    
    public static List<MCRObjectID> getLinks(MCRPath pathOfImg) throws SolrServerException, IOException {
        return getLinks(pathOfImg.getOwner() + pathOfImg.getOwnerRelativePath());
    }
        
    public static List<MCRObjectID> getLinks(String pathOfImg) throws SolrServerException, IOException {
        SolrClient solrServer = MCRSolrClientFactory.getSolrClient();
        ModifiableSolrParams params = new ModifiableSolrParams();
        List<MCRObjectID> idList = new ArrayList<MCRObjectID>();
        params.add("q", "derivateLink:" + pathOfImg);
        params.set("rows", 100);
        params.set("fl", "id");
        int numFound = Integer.MAX_VALUE, start = 0;
        while (start < numFound) {
            params.set("start", start);
            QueryResponse response = solrServer.query(params);
            SolrDocumentList results = response.getResults();
            numFound = (int) results.getNumFound();
            start += results.size();
            for (SolrDocument doc : results) {
                String docId = (String) doc.getFieldValue("id");
                try {
                    idList.add(MCRJerseyUtil.getID(docId));                    
                }
                catch (WebApplicationException e) {
                    e.printStackTrace();
                }
            }
        }
        return idList;
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
