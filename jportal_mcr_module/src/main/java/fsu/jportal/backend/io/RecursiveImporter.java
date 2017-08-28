package fsu.jportal.backend.io;

import fsu.jportal.backend.ImportDerivateObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by chi on 24.04.15.
 * @author Huu Chi Vu
 */
public class RecursiveImporter {
    private static Logger LOGGER = LogManager.getLogger(RecursiveImporter.class);

    private final ImportSource src;

    private final ImportSink sink;

    private HashSet<String> idsOfImportedObjs = new HashSet<>();
    private HashSet<String> idsOfImportedClassis = new HashSet<>();
    private HashSet<String> idsOfImportedDeris = new HashSet<>();

    private ArrayList<Document> objXMLs = new ArrayList<>();
    private ArrayList<Document> classiXMLs = new ArrayList<>();
    private ArrayList<ImportDerivateObject> deriObjs = new ArrayList<>();

    int classiCount = 0;
    int objCount = 0;
    int deriCount = 0;

    public RecursiveImporter(ImportSource importSource, ImportSink importSink) {
        this.src = importSource;
        this.sink = importSink;

    }

    public ImportSink getSink() {
        return sink;
    }

    public ImportSource getSrc() {
        return src;
    }

    public void start() {
        for (Document objXML : getSrc().getObjs()) {
            importObj(objXML);
        }
        save();
    }

    private void save() {
        LOGGER.info("Count: " + classiCount + " # List: " + classiXMLs.size() + " # Set: " + idsOfImportedClassis.size());
        LOGGER.info("Count: " + objCount + " # List: " + objXMLs.size() + " # Set: " + idsOfImportedObjs.size());
        LOGGER.info("Count: " + deriCount + " # List: " + deriObjs.size() + " # Set: " + idsOfImportedDeris.size());

        for (Document classiXML : classiXMLs) {
            getSink().saveClassification(classiXML);
        }

        for (Document objXML : objXMLs) {
            getSink().save(objXML);
        }

        for (ImportDerivateObject deriObj : deriObjs) {
            getSink().saveDerivate(deriObj);
        }

        getSink().saveDerivateLinks();
    }

    private void importObj(Document objXML) {
        Element metadata = getMetadata(objXML);
        importParticipants(metadata);
        importClassification(metadata);
//        getSink().save(objXML);
        objXMLs.add(objXML);
        Element structure = getStructure(objXML);
        importDerivate(structure);
        importChildren(structure);
    }

    private Document getObjXML(String objID) {
        Document objXML = getSrc().getObj(objID);
        LOGGER.info("Add object " + objID + " to List.");
        return objXML;
    }

    private Element getStructure(Document objXML) {
        XPathExpression<Element> metadataXpath = XPathFactory.instance()
                .compile("/mycoreobject/structure", Filters.element());
        return metadataXpath.evaluateFirst(objXML);
    }

    private Element getMetadata(Document objXML) {
        XPathExpression<Element> metadataXpath = XPathFactory.instance()
                .compile("/mycoreobject/metadata", Filters.element());
        return metadataXpath.evaluateFirst(objXML);
    }

    private void importClassification(Element metaDataXML) {
        XPathExpression<Attribute> classificationXpath = XPathFactory.instance()
                .compile("*[@class='MCRMetaClassification']/*/@classid", Filters.attribute());
        for (Attribute attribute : classificationXpath.evaluate(metaDataXML)) {
            String classID = attribute.getValue();
            if (!idsOfImportedClassis.contains(classID)) {
                idsOfImportedClassis.add(classID);
                getClassificationXML(classID);
                classiCount++;
//                getSink().saveClassification();
            }else{
                LOGGER.info("Classification exists: " + classID);
            }
        }
    }

    private Document getClassificationXML(String classID) {

        Document classiXML = getSrc().getClassification(classID);
        classiXMLs.add(classiXML);
        LOGGER.info("Add classification " + classID + " to List.");
        return classiXML;
    }

    private void importDerivate(Element structureXML) {
        Namespace xlink = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");
        XPathExpression<Attribute> participantsXpath = XPathFactory.instance()
                .compile("derobjects/derobject/@xlink:href", Filters.attribute(), null, xlink);
        for (Attribute objIDAttr : participantsXpath.evaluate(structureXML)) {
            String objID = objIDAttr.getValue();

            if (!idsOfImportedDeris.contains(objID)) {
                idsOfImportedDeris.add(objID);
                getDerivate(objID);
                deriCount++;
            }else{
                LOGGER.info("Derivates exists: " + objID);
            }
        }
    }

    private void getDerivate(String deriID) {
        Document doc = getSrc().getObj(deriID);
        ImportDerivateObject impDeri = new ImportDerivateObject(deriID, doc);
        addDerivateFiles(impDeri, "");
        deriObjs.add(impDeri);
        LOGGER.info("Add derivate " + deriID + " to List.");
    }

    private void addDerivateFiles(ImportDerivateObject derivate, String path) {
        Document derivateFiles = getSrc().getDerivateFiles(derivate.getDerivateID() + path);
        XPathExpression<Element> participantsXpath = XPathFactory.instance()
                .compile("/mcr_directory/children/child", Filters.element());
        for (Element file : participantsXpath.evaluate(derivateFiles)) {
            String name = XPathFactory.instance().compile("name", Filters.element()).evaluateFirst(file).getValue();
            long size = Long.valueOf(XPathFactory.instance().compile("size", Filters.element()).evaluateFirst(file).getValue());
            String type = XPathFactory.instance().compile("@type", Filters.attribute()).evaluateFirst(file).getValue();
            if (type.equals("directory")) {
                addDerivateFiles(derivate, "/" + name);
            }
            else {
                derivate.addChild(path + "/" + name, size);
            }
        }
    }

    private void importParticipants(Element metaDataXML) {
        importObj(metaDataXML, "participants/participant/@xlink:href");
    }

    private void importChildren(Element structureXML) {
        importObj(structureXML, "children/child/@xlink:href");
    }

    private void importObj(Element metaDataXML, String xpath) {
        Namespace xlink = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");
        XPathExpression<Attribute> participantsXpath = XPathFactory.instance()
                .compile(xpath, Filters.attribute(), null, xlink);
        for (Attribute objIDAttr : participantsXpath.evaluate(metaDataXML)) {
            String objID = objIDAttr.getValue();

            if (!idsOfImportedObjs.contains(objID)) {
                idsOfImportedObjs.add(objID);
                importObj(getObjXML(objID));
                objCount++;
            }else{
                LOGGER.info("Object exists: " + objID);
            }
        }
    }


    public void setIdsOfImportedObjs(HashSet<String> idsOfImportedObjs) {
        this.idsOfImportedObjs = idsOfImportedObjs;
    }
}
