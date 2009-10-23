package org.mycore.importer.mcrimport;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.ifs.MCRFileImportExport;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.importer.MCRImportConfig;
import org.mycore.importer.classification.MCRImportClassificationMap;
import org.mycore.importer.classification.MCRImportClassificationMappingManager;
import org.mycore.importer.derivate.MCRImportDerivateFileManager;
import org.mycore.importer.event.MCRImportStatusEvent;
import org.mycore.importer.event.MCRImportStatusListener;

/**
 * This class does the import to mycore. Ids assigned by
 * the mapping manager will be replaced by valid mycore ids. If
 * classification mapping is activated the corresponding values
 * are also replaced.
 */
public class MCRImportImporter {

    private static final Logger LOGGER = Logger.getLogger(MCRImportImporter.class);

    private MCRImportConfig config;

    private SAXBuilder builder;

    private Hashtable<String, MCRImportFileStatus> idTable = new Hashtable<String, MCRImportFileStatus>();

    protected ArrayList<MCRImportStatusListener> listenerList;

    protected MCRImportClassificationMappingManager classManager;

    protected MCRImportDerivateFileManager derivateFileManager;

    public MCRImportImporter(File file) throws IOException, JDOMException {
        this.builder = new SAXBuilder();
        Element rootElement = getRootElement(file);
        // get the config from the import xml file
        this.config = new MCRImportConfig(rootElement);
        // create the classification manager
        this.classManager = new MCRImportClassificationMappingManager(new File(config.getSaveToPath() + "classification/"));
        if(this.classManager.getClassificationMapList().isEmpty())
            LOGGER.warn("No classification mapping documents found! Check if the folder 'classification'" +
                        " in the import directory exists and all files ends with '.xml'.");

        if(!classManager.isCompletelyFilled()) {
            LOGGER.error("The following classification mapping keys are not set:");
            for(MCRImportClassificationMap map : classManager.getClassificationMapList()) {
                for(String emptyImportValue : map.getEmptyImportValues())
                    LOGGER.error(" " + emptyImportValue);
            }
            LOGGER.error(   "Before the import can start, all mycore values have to be set or" +
            		        "the classifcation mapping needs to be disabled!");
            return;
        }

        // loads the derivate file manager
        derivateFileManager = new MCRImportDerivateFileManager(new File(config.getSaveToPath() + "derivates/"), false);
        // create the listener list
        this.listenerList = new ArrayList<MCRImportStatusListener>();

        // build the id table
        File mainDirectory = new File(config.getSaveToPath());
        buildIdTable(mainDirectory);
    }

    private Element getRootElement(File file) throws IOException, JDOMException {
        // load the mapping xml file document
        Document document = builder.build(file);
        // set the root element
        return document.getRootElement();
    }

    /**
     * Browses through the specified directory to add a valid import
     * xml file to the id hash table.
     * 
     * @param dir the directory where to search
     */
    protected void buildIdTable(File dir) {
        for(File file : dir.listFiles()) {
            if(file.isDirectory())
                // call this method recursive if its a directory
                buildIdTable(file);
            else if(file.getName().endsWith(".xml")) {
                // if is a valid import file
                Document doc = null;
                try {
                    doc = builder.build(file);
                } catch(Exception exc) {
                    continue;
                }
                Element rE = doc.getRootElement();
                // mycore objects
                if(rE.getName().equals("mycoreobject")) {
                    String importId = rE.getAttributeValue("ID");
                    if(importId == null || importId.equals(""))
                        continue;
                    idTable.put(importId, new MCRImportFileStatus(importId, file.getAbsolutePath(), MCRImportFileType.MCROBJECT));
                } else if(config.isUseDerivates() && config.isImportToMycore() && rE.getName().equals("mycorederivate")) {
                    // derivate objects
                    String importId = rE.getAttributeValue("ID");
                    if(importId == null || importId.equals(""))
                        continue;
                    idTable.put(importId, new MCRImportFileStatus(importId, file.getAbsolutePath(), MCRImportFileType.MCRDERIVATE));
                }
            }
        }
    }

    /**
     * This method starts the import. The whole id table
     * will be passed through and every entry will be imported.
     */
    public void startImport() {
        for(MCRImportFileStatus fs : idTable.values()) {
            // object is already imported to mycore
            if(fs.isImported())
                continue;
            try {
                if(fs.getType().equals(MCRImportFileType.MCROBJECT)) {
                    String mcrId = importMCRObjectByFile(fs.getFilePath());
                    fs.setMycoreId(mcrId);
                }else if(fs.getType().equals(MCRImportFileType.MCRDERIVATE)) {
                    String mcrId = importMCRDerivateByFile(fs.getFilePath());
                    fs.setMycoreId(mcrId);
                    if(config.isImportFilesToMycore())
                        importInternalDerivateFiles(fs);
                }
            } catch(Exception e) {
                LOGGER.error(e);
            }
        }
    }

