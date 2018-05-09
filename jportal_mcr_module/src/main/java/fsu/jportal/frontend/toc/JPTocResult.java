package fsu.jportal.frontend.toc;

import fsu.jportal.backend.JPComponent;
import fsu.jportal.backend.JPLegalEntity;
import fsu.jportal.util.JPDateUtil;
import org.jdom2.Element;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.metadata.JPMetaDate;
import org.mycore.datamodel.metadata.MCRObjectID;

/**
 * Base POJO for table of content entry.
 *
 * @author Matthias Eichner
 */
public abstract class JPTocResult {

    public MCRObjectID getId() {
        return getComponent().getId();
    }

    public String getTitle() {
        return getComponent().getTitle();
    }

    public Element toXML() {
        Element e = new Element("result").setAttribute("id", getId().toString());
        if (getTitle() != null) {
            e.addContent(new Element("title").setText(getTitle()));
        }
        return e;
    }

    public abstract JPComponent getComponent();

    /**
     * Helper method to convert a JPMetaDate to a jdom element.
     *
     * @param metaDate the date to convert
     * @return jdom element
     */
    protected Element toElement(JPMetaDate metaDate) {
        Element date = new Element("date");
        date.setText(JPDateUtil.prettify(metaDate, MCRSessionMgr.getCurrentSession().getCurrentLanguage()));
        date.setAttribute("type", metaDate.getType());
        return date;
    }

    /**
     * Helper method to convert a JPLegalEntity to a jdom element.
     *
     * @param legalEntity the legal entity to convert
     * @return jdom element
     */
    protected Element toElement(String name, JPLegalEntity legalEntity) {
        Element entity = new Element(name);
        entity.setText(legalEntity.getTitle());
        entity.setAttribute("id", legalEntity.getId().toString());
        return entity;
    }

}
