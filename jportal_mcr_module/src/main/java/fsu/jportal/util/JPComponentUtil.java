package fsu.jportal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Text;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRException;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPComponent;
import fsu.jportal.backend.JPContainer;
import fsu.jportal.backend.JPInstitution;
import fsu.jportal.backend.JPJournal;
import fsu.jportal.backend.JPLegalEntity;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.backend.JPPerson;
import fsu.jportal.backend.JPVolume;

/**
 * Utility class for jportal components.
 */
public abstract class JPComponentUtil {

    public static class JPInfoProvider {
        private String id;

        private String[] xpathList;

        public JPInfoProvider(String id, String xpath) {
            this(id, new String[] { xpath });
        }

        public JPInfoProvider(String id, String... xpath) {
            this.id = id;
            this.xpathList = xpath;
        }

        public <T> T get(JPObjectInfo<T> fromObj) {
            MCRObjectID mcrid = MCRObjectID.getInstance(id);
            List<Object> nodes = new ArrayList<Object>();
            if (MCRMetadataManager.exists(mcrid)) {
                Document xml = MCRMetadataManager.retrieve(mcrid).createXML();
                for (String xpath : xpathList) {
                    XPathExpression<Object> exp = XPathFactory.instance().compile(xpath);
                    Object node = exp.evaluateFirst(xml);
                    if (node != null) {
                        nodes.add(node);
                    }
                }
            }
            return fromObj.getInfo(nodes);
        }
    }

    public static class JPSimpleText implements JPObjectInfo<Optional<String>> {
        public Optional<String> getInfo(List<Object> node) {
            if (node.size() == 1) {
                Text textNode = (Text) node.get(0);
                return Optional.of(textNode.getText());
            }
            return Optional.empty();
        }
    }

    public static class JPSimpleAttribute implements JPObjectInfo<Optional<String>> {
        public Optional<String> getInfo(List<Object> node) {
            if (node.size() == 1) {
                Attribute attrNode = (Attribute) node.get(0);
                return Optional.of(attrNode.getValue());
            }
            return Optional.empty();
        }
    }

    public static interface JPObjectInfo<T> {
        public T getInfo(List<Object> node);
    }

    /**
     * Returns an optional of a mycore id when the identifier is valid
     * and the object does exists.
     * 
     * @param id the id as string
     * @return optional mycore object id
     */
    public static Optional<MCRObjectID> getValidID(String id) {
        MCRObjectID mcrId = null;
        try {
            mcrId = MCRObjectID.getInstance(id);
        } catch (MCRException mcrExc) {
            return Optional.empty();
        }
        if (!MCRMetadataManager.exists(mcrId)) {
            return Optional.empty();
        }
        return Optional.of(mcrId);
    }

    /**
     * Returns the corresponding journal id of the given mycore object id.
     * 
     * @param mcrID mycore object id
     * @return id of the journal
     */
    public static String getJournalID(String mcrID) {
        return getPeriodical(MCRObjectID.getInstance(mcrID)).map(JPPeriodicalComponent::getJournalId).orElse(null);
    }

    /**
     * Returns the main title.
     * 
     * @param mcrID mycore object
     * @return main title
     */
    public static Optional<String> getMaintitle(String mcrID) {
        return get(mcrID).map(JPComponent::getTitle);
    }

    public static Optional<String> getListType(String mcrID) {
        JPInfoProvider infoProvider = new JPInfoProvider(mcrID,
            "/mycoreobject/metadata/contentClassis1/contentClassi1/@categid");
        return infoProvider.get(new JPSimpleAttribute());
    }

    /**
     * Returns a <JPComponent> based on the given id.
     * 
     * @param id the mycore id
     * @return optional of JPComponent
     */
    public static Optional<? extends JPComponent> get(String id) {
        return get(MCRObjectID.getInstance(id));
    }

