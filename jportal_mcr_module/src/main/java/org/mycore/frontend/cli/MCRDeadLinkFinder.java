package org.mycore.frontend.cli;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.Filter;
import org.mycore.backend.hibernate.MCRHIBConnection;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

public class MCRDeadLinkFinder extends MCRAbstractCommands {
    private static final Namespace xlink = Namespace.getNamespace("http://www.w3.org/1999/xlink");
    private static Logger LOGGER = Logger.getLogger(MCRDeadLinkFinder.class.getName());

    public MCRDeadLinkFinder() {
        super();
        addCommand(new MCRCommand("remove dead links", "org.mycore.frontend.cli.MCRDeadLinkFinder.findDeadLinks", "The command remove dead links."));
    }

    public static void findDeadLinks() {
        LOGGER.info("Find dead Links");
        Session session = MCRHIBConnection.instance().getSession();

        // the SQL query check, if there are references to non existing objects
        @SuppressWarnings("unchecked")
        List<String[]> list = session.createQuery("select key.mcrfrom,key.mcrto from MCRLINKHREF where key.mcrto not in (select key.id from MCRXMLTABLE)").list();

        if (list.isEmpty()) {
            LOGGER.info("No dead links found.");
        }

        HashMap<String, Document> objectXMLs = new HashMap<String, Document>();
        for (Iterator<String[]> iter = list.iterator(); iter.hasNext();) {
            String[] resultArray = iter.next();
            String keyMCRFrom = resultArray[0];
            String keyMCRTo = resultArray[1];

            Document faultyObjDoc = null;

            if (objectXMLs.containsKey(keyMCRFrom)) {
                faultyObjDoc = objectXMLs.get(keyMCRFrom);
            } else {
                // retrieve the object with the faulty reference
                // LOGGER.info("object: " + keyMCRFrom);
                faultyObjDoc = MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(keyMCRFrom));
                objectXMLs.put(keyMCRFrom, faultyObjDoc);
            }

            // filter for tags with an xlink:href
            Filter elementFilter = new Filter() {
                public boolean matches(Object obj) {
                    if (obj instanceof Element) {
                        Element elem = (Element) obj;
                        String attributeValue = elem.getAttributeValue("href", xlink);

                        if (attributeValue != null) {
                            return true;
                        }
                    }
                    return false;
                }
            };

            HashMap deleteMap = new HashMap();

            for (Iterator iterator = faultyObjDoc.getDescendants(elementFilter); iterator.hasNext();) {
                Element elem = (Element) iterator.next();
                String attributeValue = elem.getAttributeValue("href", xlink);

                if (attributeValue.equals(keyMCRTo)) {
                    deleteMap.put(elem.getParent(), elem);
                }
            }

            for (Iterator iterator = deleteMap.keySet().iterator(); iterator.hasNext();) {
                Element key = (Element) iterator.next();
                key.removeContent((Element) deleteMap.get(key));

                if (key.getChildren().isEmpty())
                    key.getParent().removeContent(key);
            }

            Element maintitlesElem = faultyObjDoc.getRootElement().getChild("metadata").getChild("maintitles");
            StringBuilder maintitle = new StringBuilder();

            for (Iterator iterator = maintitlesElem.getChildren().iterator(); iterator.hasNext();) {
                Element partOfTitle = (Element) iterator.next();

                maintitle.append(partOfTitle.getText());

                if (iterator.hasNext()) {
                    maintitle.append(" | ");
                }
            }

            MCRXMLMetadataManager.instance().update(MCRObjectID.getInstance(keyMCRFrom), faultyObjDoc, new Date());

            // XML holen, fehlerhafte Links löschen
            LOGGER.info(maintitle.toString() + ": " + keyMCRFrom + " link with " + keyMCRTo);
        }
    }
}
