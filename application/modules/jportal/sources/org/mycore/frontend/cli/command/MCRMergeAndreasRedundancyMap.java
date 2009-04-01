package org.mycore.frontend.cli.command;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.mycore.common.MCRConfiguration;

public class MCRMergeAndreasRedundancyMap {

    private static final Logger LOGGER = Logger.getLogger(MCRFindDuplicates.class);
    private static final String FS = System.getProperty("file.seperator", "/");
    private static final String DIR = MCRConfiguration.instance().getString("MCR.doubletFinder") + FS;

    public static void merge(String type, String file) throws Exception {

        // open both files
        Document andreasDocument;
        Document myDocument;
        try {
            andreasDocument = new SAXBuilder().build(new File(file));
        } catch(Exception e) {
            LOGGER.error("Couldnt find " + file);
            return;
        }
        try {
            myDocument = new SAXBuilder().build(new File(DIR + "redundancy-" + type + ".xml"));
        } catch(Exception e) {
            LOGGER.error("Couldnt find redundancy-" + type + ".xml");
            return;
        }

        merge(andreasDocument.getRootElement(), myDocument.getRootElement());
        
        // save merged document
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        FileOutputStream output = new FileOutputStream(DIR + "redundancy-" + type + "-merged.xml");
        outputter.output(myDocument, output);
    }

    private static void merge(Element andRE, Element myRE) {
        // create a hashtable of the entrys of my document. id of each entry is the obj id.
        Hashtable<String, Element> myEntries = new Hashtable<String, Element>();
        // filter
        Filter filter = new Filter() {
            public boolean matches(Object arg0) {
                // if its an element
                if(!(arg0 instanceof Element))
                    return false;
                Element e = (Element)arg0;
                // if its the redundancyID element
                if(!e.getName().equals("object"))
                    return false;
                // is it closed?
                if(e.getAttribute("objId") == null)
                    return false;
                return true;
            }
        };
        // fill the hashtable
        Iterator it = myRE.getDescendants(filter);
        while(it.hasNext()) {
            Element objElement = (Element)it.next();
            String id = objElement.getAttributeValue("objId");
            myEntries.put(id, objElement);
        }

        // go through all redundancyID elements from andreas file where the status is not open
        filter = new Filter() {
            public boolean matches(Object arg0) {
                // if its an element
                if(!(arg0 instanceof Element))
                    return false;
                Element e = (Element)arg0;
                // if its the redundancyID element
                if(!e.getName().equals("redundancyID"))
                    return false;
                // is it closed?
                if(e.getAttribute("status") == null || e.getAttribute("status").equals("open"))
                    return false;
                return true;
            }
        };

        it = andRE.getDescendants(filter);
        while(it.hasNext()) {
            Element closedElement = (Element)it.next();
            // ids
            String notRedundancyID = closedElement.getAttributeValue("notRedundancyID");
            String otherID = closedElement.getText();
            // general infos of the element
            String status = closedElement.getAttributeValue("status");
            String user = closedElement.getAttributeValue("user");
            String userRealName = closedElement.getAttributeValue("userRealName");
            String time = closedElement.getAttributeValue("time");
            String timePretty = closedElement.getAttributeValue("timePretty");

            // search through generated hashtable to find the same element 
            Element myNotRedElement = myEntries.get(notRedundancyID);
            Element myOtherElement = myEntries.get(otherID);
            // check if both exists
            if(myNotRedElement != null && myOtherElement != null) {
                // check if they are in the same group
                Element groupElement1 = myNotRedElement.getParentElement();
                Element groupElement2 = myOtherElement.getParentElement();
                if(groupElement1.equals(groupElement2)) {
                    // all fine -> set status
                    myNotRedElement.setAttribute("status", "nonDoublet");
                    if(status.equals("denied"))
                        myOtherElement.setAttribute("status", "nonDoublet");
                    else
                        myOtherElement.setAttribute("status", "doublet");

                    // set group infos
                    groupElement1.setAttribute("user", user);
                    groupElement1.setAttribute("userRealName", userRealName);
                    groupElement1.setAttribute("time", time);
                    groupElement1.setAttribute("timePretty", timePretty);
                    if(statusOfAllChildsSet(groupElement1))
                        groupElement1.setAttribute("status", "closed");
                }
            }
        }
    }

    private static boolean statusOfAllChildsSet(Element groupElement) {
        List<Element> l = (List<Element>)groupElement.getContent(new ElementFilter("object"));
        for(Element o : l) {
            if(o.getAttribute("status") == null)
                return false;
        }
        return true;
    }
}