package org.mycore.frontend.pagegeneration;

import org.jdom.Document;
import org.jdom.Element;
import org.mycore.frontend.pagegeneration.JournalListCfg.JournalListDef;

public class JournalListCfgToXML extends Document {
    public JournalListCfgToXML(JournalListCfg journalListCfg) {
        super(new Element("journalListCfg"));
        Element rootElement = getRootElement();
        for (JournalListDef listDef : journalListCfg.getListDefs()) {
            rootElement.addContent(new FileNameXML(listDef.getFileName()));
            rootElement.addContent(listDef.getQuery().buildXML().getRootElement().detach());
        }
    }
    
    private class FileNameXML extends Element{
        public FileNameXML(String fileName) {
            super("fileName");
            setText(fileName);
        }
    }
}
