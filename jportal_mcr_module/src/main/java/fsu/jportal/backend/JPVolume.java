package fsu.jportal.backend;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

/**
 * Volume abstraction. Be aware that this class is not complete.
 * 
 * @author Matthias Eichner
 */
public class JPVolume implements JPContainer {

    private MCRObject volume;

    private Map<MCRObjectID, JPComponent> childrenMap;

    public JPVolume() {
        volume = new MCRObject();
        volume.setId(MCRObjectID.getNextFreeId("jportal_jpvolume"));
        volume.setSchema("datamodel-jpvolume.xsd");
        volume.setImportMode(true);
        childrenMap = new HashMap<MCRObjectID, JPComponent>();
    }

    public JPVolume(MCRObject volume) {
        if (!volume.getId().getTypeId().equals("jpvolume")) {
            throw new IllegalArgumentException("Object is not a jpvolume " + volume.getId());
        }
        this.volume = volume;
        this.childrenMap = new HashMap<MCRObjectID, JPComponent>();
    }

    @Override
    public void addChild(JPComponent child) {
        MCRMetaLinkID link = new MCRMetaLinkID("parent", volume.getId(), null, getTitle());
        child.getObject().getStructure().setParent(link);
        childrenMap.put(child.getObject().getId(), child);
    }

    @Override
    public void removeChild(MCRObjectID id) {
        JPComponent component = childrenMap.remove(id);
        component.getObject().getStructure().setParent((MCRMetaLinkID) null);
    }

    @Override
    public Collection<JPComponent> getChildren() {
        return Collections.unmodifiableCollection(childrenMap.values());
    }

    @Override
    public void importComponent() throws MCRPersistenceException, MCRActiveLinkException {
        MCRMetadataManager.update(volume);
        for (JPComponent component : childrenMap.values()) {
            component.importComponent();
        }
    }

    public void setTitle(String title) {
        MCRMetaElement maintitles = new MCRMetaElement(MCRMetaLangText.class, "maintitles", true, false, null);
        maintitles.addMetaObject(new MCRMetaLangText("maintitle", null, null, 0, null, title));
        volume.getMetadata().setMetadataElement(maintitles);
    }

    public void setHiddenPosition(String position) {
        if (position == null) {
            volume.getMetadata().removeMetadataElement("hidden_positions");
            return;
        }
        MCRMetaElement positions = new MCRMetaElement(MCRMetaLangText.class, "hidden_positions", false, false, null);
        positions.addMetaObject(new MCRMetaLangText("hidden_position", null, null, 0, "plain", position));
        volume.getMetadata().setMetadataElement(positions);
    }

    public void setHiddenPosition(int position) {
        setHiddenPosition(EIGHT_DIGIT_FORMAT.format(Integer.valueOf(position)));
    }

    public MCRObject getObject() {
        return volume;
    }

    @Override
    public String getTitle() {
        MCRMetaElement maintitles = volume.getMetadata().getMetadataElement("maintitles");
        if (maintitles == null) {
            return null;
        }
        MCRMetaLangText maintitle = (MCRMetaLangText) maintitles.getElementByName("maintitle");
        if (maintitle == null) {
            return null;
        }
        return maintitle.getText();
    }

}
