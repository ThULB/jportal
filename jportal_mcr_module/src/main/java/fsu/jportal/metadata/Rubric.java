package fsu.jportal.metadata;

import java.util.Iterator;

import org.jdom.Element;
import org.jdom.Namespace;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaInterface;
import org.mycore.datamodel.metadata.MCRMetaXML;

public class Rubric implements Iterable<Rubric.Label>{


    public class LabelIterator implements Iterator<Label> {

        private Iterator<MCRMetaInterface> metaDataIterator;

        public LabelIterator(Iterator<MCRMetaInterface> iterator) {
            this.metaDataIterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return metaDataIterator.hasNext();
        }

        @Override
        public Label next() {
            MCRMetaInterface nextLabel = metaDataIterator.next();
            Element labelXML = nextLabel.createXML();
            String lang = labelXML.getAttributeValue(LANG, Namespace.XML_NAMESPACE);
            String text = labelXML.getChildText(TEXT);
            String description = labelXML.getChildText(DESCRIPTION);
            
            return new Label(lang,text,description);
        }

        @Override
        public void remove() {
            metaDataIterator.remove();
        }

    }

    public class Label {
        
        private String lang;
        private String text;
        private String description;

        public Label(String lang, String text, String description) {
            setLang(lang);
            setText(text);
            setDescription(description);
        }

        public void setLang(String lang) {
            this.lang = lang;
        }

        public String getLang() {
            return lang;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
        
    }
    
    public static final String DESCRIPTION = "descriptionsen";

    public static final String TEXT = "text";

    public static final String LABEL = "label";
    
    public static final String LANG = "lang";
    
    public static final String TAGNAME = "rubric";

    public static final String PARENTID = "parentID";

    public static final String CATEGID = "categID";

    public static final String ROOTID = "rootID";

    public static final String ID = "ID";
    
    private MCRMetaElement rubricMetaElement;
    
    private String parentID = null;
    
    public Rubric() {
        setRubricMetaElement(new MCRMetaElement());
        getRubricMetaElement().setTag(TAGNAME);
        getRubricMetaElement().setClass(MCRMetaXML.class);
    }

    public void setRubricMetaElement(MCRMetaElement rubricMetaElement) {
        this.rubricMetaElement = rubricMetaElement;
    }

    public MCRMetaElement getRubricMetaElement() {
        return rubricMetaElement;
    }
    
    public void addLabel(String lang, String text, String description){
        MCRMetaXML labelXML = new MCRMetaXML();
        labelXML.setSubTag(LABEL);
        labelXML.setLang(lang);
        labelXML.addContent(newElement(TEXT, text));
        labelXML.addContent(newElement(DESCRIPTION, description));
        
        getRubricMetaElement().addMetaObject(labelXML);
    }
    
    private Element newElement(String tagName, String text) {
        Element xmlTag = new Element(tagName);
        xmlTag.addContent(text);
        return xmlTag;
    }

    @Override
    public Iterator<Label> iterator() {
        return new LabelIterator(getRubricMetaElement().iterator());
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    public String getParentID() {
        return parentID;
    }
}