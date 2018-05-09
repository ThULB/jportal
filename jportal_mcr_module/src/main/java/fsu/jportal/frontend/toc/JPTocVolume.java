package fsu.jportal.frontend.toc;

import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.backend.JPVolume;
import org.jdom2.Element;
import org.mycore.datamodel.metadata.JPMetaDate;
import org.mycore.datamodel.metadata.MCRObjectID;

/**
 * POJO representation for "table of content" entry of an jportal volume.
 *
 * @author Matthias Eichner
 */
public class JPTocVolume extends JPTocResult {

    private JPVolume volume;

    public JPTocVolume(MCRObjectID id) {
        this.volume = new JPVolume(id);
    }

    public JPMetaDate getPublished() {
        return getComponent().getDate(JPPeriodicalComponent.DateType.published).orElse(null);
    }

    @Override
    public Element toXML() {
        Element element = super.toXML();
        JPMetaDate published = getPublished();
        if (published != null) {
            element.addContent(toElement(published));
        }
        return element;
    }

    @Override
    public JPVolume getComponent() {
        return this.volume;
    }

}
