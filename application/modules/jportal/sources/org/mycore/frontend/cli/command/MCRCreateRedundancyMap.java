package org.mycore.frontend.cli.command;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.mycore.common.MCRConfiguration;

public class MCRCreateRedundancyMap {

    private static final Logger LOGGER = Logger.getLogger(MCRFindDuplicates.class);
    private static final String FS = System.getProperty("file.seperator", "/");
    private static final String DIR = MCRConfiguration.instance().getString("MCR.doubletFinder") + FS;

    /**
     * Creates a file with duplicate informations of a specified type.
     * @throws Exception
     */
    public static void internalCreateRedundancyMap(String type) throws Exception {
        Document d;
        try {
            d = new SAXBuilder().build(new File(DIR + "checkForDuplicates-" + type + ".xml"));
        } catch(Exception e) {
            LOGGER.error("Couldnt find checkForDuplicates-" + type + ".xml");
            return;
        }
        Element rootElement = d.getRootElement();
        // filter for elements with an object tag
        Filter elementAndObjectFilter = new Filter() {
            public boolean matches(Object obj) {
                if (obj instanceof Element) {
                    Element elem = (Element) obj;
                    if (elem.getName().equals("object") && elem.getAttribute("objId") != null) {
                        return true;
                    }
                }
                return false;
            }
        };
        // create a hashtable<objId, DuplicateObject> for faster search
        LOGGER.info("Creating the hashtable.");
        Hashtable<String, DuplicateObject> duplicateList = new Hashtable<String, DuplicateObject>();
        List objectList = rootElement.getContent(elementAndObjectFilter);
        for (Object o : objectList) {
            Element objectElement = (Element) o;
            List conditionList = objectElement.getChildren("condition");
            DuplicateObject dupObject = new DuplicateObject();
            for (Object condition : conditionList) {
                Element cElement = (Element) condition;
                dupObject.addCondition(cElement.getAttributeValue("id"), cElement.getAttributeValue("value").toLowerCase());
            }
            duplicateList.put(objectElement.getAttributeValue("objId"), dupObject);
        }

        LOGGER.info("Fill the hashtable with duplicate informations - this can take up some time.");
        // fill the hashtable with duplicate informations
        int count = 0;
        int lastPrintedInterval = 0;
        // TODO: doesnt found a generic solution for Map.Entry<String, DuplicateObject> to array
        Object[] compareArray = duplicateList.entrySet().toArray();
        int objectCount = compareArray.length;
        Iterator<Map.Entry<String, DuplicateObject>> it = duplicateList.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, DuplicateObject> currentEntry = it.next();
            count++;
            // compare object is already a duplicate -> no comparison necessary
            if (currentEntry.getValue().isDuplicate())
                continue;

            DuplicateObject currentObject = currentEntry.getValue();

            // compare to n + 1 objects
            for (int i = count; i < compareArray.length; i++) {
                Map.Entry<String, DuplicateObject> compareEntry = (Map.Entry<String, DuplicateObject>) compareArray[i];
                // compare object is already a duplicate -> no comparison necessary
                if (compareEntry.getValue().isDuplicate())
                    continue;

                if (currentEntry.getKey().equals("jportal_person_00000163") && compareEntry.getKey().equals("jportal_person_00010010")) {
                    currentEntry.toString();
                }

                DuplicateObject compareObject = compareEntry.getValue();
                boolean equals = currentObject.areConditionsEqual(compareObject.getConditions());
                if (equals) {
                    // set the duplicate information
                    currentEntry.getValue().setDuplicate(true);
                    currentEntry.getValue().getDuplicateLinkList().add(compareEntry.getKey());
                }
            }
            lastPrintedInterval = printPercentLogStatus(count, objectCount, 10, lastPrintedInterval);
        }

        LOGGER.info("Create the duplicate xml-file.");
        // generate a new xml-document with the duplicates
        Document duplicateXMLFile = new Document();
        Element duplicateRootElement = new Element("redundancyMap");
        duplicateRootElement.setAttribute("type", type);
        duplicateRootElement.setAttribute("tableHead", rootElement.getAttributeValue("tableHead"));
        duplicateXMLFile.setRootElement(duplicateRootElement);

