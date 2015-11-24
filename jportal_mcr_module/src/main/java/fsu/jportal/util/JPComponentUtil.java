package fsu.jportal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Text;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

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
     * Returns the name of the template of a given journal id.
     * 
     * @param journalID where to get the name of the template.
     * @return name of the template
     */
    public static Optional<String> getNameOfTemplate(String journalID) {
        JPInfoProvider infoProvider = new JPInfoProvider(journalID,
            "/mycoreobject/metadata/hidden_templates/hidden_template/text()");
        return infoProvider.get(new JPSimpleText());
    }

    /**
     * Returns the corresponding journal id of the given mycore object id.
     * 
     * @param mcrID mycore object id
     * @return id of the journal
     */
    public static Optional<String> getJournalID(String mcrID) {
        JPInfoProvider infoProvider = new JPInfoProvider(mcrID,
            "/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID/text()");
        return infoProvider.get(new JPSimpleText());
    }

    /**
     * Returns the main title.
     * 
     * @param mcrID mycore object
     * @return main title
     */
    public static Optional<String> getMaintitle(String mcrID) {
        JPInfoProvider infoProvider = new JPInfoProvider(mcrID,
            "/mycoreobject/metadata/maintitles/maintitle[@inherited='0']/text()");
        return infoProvider.get(new JPSimpleText());
    }

    public static Optional<String> getListType(String mcrID) {
        JPInfoProvider infoProvider = new JPInfoProvider(mcrID,
            "/mycoreobject/metadata/contentClassis1/contentClassi1/@categid");
        return infoProvider.get(new JPSimpleAttribute());
    }

}
