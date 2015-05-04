package fsu.jportal.backend.io;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.util.HashSet;

/**
 * Created by chi on 24.04.15.
 * @author Huu Chi Vu
 */
public class RecursiveImporter {

    private final ImportSource src;

    private final ImportSink sink;

    private HashSet<String> idsOfImportedObjs;

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
    }

    private void importObj(Document objXML) {
        Element metadata = getMetadata(objXML);
        importParticipants(metadata);
        importClassification(metadata);
        getSink().save(objXML);
        Element structure = getStructure(objXML);
        importChildren(structure);
    }

    private void importChildren(Element structureXML) {
        Namespace xlink = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");
        XPathExpression<Attribute> participantsXpath = XPathFactory.instance()
                .compile("children/child/@xlink:href", Filters.attribute(), null, xlink);
        for (Attribute objIDAttr : participantsXpath.evaluate(structureXML)) {
            String objID = objIDAttr.getValue();
            setIdsOfImportedObjs(new HashSet<String>());
            if (!getIdsOfImportedObjs().contains(objID)) {
                getIdsOfImportedObjs().add(objID);
                importObj(getSrc().getObj(objID));
            }
        }
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
            if (!getIdsOfImportedObjs().contains(classID)) {
                getIdsOfImportedObjs().add(classID);
                getSink().saveClassification(getSrc().getClassification(classID));
            }
        }
    }

    public HashSet<String> getIdsOfImportedObjs() {
        if(idsOfImportedObjs == null){
            setIdsOfImportedObjs(new HashSet<String>());
        }

        return idsOfImportedObjs;
    }

    private void importParticipants(Element metaDataXML) {
        Namespace xlink = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");
        XPathExpression<Attribute> participantsXpath = XPathFactory.instance()
                .compile("participants/participant/@xlink:href", Filters.attribute(), null, xlink);
        for (Attribute objIDAttr : participantsXpath.evaluate(metaDataXML)) {
            String objID = objIDAttr.getValue();

            if (!getIdsOfImportedObjs().contains(objID)) {
                getIdsOfImportedObjs().add(objID);
                importObj(getSrc().getObj(objID));
            }
        }
    }

    public void setIdsOfImportedObjs(HashSet<String> idsOfImportedObjs) {
        this.idsOfImportedObjs = idsOfImportedObjs;
    }
}
