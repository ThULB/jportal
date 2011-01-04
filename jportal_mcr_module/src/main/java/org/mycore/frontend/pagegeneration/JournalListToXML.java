/**
 * 
 */
package org.mycore.frontend.pagegeneration;

import org.jdom.Document;
import org.jdom.Element;
import org.mycore.frontend.pagegeneration.JournalListXML.Section;

public class JournalListToXML extends Document{
    public JournalListToXML(JournalListXML list) {
        super(new Element("journalList"));
        Element rootElement = getRootElement();
        rootElement.setAttribute("mode", "alphabetical");
        rootElement.setAttribute("type", list.getType());
        
        for (Section section : list.getSections()) {
            rootElement.addContent(new SectionToXML(section));
        }
    }
    
    private class SectionToXML extends Element {
        public SectionToXML(Section section) {
            super("section");
            setAttribute("name", section.getName());
            
            for (Entry entry : section) {
                addContent(new EntryToXML(entry));
            }
        }
    }
    
    private class EntryToXML extends Element {
        public EntryToXML(Entry entry) {
            super("journal");
            setAttribute("title", entry.getTitle());
            setText(entry.getId());
        }
    }
}