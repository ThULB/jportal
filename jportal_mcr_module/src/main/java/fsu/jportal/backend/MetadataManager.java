package fsu.jportal.backend;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.metadata.MCRBase;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectUtils;
import org.mycore.mets.model.Mets;
import org.xml.sax.SAXParseException;

import fsu.jportal.util.MetsUtil;
import fsu.thulb.connections.HttpsClient;

/**
 * Created by chi on 18.05.20
 *
 * @author Huu Chi Vu
 */
public class MetadataManager {
    private static final String REMOTE_XML = "JP.Remote.XML";
    private static final String REMOTE_METS = "JP.Remote.METS";
    private static boolean remotely = false;

    public static MCRObject retrieveMCRObject(MCRObjectID mcrId) {
        if(!remotely) {
            return MCRMetadataManager.retrieveMCRObject(mcrId);
        }

        return getRemotely(mcrId, uri -> new MCRObject(uri));
    }

    public static MCRDerivate retrieveMCRDerivate(MCRObjectID mcrId) {
        if(!remotely) {
            return MCRMetadataManager.retrieveMCRDerivate(mcrId);
        }

        return getRemotely(mcrId, uri -> new MCRDerivate(uri));
    }

    private static <T> T getRemotely(MCRObjectID mcrId, MCRObjFromURI<T> supplier) {
        String urlConf = MCRConfiguration.instance().getString(REMOTE_XML);
        if(urlConf == null || "".equals(urlConf)){
            throw new RuntimeException("Property " + REMOTE_XML + " not set.");
        }

        String urlStr = String.format(urlConf, mcrId.toString());
        try {
            URI uri = new URI(urlStr);
            return supplier.receive(uri);
        } catch (IOException | URISyntaxException | SAXParseException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not resolve object at url " + urlStr, e);
        }
    }

    public static MCRBase retrieve(MCRObjectID mcrId) {
        if(mcrId.getTypeId().equals("derivate")) {
            return retrieveMCRDerivate(mcrId);
        }

        return retrieveMCRObject(mcrId);
    }

    public static JPJournal getJournal(MCRObject object) {
        return object.getMetadata()
                .stream("hidden_jpjournalsID")
                .findFirst()
                .map(MCRMetaLangText.class::cast)
                .map(MetadataManager::retrieveJournal)
                .orElseGet(() -> retrieveJournalFromRoot(object));
    }

    private static JPJournal retrieveJournalFromRoot(MCRObject object) {
        MCRObject journal = MCRObjectUtils.getRoot(object);
        if (!journal.getId().getTypeId().equals(JPJournal.TYPE)) {
            throw new MCRException("Unable to get template of object " + journal.getId()
                    + " because its not a journal but the root ancestor of " + object.getId() + ".");
        }
        return new JPJournal(journal);
    }

    private static JPJournal retrieveJournal(MCRMetaLangText hiddenId) {
        MCRObjectID journalId = MCRObjectID.getInstance(hiddenId.getText());
        MCRObject journal = retrieveMCRObject(journalId);
        return new JPJournal(journal);
    }

    public static List<MCRObject> getAncestorsAndSelf(MCRObject object) {
        if(!remotely) {
            return MCRObjectUtils.getAncestorsAndSelf(object);
        }

        List<MCRObject> ancestors = getAncestorsRemotly(object);
        ancestors.add(0,object);
        return ancestors;
    }

    public static List<MCRObject> getAncestorsRemotly(MCRObject mcrObject) {
        List<MCRObject> ancestorList = new ArrayList<>();
        while (mcrObject.hasParent()) {
            MCRObjectID parentID = mcrObject.getStructure().getParentID();
            MCRObject parent = MetadataManager.retrieveMCRObject(parentID);
            ancestorList.add(parent);
            mcrObject = parent;
        }
        return ancestorList;
    }

    public static void setRemotely(boolean remotely) {
        MetadataManager.remotely = remotely;
    }

    public static List<MCRObject> getAncestors(MCRObject object) {
        if(!remotely){
            return MCRObjectUtils.getAncestors(object);
        }

        return getAncestorsRemotly(object);
    }

    public static boolean hasMetsFile(JPDerivateComponent derivate) {
        if(!remotely) {
            return Files.exists(derivate.getPath().resolve("mets.xml"));
        }

        String derivateId = derivate.getObject().getId().toString();
        String urlConf = MCRConfiguration.instance().getString(REMOTE_METS);
        if(urlConf == null || "".equals(urlConf)){
            throw new RuntimeException("Property " + REMOTE_METS + " not set.");
        }
        String urlStr = String.format(urlConf, derivateId);
        CloseableHttpResponse head = HttpsClient
                .head(urlStr);
        int statusCode = head.getStatusLine().getStatusCode();
        return statusCode == HttpStatus.SC_OK;
    }

    public static Mets getMets(JPDerivateComponent derivate) throws IOException, JDOMException {
        String derivateId = derivate.getId().toString();
        if(!remotely){
            return MetsUtil.getMets(derivateId);
        }

        String urlConf = MCRConfiguration.instance().getString(REMOTE_METS);
        if(urlConf == null || "".equals(urlConf)){
            throw new RuntimeException("Property " + REMOTE_METS + " not set.");
        }
        String urlStr = String.format(urlConf, derivateId);

        URL url = new URL(urlStr);
        SAXBuilder saxBuilder = new SAXBuilder();
        Document metsDoc = saxBuilder.build(url);

        return new Mets(metsDoc);
    }

    public static List<MCRObject> getDescendantsAndSelf(MCRObject object) {
        if(!remotely){
            MCRObjectUtils.getDescendantsAndSelf(object);
        }

        List<MCRObject> objectList = getDescendants(object);
        objectList.add(object);
        return objectList;
    }

    public static List<MCRObject> getDescendants(MCRObject mcrObject) {
        List<MCRObject> objectList = new ArrayList<>();
        getChildren(mcrObject).forEach(child -> objectList.addAll(getDescendantsAndSelf(child)));
        return objectList;
    }

    public static List<MCRObject> getChildren(MCRObject mcrObject) {
        return mcrObject.getStructure()
                .getChildren()
                .stream()
                .map(MCRMetaLinkID::getXLinkHrefID)
                .map(MetadataManager::retrieveMCRObject)
                .collect(Collectors.toList());
    }

    interface MCRObjFromURI<T> {
        T receive(URI uri) throws IOException, SAXParseException;
    }
}