    /**
     * Returns a <JPComponent> based on the given id.
     * 
     * @param id the mycore id
     * @return optional of JPComponent
     */
    public static Optional<? extends JPComponent> get(MCRObjectID id) {
        Optional<JPPeriodicalComponent> periodical = getPeriodical(id);
        if (periodical.isPresent()) {
            return periodical;
        } else {
            return getLegalEntity(id);
        }
    }

    /**
     * Checks if the given mcr object id is one of jpjournal, jpvolume or jparticle.
     * 
     * @param id the mycore identifier
     * @return true if its a periodical, otherwise false
     */
    public static boolean isPeriodical(MCRObjectID id) {
        String type = id.getTypeId();
        return type.equals(JPArticle.TYPE) || type.equals(JPVolume.TYPE) || type.equals(JPJournal.TYPE);
    }

    /**
     * checks if the given mcr object id is one of person or jpinst.
     * 
     * @param id the mycore identifer
     * @return true if its a legal entity, otherwise false
     */
    public static boolean isLegalEntity(MCRObjectID id) {
        String type = id.getTypeId();
        return type.equals(JPPerson.TYPE) || type.equals(JPInstitution.TYPE);
    }

    /**
     * Returns a periodical object for the given id.
     * 
     * @param id mycore identifier
     * @return periodical optional
     */
    public static Optional<JPPeriodicalComponent> getPeriodical(MCRObjectID id) {
        String type = id.getTypeId();
        if (type.equals(JPArticle.TYPE)) {
            return Optional.of(new JPArticle(id));
        } else if (type.equals(JPVolume.TYPE)) {
            return Optional.of(new JPVolume(id));
        } else if (type.equals(JPJournal.TYPE)) {
            return Optional.of(new JPJournal(id));
        }
        return Optional.empty();
    }

    /**
     * Returns a container object for the given id.
     * 
     * @param id mycore identifier
     * @return container optional
     */
    public static Optional<JPContainer> getContainer(MCRObjectID id) {
        String type = id.getTypeId();
        if (type.equals(JPVolume.TYPE)) {
            return Optional.of(new JPVolume(id));
        } else if (type.equals(JPJournal.TYPE)) {
            return Optional.of(new JPJournal(id));
        }
        return Optional.empty();
    }

    /**
     * Returns a periodical component for the given mycore object.
     * 
     * @param mcrObject the mycore object
     * @return periodical component
     */
    public static JPPeriodicalComponent getPeriodical(MCRObject mcrObject) {
        String type = mcrObject.getId().getTypeId();
        if (type.equals(JPArticle.TYPE)) {
            return new JPArticle(mcrObject);
        } else if (type.equals(JPVolume.TYPE)) {
            return new JPVolume(mcrObject);
        } else if (type.equals(JPJournal.TYPE)) {
            return new JPJournal(mcrObject);
        } else {
            throw new IllegalArgumentException("MCRObject is not a periodical component " + mcrObject.getId());
        }
    }

    /**
     * Returns a legal entity object for the given id.
     * 
     * @param id mycore identifier
     * @return legal entity optional
     */
    public static Optional<JPLegalEntity> getLegalEntity(MCRObjectID id) {
        String type = id.getTypeId();
        if (type.equals(JPPerson.TYPE)) {
            return Optional.of(new JPPerson(id));
        } else if (type.equals(JPInstitution.TYPE)) {
            return Optional.of(new JPInstitution(id));
        }
        return Optional.empty();
    }

    /**
     * Checks if the given component is of the type.
     * 
     * @param component
     * @param type something like jparticle or jpvolume 
     * @return true if its the same type
     */
    public static boolean is(JPComponent component, String type) {
        return component.getType().equals(type);
    }

    /**
     * Checks if the given id is of the type.
     * 
     * @param id the id to check
     * @param type the type
     * @return true if its the same type
     */
    public static boolean is(MCRObjectID id, String type) {
        return id.getTypeId().equals(type);
    }

}
