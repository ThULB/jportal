package org.mycore.frontend.cli.command;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.mycore.datamodel.common.MCRXMLTableManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

/**
 * Contains commands for adding persons and institions in the database.
 * @author Matthias Eichner
 */
public class MCRGenerateObjects {

    private static final Logger LOGGER = Logger.getLogger(MCRGenerateObjects.class);

    /**
     * Generates new persons from a copy of the last person in the database.
     * Only the name of each person will be changed (TestPerson_Nachname_X).
     * @param start The start of X.
     * @param stop The end of X.
     * @throws Exception
     */
    public static void generatePersons(int start, int stop) throws Exception {
        LOGGER.info("Start generation");
        // get last person in db
        MCRObjectID lastMcrID = new MCRObjectID();
        lastMcrID.setNextFreeId("jportal_person");
        lastMcrID.setNumber(lastMcrID.getNumberAsInteger() - 1);
        MCRObject lastObject = new MCRObject();
        lastObject.receiveFromDatastore(lastMcrID);

        // get the xml elements
        Document xmlDoc = lastObject.createXML();
        Element rootElement = xmlDoc.getRootElement();
        Element heading = rootElement.getChild("metadata").getChild("def.heading").getChild("heading");
        Element lastName = heading.getChild("lastName");
        Element firstName = heading.getChild("firstName");

        int id = 0;
        // generate new persons
        for (int i = start; i <= stop; i++) {
            MCRObjectID newMcrID = lastObject.getId();
            newMcrID.setNextFreeId("jportal_person");
            MCRObject newPerson = new MCRObject();
            newPerson.setId(newMcrID);

            // change some xml attributes for the new person
            rootElement.setAttribute("ID", newPerson.getId().getId());
            rootElement.setAttribute("label", newPerson.getId().getId());
            if (lastName != null)
                lastName.setText("TestPerson_Nachname_" + i);
            if (firstName != null)
                firstName.setText("TestPerson_Vorname_" + i);
            newPerson.setFromJDOM(xmlDoc);
            newPerson.createInDatastore();
        }
        LOGGER.info("Generation finished");
    }

    /**
     * Generates new institution from a copy of the last institution in the database.
     * Only the name of each institution will be changed (Testinstitution_X).
     * @param start The start of X.
     * @param stop The end of X.
     * @throws Exception
     */
    public static void generateInstitutions(int start, int stop) throws Exception {
        LOGGER.info("Start generation");
        // get last institution in db
        MCRObjectID lastMcrID = new MCRObjectID();
        lastMcrID.setNextFreeId("jportal_jpinst");
        lastMcrID.setNumber(lastMcrID.getNumberAsInteger() - 1);
        MCRObject lastObject = new MCRObject();
        lastObject.receiveFromDatastore(lastMcrID);

        // get the xml elements
        Document xmlDoc = lastObject.createXML();
        Element rootElement = xmlDoc.getRootElement();
        Element heading = rootElement.getChild("metadata").getChild("names").getChild("name");
        Element name = heading.getChild("fullname");

        int id = 0;
        // generate new institution
        for (int i = start; i <= stop; i++) {
            MCRObjectID newMcrID = lastObject.getId();
            newMcrID.setNextFreeId("jportal_jpinst");
            newMcrID.setNumber(id);
            MCRObject newJpInst = new MCRObject();
            newJpInst.setId(newMcrID);

            // change some xml attributes for the new institution
            rootElement.setAttribute("ID", newJpInst.getId().getId());
            rootElement.setAttribute("label", newJpInst.getId().getId());
            if (name != null)
                name.setText("Testinstitution_" + i);
            newJpInst.setFromJDOM(xmlDoc);
            newJpInst.createInDatastore();
        }
        LOGGER.info("Generation finished");
    }
}
