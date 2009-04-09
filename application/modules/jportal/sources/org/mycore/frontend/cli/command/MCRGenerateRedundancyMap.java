package org.mycore.frontend.cli.command;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.mycore.common.MCRConfiguration;
import org.mycore.parsers.bool.MCRCondition;
import org.mycore.services.fieldquery.MCRFieldDef;
import org.mycore.services.fieldquery.MCRFieldValue;
import org.mycore.services.fieldquery.MCRHit;
import org.mycore.services.fieldquery.MCRQuery;
import org.mycore.services.fieldquery.MCRQueryCondition;
import org.mycore.services.fieldquery.MCRQueryManager;
import org.mycore.services.fieldquery.MCRResults;
import org.mycore.services.fieldquery.MCRSortBy;

/**
 * Generates a redundancy map for a specific type.
 * @author Matthias Eichner
 */
public class MCRGenerateRedundancyMap {

    private static final Logger LOGGER = Logger.getLogger(MCRGenerateRedundancyMap.class);
    private static final String FS = System.getProperty("file.seperator", "/");
    private static final String DIR = MCRConfiguration.instance().getString("MCR.doubletFinder") + FS;

    public static void generate(String type) throws Exception {
        AbstractRedundancyMapGenerator generator = null;
        if(type.equals("person"))
            generator = new PersonRedundancyMapGenerator();
        else if(type.equals("jpinst"))
            generator = new JPInstRedundancyMapGenerator();
        else {
            LOGGER.error("No valid type defined. Valid types are 'person' and 'jpinst'");
            return;
        }
        generator.createRedundancyMap();
        generator.saveToFile();
    }

    /**
     * Abstract class for creating a redundancy map.
     * @author Matthias Eichner
     */
    private static abstract class AbstractRedundancyMapGenerator {
        protected Element redundancyMap;

        /**
         * Returns the type of this redundancy map generator.
         * @return the type as string
         */
        protected abstract String getType();
        /**
         * @return a new arraylist for sorting.
         */
        protected abstract ArrayList<String> getSortByStringList();
        /**
         * @return the table head as string.
         */
        protected abstract String getTableHead();

        protected String getFileName() {
            return DIR + "redundancy-" + getType() + ".xml";
        }
        protected ArrayList<MCRSortBy> createSortByList() {
            ArrayList<String> sortByStrings = getSortByStringList();
            ArrayList<MCRSortBy> sortByList = new ArrayList<MCRSortBy>();
            for(String def : sortByStrings) {
                MCRFieldDef fieldDef = MCRFieldDef.getDef(def);
                MCRSortBy mcrSortBy = new MCRSortBy(fieldDef, MCRSortBy.ASCENDING);
                sortByList.add(mcrSortBy);
            }
            return sortByList;
        }

        /**
         * Creates the search condition.
         * @return a new search condition
         */
        protected MCRCondition createCondition() {
            MCRFieldDef def = MCRFieldDef.getDef("objectType");
            MCRCondition cond = new MCRQueryCondition(def, "=", getType());
            return cond;
        }

        /**
         * Returns the name of a search hit. It will
         * be compared afterwards to another hits name.
         * @param mcrHit
         * @return
         */
        protected String getName(MCRHit mcrHit) {
            String objectName = "";
            List<MCRFieldValue> fieldValues = mcrHit.getSortData();
            for(int i = 0; i < fieldValues.size(); i++) {
                objectName += fieldValues.get(i).getValue();
                if(i < fieldValues.size() - 1)
                    objectName += ",";
            }
            return objectName;
        }

        /**
         * Checks if the redundancy objects are equal.
         * @param obj1 the first redundancy object
         * @param obj2 the second redundancy object
         * @return if they are equal
         */
        protected boolean areObjectsEqual(RedundancyObject obj1, RedundancyObject obj2) {
            if(obj1 == null || obj2 == null)
                return false;
            if(obj1.getName() == null || obj2.getName() == null)
                return false; 
            return obj1.getName().equals(obj2.getName());
        }

