package org.mycore.frontend.cli;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.AbstractFilter;
import org.jdom2.filter.Filter;
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
                try {
                    faultyObjDoc = MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(keyMCRFrom));
                    objectXMLs.put(keyMCRFrom, faultyObjDoc);
                } catch(Exception exc) {
                    LOGGER.error("Unable to retrieve object " + keyMCRFrom, exc);
                }
            }
            Filter<Element> hrefFilter = new AbstractFilter<Element>() {
                @Override
                public Element filter(Object content) {
                    if(content instanceof Element) {
                        if(((Element) content).getAttribute("href", xlink) != null)
                            return (Element)content;
                    }
                    return null;
                }
            };
            // filter for tags with an xlink:href
            HashMap<Element, Element> deleteMap = new HashMap<Element, Element>();
            for (Iterator<Element> iterator = faultyObjDoc.getDescendants(hrefFilter); iterator.hasNext();) {
                Element elem = iterator.next();
                String attributeValue = elem.getAttributeValue("href", xlink);

                if (attributeValue.equals(keyMCRTo)) {
                    deleteMap.put(elem.getParentElement(), elem);
                }
            }
            for (Iterator<Element> iterator = deleteMap.keySet().iterator(); iterator.hasNext();) {
                Element key = iterator.next();
                key.removeContent((Element) deleteMap.get(key));
                if (key.getChildren().isEmpty()) {
                    key.getParent().removeContent(key);
                }
            }

            Element maintitlesElem = faultyObjDoc.getRootElement().getChild("metadata").getChild("maintitles");
            StringBuilder maintitle = new StringBuilder();

            for (Iterator<Element> iterator = maintitlesElem.getChildren().iterator(); iterator.hasNext();) {
                Element partOfTitle = iterator.next();
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
