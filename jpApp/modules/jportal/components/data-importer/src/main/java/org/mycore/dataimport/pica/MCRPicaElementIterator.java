package org.mycore.dataimport.pica;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * This class iterates through a pica xml document to separate records. 
 * The ZiNG and the srw format are supported.
 * 
 * @author Matthias Eichner
 */
public class MCRPicaElementIterator implements Iterator<Element> {

    private List<Element> recordList;

    private int recordPos;

    /**
     * Creates a new instance of <code>MCRPicaElementIterator</code>. The document
     * is completely parsed in this constructor.
     * 
     * @param doc the document containing all the records in the ZiNG or srw format.
     * @throws UnsupportedPicaFormatException
     */
    public MCRPicaElementIterator(Document doc) throws UnsupportedPicaFormatException {
        this.recordList = new ArrayList<Element>();
        Namespace ns = doc.getRootElement().getNamespace();
        if(ns.getPrefix().equals("ZiNG"))
            createRecordListFromZiNG(doc);
        else if(ns.getPrefix().equals("srw"))
            createRecordListFromSrw(doc);
        else
            throw new UnsupportedPicaFormatException(doc.getBaseURI());
        this.recordPos = 0;
    }

    /**
     * Fills the record list.
     * 
     * @param doc the document to parse
     */
    @SuppressWarnings("unchecked")
    private void createRecordListFromZiNG(Document doc) {
        Namespace zingNS = Namespace.getNamespace("ZiNG", "urn:z3950:ZiNG:Service");
        Element recordsElement = doc.getRootElement().getChild("records", zingNS);
        List<Element> recordElementList = recordsElement.getChildren("record", zingNS);
        for (Element zingRecordElement : recordElementList) {
            Element recordDataElement = zingRecordElement.getChild("recordData", zingNS);
            Element collectionElement = (Element)recordDataElement.getChildren().get(0);
            Namespace ns = collectionElement.getNamespace();
            Element recordElement = collectionElement.getChild("record", ns);
            recordList.add(recordElement);
        }
    }

    /**
     * Fills the record list.
     * 
     * @param doc the document to parse
     */
    @SuppressWarnings("unchecked")
    private void createRecordListFromSrw(Document doc) {
        Namespace srwNs = Namespace.getNamespace("srw", "http://www.loc.gov/zing/srw/");
        Element recordsElement = doc.getRootElement().getChild("records", srwNs);
        List<Element> recordElementList = recordsElement.getChildren("record", srwNs);
        for(Element srwRecordElement : recordElementList) {
            Element srwRecordData = srwRecordElement.getChild("recordData", srwNs);
            recordList.add(srwRecordData.getChild("record"));
        }
    }

    public boolean hasNext() {
        if (recordList.size() > recordPos)
            return true;
        return false;
    }

    /**
     * Returns the next record element.
     * 
     * @return next record element
     */
    public Element next() {
        return recordList.get(recordPos++);
    }

    public void remove() {
        recordList.remove(recordPos);
    }
}