    /**
     * Imports a mycore object xml file to mycore by its path. All internal imports
     * ids and classifiaction mapping values (if enabled) are resolved.
     * 
     * @param filePath the path of the xml file which should be imported
     * @throws IOException
     * @throws JDOMException
     * @throws MCRActiveLinkException
     */
    protected String importMCRObjectByFile(String filePath) throws IOException, JDOMException, MCRActiveLinkException, URISyntaxException {
        Document doc = builder.build(filePath);
        // resolve links
        resolveLinks(doc);
        // map classification values
        if(config.isCreateClassificationMapping())
            mapClassificationValues(doc);

        // use the xsi:noNamespaceSchemaLocation to get the type
        String type = doc.getRootElement().getAttributeValue("noNamespaceSchemaLocation", MCRConstants.XSI_NAMESPACE);
        if(type == null) {
            LOGGER.error("Couldnt get object type because there is no xsi:noNamespaceSchemaLocation defined for object " + doc.getBaseURI());
            return null;
        }
        type = type.substring(0, type.indexOf('.'));
        // create the next id
        MCRObjectID mcrObjId = new MCRObjectID();
        mcrObjId.setNextFreeId(config.getProjectName() + "_" + type);
        // set the new id in the xml document
        doc.getRootElement().setAttribute("ID", mcrObjId.getId());
        // create a new mycore object
        MCRObject mcrObject = new MCRObject();
        // set the xml part
        mcrObject.setFromJDOM(doc);
        // set a flag that this object was imported
        mcrObject.getService().addFlag("imported");
        // save it to the database
        mcrObject.createInDatastore();

        fireMCRObjectImported(mcrObjId.getId());

        return mcrObjId.getId();
    }

    protected String importMCRDerivateByFile(String filePath) throws IOException, JDOMException, MCRActiveLinkException, URISyntaxException {
        Document doc = builder.build(filePath);
        // resolve links
        resolveLinks(doc);
        // create the next id
        MCRObjectID mcrDerivateId = new MCRObjectID();
        mcrDerivateId.setNextFreeId(config.getProjectName() + "_derivate");
        // set the new id in the xml document
        doc.getRootElement().setAttribute("ID", mcrDerivateId.getId());
        // create the derivate
        MCRDerivate derivate = new MCRDerivate();
        // TODO: setFromJDOM überall verfügbar machen !!!T
        setDerivateFromXML(doc, derivate);
        // set a flag that this object was imported
        derivate.getService().addFlag("imported");
        // save it to the database
        derivate.createInDatastore();

        fireMCRDerivateImported(mcrDerivateId.getId());

        return mcrDerivateId.getId();
    }

