package org.mycore.dataimport.pica;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

public class MCRGbvSruRecordElementIterator implements Iterator<Element> {
    private List<Element> recordList;
    private int recordPos;

    @SuppressWarnings("unchecked")
    public MCRGbvSruRecordElementIterator(Document doc) {
        this.recordList = new ArrayList<Element>();
        Namespace srwNs = Namespace.getNamespace("srw", "http://www.loc.gov/zing/srw/");
        Element srwRecordsElement = doc.getRootElement().getChild("records", srwNs);
        List<Element> srwRecordList = srwRecordsElement.getChildren("record", srwNs);

        for(Element srwRecordElement : srwRecordList) {
            Element srwRecordData = srwRecordElement.getChild("recordData", srwNs);
            recordList.add(srwRecordData.getChild("record"));
        }
        this.recordPos = 0;
    }

    public boolean hasNext() {
        if(recordList.size() > recordPos)
            return true;
        return false;
    }

    public Element next() {
        return recordList.get(recordPos++);
    }

    public void remove() {
        recordList.remove(recordPos);
    }
}