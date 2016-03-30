package fsu.jportal.backend;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaIFS;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

import fsu.jportal.backend.upload.Uploader;

public class JPDerivateComponent implements JPComponent {

    protected MCRDerivate derivate;

    protected Map<String, ContentInfo> newContentMap;

    /**
     * Creates a new <code>MCRDerivate</code>.
     */
    public JPDerivateComponent() {
        derivate = new MCRDerivate();
        derivate.setId(MCRObjectID.getNextFreeId("jportal_derivate"));
        String schema = MCRConfiguration.instance().getString("MCR.Metadata.Config.derivate", "datamodel-derivate.xml")
            .replaceAll(".xml", ".xsd");
        derivate.setSchema(schema);

        MCRMetaIFS ifs = new MCRMetaIFS();
        ifs.setSubTag("internal");
        ifs.setSourcePath(null);
        derivate.getDerivate().setInternals(ifs);

        this.newContentMap = new HashMap<>();
    }

    /**
     * Creates a new JPDerivateComponent container for the given mcrId.
     * 
     * @param mcrId a mycore object id
     */
    public JPDerivateComponent(String mcrId) {
        this(MCRObjectID.getInstance(mcrId));
    }

    /**
     * Creates a new JPDerivateComponent container for the given mcrId.
     * 
     * @param mcrId a mycore object id
     */
    public JPDerivateComponent(MCRObjectID mcrId) {
        this(MCRMetadataManager.retrieveMCRDerivate(mcrId));
    }

    /**
     * Creates a new JPDerivateComponent container for the mycore object.
     * 
     * @param mcrObject the mycore object
     */
    public JPDerivateComponent(MCRDerivate mcrDerivate) {
        this.derivate = mcrDerivate;
        this.newContentMap = new HashMap<>();
    }

    @Override
    public MCRDerivate getObject() {
        return this.derivate;
    }

    @Override
    public String getTitle() {
        return this.derivate.getLabel();
    }

    /**
     * Add new content to the derivate.
     * 
     * @param content a URL to the content
     * @param targetPath where the content is stored (e.g. "my_image.tif" or "alto/my_alto.xml")
     */
    public void add(URL content, String targetPath) {
        this.add(content, targetPath, null);
    }

    /**
     * Add new content to the derivate.
     * 
     * @param content a URL to the content
     * @param targetPath where the content is stored (e.g. "my_image.tif" or "alto/my_alto.xml")
     * @param contentSize size of the content in bytes, can be null
     */
    public void add(URL content, String targetPath, Integer contentSize) {
        ContentInfo ci = new ContentInfo();
        ci.contentURL = content;
        ci.contentSize = contentSize;
        this.newContentMap.put(targetPath, ci);
    }

    @Override
    public void store() throws MCRPersistenceException, MCRActiveLinkException, MCRAccessException, IOException {
        MCRMetadataManager.update(this.derivate);
        Uploader uploader = new Uploader(this.derivate);
        for (Map.Entry<String, ContentInfo> entry : this.newContentMap.entrySet()) {
            String path = entry.getKey();
            ContentInfo contentInfo = entry.getValue();
            try (InputStream in = contentInfo.contentURL.openStream()) {
                uploader.upload(in, path, contentInfo.contentSize);
            }
        }
        DerivateTools.updateMainFile(this.derivate);
    }

    private static class ContentInfo {
        private URL contentURL;

        private Integer contentSize;
    }

}
