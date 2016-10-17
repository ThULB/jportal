package fsu.jportal.xml.stream;

import fsu.jportal.xml.JPMCRObjXMLElementName;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by chi on 13.10.16.
 */
public class ParsedMCRObj {
    private String ID;

    private String parentID;

    private ParsedXML parsedXML;

    public Stream<ParsedXML.ElementData> element(String localName) {
        return parsedXML.element(localName);
    }

    public Stream<ParsedXML.ElementData> element(JPMCRObjXMLElementName localName) {
        return parsedXML.element(localName);
    }

    public Stream<ParsedXML.ElementData> element(String prefix, String localName) {
        return parsedXML.element(prefix, localName);
    }

    public Stream<String> getChildIDs() {
        return parsedXML.getChildIDs();
    }

//    private final Map<String, List<Map<String, Optional<String>>>> elementData;

    public ParsedMCRObj(String ID, String parentID, Map<String, List<Map<String, Optional<String>>>> elementData) {
        this.ID = ID;
        this.parentID = parentID;
//        this.elementData = elementData;
        this.parsedXML = new ParsedXML(elementData);
    }

    public String getID() {
        return ID;
    }

    public String getParentID() {
        return isRoot() ? "root" : parentID;
    }

    public boolean isRoot() {
        return parentID == null;
    }


}
