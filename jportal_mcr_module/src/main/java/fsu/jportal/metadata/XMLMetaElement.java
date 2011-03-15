package fsu.jportal.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.jdom.Element;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaXML;

public class XMLMetaElement{
    List<XMLMetaElementEntry> listOfMetaElemEntry;
    private String tagName;
    
    public XMLMetaElement(String tagName) {
        this.tagName = tagName;
    }
    
    public void addMetaElemEntry(XMLMetaElementEntry entry){
        if(listOfMetaElemEntry == null){
            listOfMetaElemEntry = new ArrayList<XMLMetaElementEntry>();
        }
        
        listOfMetaElemEntry.add(entry);
    }
    
    public MCRMetaElement toMCRMetaElement() {
        MCRMetaElement mcrMetaElement = createMetaElement();

        for (XMLMetaElementEntry metaElemEntry : listOfMetaElemEntry) {
            MCRMetaXML metaXML = createMetaXML(metaElemEntry.getMetaElemName(), metaElemEntry.getLang());
            createTagsInMetaXML(metaElemEntry, metaXML);
            mcrMetaElement.addMetaObject(metaXML);
        }

        return mcrMetaElement;
    }

    private MCRMetaXML createMetaXML(String metaElemName, String lang) {
        MCRMetaXML metaXML = new MCRMetaXML(metaElemName, null, 0);
        metaXML.setLang(lang);
        return metaXML;
    }

    private void createTagsInMetaXML(XMLMetaElementEntry metaElemEntry, MCRMetaXML metaXML) {
        for (Entry<String, String> tagValue : metaElemEntry.getTagValueMap().entrySet()) {
            metaXML.addContent(createElement(tagValue.getKey(), tagValue.getValue()));
        }
    }

    private MCRMetaElement createMetaElement() {
        MCRMetaElement mcrMetaElement = new MCRMetaElement();
        mcrMetaElement.setTag(this.tagName);
        mcrMetaElement.setClass(MCRMetaXML.class);
        return mcrMetaElement;
    }

    private Element createElement(String tagName, String nameStr) {
        Element element = new Element(tagName);
        element.setText(nameStr);
        return element;
    }
}