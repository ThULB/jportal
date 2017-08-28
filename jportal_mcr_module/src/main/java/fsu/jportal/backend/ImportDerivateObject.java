package fsu.jportal.backend;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.util.ArrayList;

/**
 * Created by michel on 07.07.15.
 * @author Michel Büchner
 */
public class ImportDerivateObject {
    private String documentID;
    private String derivateID;
    private Document derivateXML;
    private ArrayList<ImportFileObject> children = new ArrayList<ImportFileObject>();

    public ImportDerivateObject (String documentID, String derivateID, Document derivateXML){
        this.documentID = documentID;
        this.derivateID = derivateID;
        this.derivateXML = derivateXML;
    }

    public ImportDerivateObject (String derivateID, Document derivateXML){
        this.documentID = getDocID(derivateXML);
        this.derivateID = derivateID;
        this.derivateXML = derivateXML;
    }

    public void addChild(String path, long size){
        children.add(new ImportFileObject(path, size));
    }

    public String getDocumentID() {
        return documentID;
    }

    public Document getDerivateXML() {
        return derivateXML;
    }

    public ArrayList<ImportFileObject> getChildren() {
        return children;
    }

    public String getDerivateID() {
        return derivateID;
    }

    private String getDocID(Document objXML) {
        Namespace xlink = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");
        XPathExpression<Attribute> idXpath = XPathFactory.instance()
                .compile("/mycorederivate/derivate/linkmetas/linkmeta/@xlink:href", Filters.attribute(), null, xlink);
        return idXpath.evaluateFirst(objXML).getValue();
    }
}
