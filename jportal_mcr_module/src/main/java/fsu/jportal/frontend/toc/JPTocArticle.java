package fsu.jportal.frontend.toc;

import java.util.List;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPComponent;
import fsu.jportal.backend.JPLegalEntity;
import org.apache.logging.log4j.LogManager;
import org.jdom2.Element;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.metadata.JPMetaDate;
import org.mycore.datamodel.metadata.MCRObjectID;

/**
 * POJO representation for table of content entry of an jportal article.
 *
 * @author Matthias Eichner
 */
public class JPTocArticle extends JPTocResult {

    private JPArticle article;

    public JPTocArticle(MCRObjectID id) {
        this.article = new JPArticle(id);
    }

    public String getSize() {
        return this.article.getSize().orElse(null);
    }

    public JPLegalEntity getAuthor() {
        return this.article.getCreator().orElse(null);
    }

    public List<JPMetaDate> getDates() {
        return this.article.getDates();
    }

    public String getRubrics() {
        List<String> rubrics = this.article.getRubrics(MCRSessionMgr.getCurrentSession().getCurrentLanguage());
        if (rubrics.isEmpty()) {
            return null;
        }
        return String.join("; ", rubrics);
    }

    @Override
    public Element toXML() {
        Element element = super.toXML();
        try {
            if (getSize() != null) {
                element.addContent(new Element("size").setText(getSize()));
            }
            if (getAuthor() != null) {
                element.addContent(toElement("author", getAuthor()));
            }
            if (getRubrics() != null) {
                element.addContent(new Element("rubric").setText(getRubrics()));
            }
            for (JPMetaDate date : getDates()) {
                element.addContent(toElement(date));
            }
        } catch (Exception exc) {
            LogManager.getLogger().error("Unable to format article " + getId(), exc);
        }
        return element;
    }

    @Override
    public JPComponent getComponent() {
        return this.article;
    }

}
