package org.mycore.frontend.cli.command;

import java.io.FileOutputStream;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.mycore.common.MCRConfiguration;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.parsers.bool.MCRCondition;
import org.mycore.services.fieldquery.MCRHit;
import org.mycore.services.fieldquery.MCRQuery;
import org.mycore.services.fieldquery.MCRQueryManager;
import org.mycore.services.fieldquery.MCRQueryParser;
import org.mycore.services.fieldquery.MCRResults;

/**
 * Creates a xml file containing all objects of the specified type from the database.
 * This file is necessary for the "find duplicates" command.
 * @author Matthias Eichner
 */
public class MCRCheckForDuplicates {

    private static final Logger LOGGER = Logger.getLogger(MCRCheckForDuplicates.class);
    private static final String FS = System.getProperty("file.seperator", "/");
    private static final String DIR = MCRConfiguration.instance().getString("MCR.doubletFinder") + FS;

    /**
     * The start method.
     * @param type Valid types are "person" and "jpinst".
     * @throws Exception
     */
    public static void createCheckForDuplicatesFile(String type) throws Exception {
        if (type == null) {
            LOGGER.error("No type defined. Please enter a valid type. Valid types are 'person' and 'jpinst'");
            return;
        }

        // File appender for logging
        SimpleLayout layout = new SimpleLayout();
        FileAppender fileAppender = new FileAppender(layout, DIR, false);
        LOGGER.addAppender(fileAppender);

        // get the search condition
        MCRCondition cond = null;
        if (type.equals("person")) {
            cond = new MCRQueryParser().parse(getPersonSearchCondition());
        } else if (type.equals("jpinst")) {
            cond = new MCRQueryParser().parse(getInstitutionSearchCondition());
        }

        if (cond == null) {
            LOGGER.error("Type: " + type + " not defined.");
            return;
        }

        // do search
        MCRQuery query = new MCRQuery(cond);
        MCRResults result = MCRQueryManager.search(query);

        // create xml-file
        Element rootElement = new Element("checkForDuplicates");
        rootElement.setAttribute("type", type);
        rootElement.setAttribute("tableHead", getTableHead(type));
        long count = 0;
        for (MCRHit mcrHit : result) {
            MCRObject mcr = new MCRObject();
            // try to get a valid MCRObject from datastore 
            try {
                mcr.receiveFromDatastore(mcrHit.getID());
            } catch(Exception e) {
                // Exception occurred -> do no not end the cycle
                LOGGER.error("exception occurred while receving mycore object from datastore (" + mcrHit.getID() + "): " + e);
                continue;
            }
            // creates a new element for each object in the db.
            Element objectElement = new Element("object");
            objectElement.setAttribute("objId", mcr.getId().getId());
            rootElement.addContent(objectElement);
            // set the condition information depending on the type 
            if (type.equals("person")) {
                MCRMetaElement meta = mcr.getMetadata().getMetadataElement("def.heading");
                Element lastNameElement = getConditionElementById("lastName", meta);
                Element firstNameElement = getConditionElementById("firstName", meta);
                objectElement.addContent(lastNameElement);
                objectElement.addContent(firstNameElement);
            } else if (type.equals("jpinst")) {
                MCRMetaElement meta = mcr.getMetadata().getMetadataElement("names");
                Element fullnameElement = getConditionElementById("fullname", meta);
                objectElement.addContent(fullnameElement);
            }
            count++;
            // do a simple log
            if (count % 500 == 0)
                LOGGER.info(count + " objects of type " + type + " written in xml file.");
        }
        // writes the xml-file
        Document d = new Document(rootElement);
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        FileOutputStream output = new FileOutputStream(DIR + "checkForDuplicates-" + type + ".xml");
        outputter.output(d, output);
        
        // remove appender
        LOGGER.removeAppender(fileAppender);
    }

    /**
     * Defines the search condition for persons. 
     * @return An element with the search condition.
     */
    protected static Element getPersonSearchCondition() {
        Element element = new Element("boolean");
        element.setAttribute("operator", "and");
        Element condition = new Element("condition");
        condition.setAttribute("field", "objectType");
        condition.setAttribute("operator", "=");
        condition.setAttribute("value", "person");
        element.addContent(condition);
        return element;
    }

    /**
     * Defines the search condition for institution. 
     * @return An element with the search condition.
     */
    protected static Element getInstitutionSearchCondition() {
        Element condition = new Element("condition");
        condition.setAttribute("field", "objectType");
        condition.setAttribute("operator", "=");
        condition.setAttribute("value", "jpinst");
        return condition;
    }

    /**
     * Returns the condition element of a specified id in an meta element.
     * Each type has different conditions. For example a name or a date.
     * This method creates an condition element for the specified type. 
     * @param id The condition id.
     * @param meta The meta element.
     * @return A new condition element.
     */
    protected static Element getConditionElementById(String id, MCRMetaElement meta) {
        Element conditionElement = new Element("condition");
        conditionElement.setAttribute("id", id);
        conditionElement.setAttribute("value", "");
        if (meta != null && meta.size() != 0) {
            Element childElement = meta.getElement(0).createXML().getChild(id);
            if (childElement != null) {
                conditionElement.setAttribute("value", ((Element) childElement).getText());
            }
        }
        return conditionElement;
    }

    /**
     * Each type needs a different table layout, depending on their conditions.
     * @param type The type.
     * @return A new table head as String.
     */
    protected static String getTableHead(String type) {
        if (type.equals("person")) {
            return "Nachname,Vorname";
        } else if (type.equals("jpinst")) {
            return "Institution";
        }
        return null;
    }
}