        /**
         * Main method to create the redundancy map.
         */
        public void createRedundancyMap() {
            LOGGER.info("create the redundancy map");
            redundancyMap = new Element("redundancyMap");
            redundancyMap.setAttribute("tableHead", getTableHead());

            // get the search condition
            MCRCondition cond = createCondition();
            // sort the list
            ArrayList<MCRSortBy> sortByList = createSortByList();
            // do search
            MCRQuery query = new MCRQuery(cond, sortByList, 0);
            MCRResults result = MCRQueryManager.search(query);

            RedundancyObject previousRedundancyObject = null;
            Element currentGroupElement = null;
            int groupCount = 0;

            // go through all results
            for (MCRHit mcrHit : result) {
                // get the name of the mcr hit object
                String objectName = getName(mcrHit);
                // set the current redundancy object
                RedundancyObject currentRedundancyObject = new RedundancyObject(mcrHit.getID(), objectName);
                // test if the objects are equal, if true returned they are duplicates
                if(areObjectsEqual(currentRedundancyObject, previousRedundancyObject)) {
                    // there is no existing group element for the duplicates -> create a new group element
                    if(currentGroupElement == null) {
                        currentGroupElement = createGroupElement(groupCount++, objectName);
                        currentGroupElement.addContent(createObjectElement(previousRedundancyObject.getObjId()));
                        redundancyMap.addContent(currentGroupElement);
                    }
                    currentGroupElement.addContent(createObjectElement(currentRedundancyObject.getObjId()));
                } else {
                    // the names are different, so there is no group for those
                    currentGroupElement = null;
                }
                // set the previous element
                previousRedundancyObject = currentRedundancyObject;
            }
            LOGGER.info("redundancy map created");
        }

        /**
         * Saves the redundancy map to the file system.
         * @throws FileNotFoundException
         * @throws IOException
         */
        public void saveToFile() throws FileNotFoundException, IOException {
            if(redundancyMap == null) {
                LOGGER.error("Redundancy map element is null. Execute createRedundancyMap before saving.");
                return;
            }
            // save xml file
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            FileOutputStream output = new FileOutputStream(getFileName());
            outputter.output(new Document(redundancyMap), output);
            LOGGER.info("command successfully finished");
        }

        /**
         * Creates a group as element. Each equal redundancy objects will be stored
         * in one group.
         * @param id the id of the group. its a increasing integer number.
         * @param name the name of the group 
         * @return a new group element
         */
        protected Element createGroupElement(int id, String name) {
            Element groupElement = new Element("redundancyObjects");
            groupElement.setAttribute("id", String.valueOf(id));
            groupElement.setAttribute("name", name);
            return groupElement;
        }
        /**
         * Creates a single object element. This object elements are childs
         * of a group. They only contains the id of an mcr object.
         * @param id the id of the mcr object
         * @return a new object element.
         */
        protected Element createObjectElement(String id) {
            Element object = new Element("object");
            object.setAttribute("objId", id);
            return object;
        }
    }

    /**
     * Redundancy map generator for institutions.
     */
    private static class JPInstRedundancyMapGenerator extends AbstractRedundancyMapGenerator {
        @Override
        protected ArrayList<String> getSortByStringList() {
            ArrayList<String> sortByList = new ArrayList<String>();
            sortByList.add("instname");
            return sortByList;
        }
        @Override
        protected String getTableHead() {
            return "Institution";
        }
        @Override
        protected String getType() {
            return "jpinst";
        }
    }

    /**
     * Redundancy map generator for person.
     */
    public static class PersonRedundancyMapGenerator extends AbstractRedundancyMapGenerator {
        @Override
        protected ArrayList<String> getSortByStringList() {
            ArrayList<String> sortByList = new ArrayList<String>();
            sortByList.add("lastname");
            sortByList.add("firstname");
            return sortByList;
        }
        @Override
        protected String getTableHead() {
            return "Nachname,Vorname";
        }
        @Override
        protected String getType() {
            return "person";
        }
        protected String getName(MCRHit mcrHit) {
            String objectName = "";
            List<MCRFieldValue> fieldValues = mcrHit.getSortData();
            for(int i = 0; i < fieldValues.size(); i++) {
                objectName += fieldValues.get(i).getValue();
                if(i == 0)
                    objectName += ",";
            }
            return objectName;
        }
    }

    /**
     * A redundancy object is defined by an id and a name.
     */
    private static class RedundancyObject {
        private String objId;
        private String name;
        public RedundancyObject(String objId, String name) {
            this.objId = objId;
            this.name = name;
        }
        public String getObjId() {
            return objId;
        }
        public String getName() {
            return name;
        }
    }
}