package org.mycore.frontend.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.AbstractFilter;
import org.jdom2.filter.Filter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.backend.hibernate.MCRHIBConnection;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRBase;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.frontend.util.DerivateLinkUtil;
import org.xml.sax.SAXException;

import fsu.jportal.backend.DerivateTools;

@MCRCommandGroup(name = "JP Commands")
public class JPortalCommands {

    private static Logger LOGGER = Logger.getLogger(JPortalCommands.class.getName());

    @MCRCommand(help = "Export object XML with id to file: export object {id} to file {name}.", syntax = "export object {0} to file {1}")
    public static void exportBlob(String objectID, String file) throws SAXException, JDOMException, IOException {
        Document objXML = MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(objectID));
        XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
        try {
            xo.output(objXML, new FileOutputStream(new File(file)));
            LOGGER.info("exported blob of object " + objectID + " to " + file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @MCRCommand(help = "Import object XML from file: import object from file {name}", syntax = "import object from file {0}")
    public static void importBlob(String file) {
        Document objXML = null;
        try {
            objXML = new SAXBuilder().build(new File(file));
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String id = objXML.getRootElement().getAttributeValue("ID");
        MCRXMLMetadataManager.instance().update(MCRObjectID.getInstance(id), objXML, new Date());
        LOGGER.info("imported object " + id + " to blob from " + file);
    }

    @MCRCommand(help = "Add derivate link to object: add derivate link {location} to object {id}", syntax = "add derivate link {0} to object {1}")
    public static void addDerivateLink(String link, String id) throws MCRActiveLinkException {
        MCRObjectID mcrId = MCRObjectID.getInstance(id);
        DerivateLinkUtil.setLink(mcrId, link);
    }

    @MCRCommand(help = "Rename file in derivate: rename derivID_1:/path/to/fileName newFileName", syntax = "rename {0} {1}")
    public static void renameFileInIFS(String file, String name) throws Exception {
        DerivateTools.rename(file, name);
    }

    @MCRCommand(help = "Copy file in derivate: copy derivID_1:/path/to/source derivID_2:/path/to/target", syntax = "copy file {0} {1}")
    public static void copyFile(String oldFile, String newFile) {
        DerivateTools.cp(oldFile, newFile);
    }

    @MCRCommand(help = "Copy object", syntax = "copy object {0}")
    public static void copyObject(String id) throws Exception {
        MCRObjectID mcrId = MCRObjectID.getInstance(id);
        MCRBase object = MCRMetadataManager.retrieve(mcrId);
        object.setId(MCRObjectID.getNextFreeId(mcrId.getBase()));
        if (object instanceof MCRObject) {
            MCRMetadataManager.create((MCRObject) object);
        } else {
            MCRMetadataManager.create((MCRDerivate) object);
        }
    }

    @MCRCommand(help = "Move file in derivate: move derivID_1:/path/to/source derivID_2:/path/to/target", syntax = "move {0} {1}")
    public static void move(String oldFile, String newFile) {
        DerivateTools.mv(oldFile, newFile);
    }

    @MCRCommand(help = "Create directories in derivate: mkir derivID:/path/to/newDir", syntax = "mkdir {0}")
    public static void mkdir(String newDir) {
        DerivateTools.mkdir(newDir);
    }
    
    @MCRCommand(help = "Add derivate to object: addDerivate objID /path/to/file(s)", syntax = "addDerivate {0} {1}")
    public static void addDerivate(String objID, String pathStr){
        if(MCRMetadataManager.exists(MCRObjectID.getInstance(objID))){
            Path path = Paths.get(pathStr);
            if(Files.exists(path)){
                if(Files.isDirectory(path)){
                    //loop
                    try {
                        Iterator<Path> childrenIter = Files.newDirectoryStream(path).iterator();
                        String derivateID = null;
                        while (childrenIter.hasNext()) {
                            Path childPath = (Path) childrenIter.next();
                            if(Files.isDirectory(childPath)){
                                
                            }else{
                                InputStream inputStream = Files.newInputStream(childPath);
                                long filesize = Files.size(childPath);
                                derivateID = DerivateTools.uploadFile(inputStream, filesize, objID, derivateID, pathStr);
                            }
                        }
                        
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }else{
                    //load file
                }
            }
        }
    }
    
    private static final Namespace xlink = Namespace.getNamespace("http://www.w3.org/1999/xlink");
    @MCRCommand(syntax = "remove dead links", help = "The command remove dead links.")
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
