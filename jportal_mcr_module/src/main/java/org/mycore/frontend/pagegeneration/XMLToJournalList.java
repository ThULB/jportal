package org.mycore.frontend.pagegeneration;

import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

public class XMLToJournalList extends JournalList {
    public XMLToJournalList(Document xml) {
        List sections = xml.getRootElement().getChildren();
        
        for (Iterator sectionIter = sections.iterator(); sectionIter.hasNext();) {
            Element section = (Element) sectionIter.next();
            List entries = section.getChildren();
            
            for (Iterator entriesIter = entries.iterator(); entriesIter.hasNext();) {
                Element entry = (Element) entriesIter.next();
                add(new XMLToEntry(entry));
            }
        }
    }
    
    private class XMLToEntry extends Entry {
        public XMLToEntry(Element entry) {
            super(entry.getAttributeValue("title"), entry.getText());
        }
    }
}
