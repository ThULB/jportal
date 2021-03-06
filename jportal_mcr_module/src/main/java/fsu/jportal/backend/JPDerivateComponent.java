package fsu.jportal.backend;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaIFS;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;

import fsu.jportal.backend.mcr.JPConfig;
import fsu.jportal.backend.mcr.MetadataManager;

/**
 * MyCoRe derivate abstraction for jportal.
 *
 * @author Matthias Eichner
 */
public class JPDerivateComponent implements JPComponent {

    protected MCRDerivate derivate;

    protected LinkedHashMap<String, URL> newContentMap;

    /**
     * Creates a new <code>MCRDerivate</code>.
     */
    public JPDerivateComponent() {
        derivate = new MCRDerivate();
        derivate.setId(MCRObjectID.getNextFreeId("jportal_derivate"));

        String schema = JPConfig.getString("MCR.Metadata.Config.derivate")
                .map(propVal -> propVal.replaceAll(".xml", ".xsd"))
                .orElse("datamodel-derivate.xsd");
        derivate.setSchema(schema);

        MCRMetaIFS ifs = new MCRMetaIFS();
        ifs.setSubTag("internal");
        ifs.setSourcePath(null);
        derivate.getDerivate().setInternals(ifs);

        this.newContentMap = new LinkedHashMap<>();
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
        this(MetadataManager.retrieveMCRDerivate(mcrId));
    }

    /**
     * Creates a new JPDerivateComponent container for the mycore object.
     * 
     * @param mcrDerivate the mycore derivate
     */
    public JPDerivateComponent(MCRDerivate mcrDerivate) {
        this.derivate = mcrDerivate;
        this.newContentMap = new LinkedHashMap<>();
    }

    @Override
    public MCRDerivate getObject() {
        return this.derivate;
    }

    @Override
    public String getTitle() {
        return this.derivate.getLabel();
    }

    @Override
    public String getType() {
        return "derivate";
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
        this.newContentMap.put(targetPath, content);
    }

    /**
     * Sets a new main document (just sets the internals main doc).
     * 
     * @param mainDoc the new main doc
     */
    public void setMainDoc(String mainDoc) {
        this.derivate.getDerivate().getInternals().setMainDoc(mainDoc);
    }

    /**
     * Returns the main document. The main document is always returned without a leading slash.
     *
     * @return the derivate/internals/internal/@maindoc
     */
    public String getMainDoc() {
        String mainDoc = this.derivate.getDerivate().getInternals().getMainDoc();
        mainDoc = mainDoc.startsWith("/") ? mainDoc.substring(1) : mainDoc;
        return mainDoc;
    }

    /**
     * Returns the main document with the derivate id as link e.g. jportal_derivate_00000001/first.tif
     *
     * @return main document as link
     */
    public String getMainDocAsLink() {
        return this.getId() + "/" + getMainDoc();
    }

    /**
     * Sets the visibality of this derivate.
     * 
     * @param visible true = derivate is visible otherwise its hidden
     */
    public void setDisplay(boolean visible) {
        this.derivate.getDerivate().setDisplayEnabled(visible);
    }

    /**
     * Checks if this derivate is visible or hidden.
     * 
     * @return true if its visible
     */
    public boolean isVisible() {
        return this.derivate.getDerivate().isDisplayEnabled();

    }

    /**
     * Returns the root MCRPath of this derivate.
     *
     * @return derivate path
     */
    public MCRPath getPath() {
        return MCRPath.getPath(this.derivate.getId().toString(), "/");
    }

    /**
     * Returns the fileset urn of this derivate.
     *
     * @return optional URN
     */
    public Optional<String> getURN() {
        return Optional.ofNullable(this.derivate.getDerivate().getURN());
    }

    @Override
    public void store(StoreOption... options) throws MCRPersistenceException, MCRAccessException {
        List<StoreOption> optionList = Arrays.asList(options);

        if (optionList.contains(StoreOption.metadata)) {
            // set main doc
            Iterator<String> it = this.newContentMap.keySet().iterator();
            if (it.hasNext()) {
                this.derivate.getDerivate().getInternals().setMainDoc(it.next());
            }
            // update derivate
            MCRMetadataManager.update(this.derivate);
        }
        if (optionList.contains(StoreOption.content)) {
            // upload files
            for (Map.Entry<String, URL> entry : this.newContentMap.entrySet()) {
                String path = entry.getKey();
                URL url = entry.getValue();
                MCRPath sourcePath = MCRPath.getPath(this.derivate.getId().toString(), path);
                try (InputStream in = url.openStream()) {
                    Files.copy(in, sourcePath, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception exc) {
                    throw new MCRPersistenceException(
                        "Unable to upload " + url.toString() + " to " + sourcePath.toAbsolutePath(), exc);
                }
            }
        }
    }

}
