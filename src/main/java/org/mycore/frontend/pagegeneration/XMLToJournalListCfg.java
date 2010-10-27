package org.mycore.frontend.pagegeneration;

import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.mycore.services.fieldquery.MCRQuery;

public class XMLToJournalListCfg extends JournalListCfg {
    public XMLToJournalListCfg(Document xml) {
        List listDefs = xml.getRootElement().getChildren();
        
        for (Iterator iterator = listDefs.iterator(); iterator.hasNext();) {
            Element listDef = (Element) iterator.next();
            addListDef(new XMLToJournalListDef(listDef));
        }
    }
    
    private class XMLToJournalListDef extends JournalListDef {
        public XMLToJournalListDef(Element listDefXML) {
            setFileName(listDefXML.getChildText("fileName"));
            setType(listDefXML.getChildText("type"));
            Element queryElem = (Element)listDefXML.getChild("query").detach();
            Document queryDoc = new Document(queryElem);
            MCRQuery query = MCRQuery.parseXML(queryDoc);
            setQuery(query);
        }
    }
}