        count = 1;
        ArrayList<String> markedAsDuplicate = new ArrayList<String>();
        it = duplicateList.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, DuplicateObject> entry = it.next();
            // Entry is a duplicate and is not added to the xml-structure
            if (entry.getValue().isDuplicate() && !markedAsDuplicate.contains(entry.getKey())) {
                // add new redundancy object
                Element redundancyObjects = new Element("redundancyObjects");
                redundancyObjects.setAttribute("id", String.valueOf(count));
                redundancyObjects.setAttribute("name", getHashtableValues(entry.getValue().getConditions()));
                Element redundancyObject = new Element("object");
                redundancyObject.setAttribute("objId", entry.getKey());
                redundancyObjects.addContent(redundancyObject);
                markedAsDuplicate.add(entry.getKey());
                // add all equal elements
                for (String equalElementString : entry.getValue().getDuplicateLinkList()) {
                    redundancyObject = new Element("object");
                    redundancyObject.setAttribute("objId", equalElementString);
                    markedAsDuplicate.add(equalElementString);
                    redundancyObjects.addContent(redundancyObject);
                }
                duplicateRootElement.addContent(redundancyObjects);
                count++;
            }
        }
        // write the xml document to the file system
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        FileOutputStream output = new FileOutputStream(DIR + "redundancy-" + type + ".xml");
        outputter.output(duplicateXMLFile, output);
    }

    /**
     * Return all values of a table as a String separated by a comma.
     * @param table The hashtable.
     * @return Hashtable values as a String.
     */
    protected static String getHashtableValues(Hashtable<String, String> table) {
        String redName = "";
        Collection<String> col = table.values();
        int count = 0;
        for (String s : col) {
            redName += s;
            if (count < col.size() - 1)
                redName += ",";
            count++;
        }
        return redName;
    }

    /**
     * Prints a percent status to the console.
     * @param curCount The current value.
     * @param maxCount The maximum value.
     * @param interval The interval on which the status is printed. (For example 10 means every ten percent)
     * @param lastPrintedInterval The last interval that was printed. Needs to be saved by the calling method.
     * @return The last printed interval.
     */
    protected static int printPercentLogStatus(int curCount, int maxCount, int interval, int lastPrintedInterval) {
        int percent = (int) (((float) curCount / (float) maxCount) * 100);

        if (percent > 0 && lastPrintedInterval != percent && percent % interval == 0) {
            LOGGER.info(percent + "% completed");
            lastPrintedInterval = percent;
        }
        return lastPrintedInterval;
    }

    /**
     * Object which is stored in a hashtable to provide an abstraction of
     * the xml structure.
     */
    private static final class DuplicateObject {
        private boolean isDuplicate = false;

        private ArrayList<String> duplicateLinkList = new ArrayList<String>();

        private Hashtable<String, String> conditions = new Hashtable<String, String>();

        public boolean isDuplicate() {
            return isDuplicate;
        }

        public void setDuplicate(boolean isDuplicate) {
            this.isDuplicate = isDuplicate;
        }

        public ArrayList<String> getDuplicateLinkList() {
            return duplicateLinkList;
        }

        public void setDuplicateLinkList(ArrayList<String> duplicateLinkList) {
            this.duplicateLinkList = duplicateLinkList;
        }

        public void addCondition(String id, String value) {
            conditions.put(id, value);
        }

        public Hashtable<String, String> getConditions() {
            return conditions;
        }

        /**
         * Compares the current condition with the compare condition.
         * Each value of an key has to be equal with the compare value.
         * @param compareConditions The compare con
         * @return If the conditions are equal.
         */
        public boolean areConditionsEqual(Hashtable<String, String> compareConditions) {
            Iterator<Map.Entry<String, String>> i = conditions.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<String, String> entry = i.next();
                String compareValue = compareConditions.get(entry.getKey());
                if (compareValue == null || !compareValue.equals(entry.getValue()))
                    return false;
            }
            return true;
        }
    }
}