    private void setDerivateFromXML(Document doc, MCRDerivate derivate) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLOutputter outputter = new XMLOutputter();
        outputter.output(doc, bos);
        derivate.setFromXML(bos.toByteArray(), false);
    }

    protected void importInternalDerivateFiles(MCRImportFileStatus fs) {
        List<String> pathList = derivateFileManager.getPathListOfDerivate(fs.getImportId());
        for(String path : pathList) {
            MCRFileImportExport.addFiles(new File(path), fs.getMycoreId());
//            MCRFileImportExport.importFiles(new File(path), fs.getMycoreId());
        }
    }

    /**
     * Imports an object by its import id. If no object with this
     * id was found an error occour. 
     * 
     * @param importId id to get the right xml file to import
     * @throws IOException
     * @throws JDOMException
     * @throws MCRActiveLinkException
     */
    protected void importObjectById(String importId) throws IOException, JDOMException, MCRActiveLinkException, URISyntaxException {
        MCRImportFileStatus fs = idTable.get(importId);
        if(fs == null) {
            LOGGER.error("there is no object with the id " + importId + " defined!");
            return;
        }
        String mcrId = null;
        if(fs.getType().equals(MCRImportFileType.MCROBJECT))
            mcrId = importMCRObjectByFile(fs.getFilePath());
        else if(fs.getType().equals(MCRImportFileType.MCRDERIVATE))
            mcrId = importMCRDerivateByFile(fs.getFilePath());
        fs.setMycoreId(mcrId);           
    }

    /**
     * Parses the document to resolve all links. Each linked object
     * will be directly imported to receive the correct mycore id. This
     * id is then set at the href attribute.
     * 
     * @param doc the document where the links have to be resolved
     * @throws IOException
     * @throws JDOMException
     * @throws MCRActiveLinkException
     */
    @SuppressWarnings("unchecked")
    protected void resolveLinks(Document doc) throws IOException, JDOMException, MCRActiveLinkException, URISyntaxException { 
        Iterator<Element> it = doc.getRootElement().getDescendants(new LinkIdFilter());
        while(it.hasNext()) {
            Element linkElement = it.next();
            String linkId = linkElement.getAttributeValue("href", MCRConstants.XLINK_NAMESPACE);
            // try to get the mycore id from the hashtable
            MCRImportFileStatus fs = idTable.get(linkId);
            if(fs == null) {
                LOGGER.error(   "Invalid id " + linkId + " found in file " + doc.getBaseURI() + 
                                " at element " + linkElement.getName() + linkElement.getAttributes());
                continue;
            }
            // if null -> the linked object is currently not imported -> do it
            if(fs.getMycoreId() == null)
                importObjectById(linkId);
            
            // set the new mycoreId
            if(fs.getMycoreId() != null) {
                linkElement.setAttribute("href", fs.getMycoreId(), MCRConstants.XLINK_NAMESPACE);
            } else {
                LOGGER.error("Couldnt resolve reference for link " + linkId + " in " + doc.getBaseURI());
                continue;
            }
        }
    }

    /**
     * Parses the document to map all classification values in
     * the document with the classification mapping files.
     * 
     * @param doc the document where the classifications have to be mapped
     */
    @SuppressWarnings("unchecked")
    protected void mapClassificationValues(Document doc) throws IOException, JDOMException, MCRActiveLinkException {
        Iterator<Element> it = doc.getRootElement().getDescendants(new ClassificationFilter());
        while(it.hasNext()) {
            Element classElement = it.next();
            // classid & categid
            String classId = classElement.getAttributeValue("classid");
            String categId = classElement.getAttributeValue("categid");

            if(classId == null || categId == null || classId.equals("") || categId.equals(""))
                continue;

            // get the mycore value from the classifcation mapping file
            String mcrValue = classManager.getMyCoReValue(classId, categId);

            if(mcrValue == null || mcrValue.equals("") || mcrValue.equals(categId))
                continue;

            // set the new mycore value
            classElement.setAttribute("categid", mcrValue);
        }
    }

    /**
     * Use this method to register a listener and get informed
     * about the import progress.
     * 
     * @param l the listener to add
     */
    public void addStatusListener(MCRImportStatusListener l) {
        listenerList.add(l);
    }

    /**
     * Remove a registerd listener.
     * 
     * @param l the listener to remove
     */
    public void removeStatusListener(MCRImportStatusListener l) {
        listenerList.remove(l);
    }

    /**
     * Sends all registerd listeners that a mycore object is
     * successfully imported in the system.
     * 
     * @param record the record which is mapped
     */
    private void fireMCRObjectImported(String mcrId) {
        for(MCRImportStatusListener l : listenerList) {
            MCRImportStatusEvent e = new MCRImportStatusEvent(this, mcrId);
            l.objectImported(e);
        }
    }

    /**
     * Sends all registerd listeners that a mycore object is
     * successfully imported in the system.
     * 
     * @param record the record which is mapped
     */
    private void fireMCRDerivateImported(String derId) {
        for(MCRImportStatusListener l : listenerList) {
            MCRImportStatusEvent e = new MCRImportStatusEvent(this, derId);
            l.derivateImported(e);
        }
    }
    
    /**
     * Internal filter class which returns only true
     * if the element is a xlink. 
     */
    private class LinkIdFilter implements Filter {
        private static final long serialVersionUID = 1L;

        public boolean matches(Object arg0) {
            // only elements
            if(!(arg0 instanceof Element))
                return false;
            Element e = (Element)arg0;
            Element p = e.getParentElement();
            // check the class attribute of the parent element
            if(p == null || p.getAttributeValue("class") == null || !p.getAttributeValue("class").equals("MCRMetaLinkID"))
                return false;
            // exists a href attribute and if its not empty
            String href = e.getAttributeValue("href", MCRConstants.XLINK_NAMESPACE);
            if(href == null || href.equals(""))
                return false;
            return true;
        }
    }

    /**
     * Internal filter calls which returns only true
     * if the element is a classification.
     */
    private class ClassificationFilter implements Filter {
        private static final long serialVersionUID = 1L;

        public boolean matches(Object arg0) {
            // only elements
            if(!(arg0 instanceof Element))
                return false;
            Element e = (Element)arg0;
            Element p = e.getParentElement();
            // check the class attribute of the parent element
            if(p == null || p.getAttributeValue("class") == null || !p.getAttributeValue("class").equals("MCRMetaClassification"))
                return false;
            return true;
        }
    }
}