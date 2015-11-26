package fsu.jportal.backend;

import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

/**
 * Volume abstraction. Be aware that this class is not complete.
 * 
 * @author Matthias Eichner
 */
public class JPVolume extends JPContainer {

    public JPVolume() {
        super();
    }

    public JPVolume(String mcrId) {
        super(mcrId);
    }

    public JPVolume(MCRObjectID mcrId) {
        super(mcrId);
    }

    public JPVolume(MCRObject mcrObject) {
        super(mcrObject);
    }

    @Override
    public String getType() {
        return "jpvolume";
    }

    public void setHiddenPosition(String position) {
        if (position == null) {
            object.getMetadata().removeMetadataElement("hidden_positions");
            return;
        }
        MCRMetaElement positions = new MCRMetaElement(MCRMetaLangText.class, "hidden_positions", false, false, null);
        positions.addMetaObject(new MCRMetaLangText("hidden_position", null, null, 0, "plain", position));
        object.getMetadata().setMetadataElement(positions);
    }

    public void setHiddenPosition(int position) {
        setHiddenPosition(EIGHT_DIGIT_FORMAT.format(Integer.valueOf(position)));
    }

    public void addSubTitle(String title, String type) {
        MCRMetaElement subtitles = object.getMetadata().getMetadataElement("subtitles");
        if (subtitles == null) {
            subtitles = new MCRMetaElement(MCRMetaLangText.class, "subtitles", false, true, null);
            object.getMetadata().setMetadataElement(subtitles);
        }
        MCRMetaLangText subtitle = new MCRMetaLangText("subtitle", null, type, 0, "plain", title);
        subtitles.addMetaObject(subtitle);
    }

}
