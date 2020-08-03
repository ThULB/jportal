package fsu.jportal.xml.stream;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import fsu.jportal.xml.JPMCRObjXMLElementName;
import static fsu.jportal.xml.JPMCRObjXMLElementName.child;
import static fsu.jportal.xml.stream.XMLStreamReaderUtils.qNameStr;

/**
 * Created by chi on 17.10.16.
 */
public class ParsedXML {
    private final Map<String, List<Map<String, Optional<String>>>> elementData;

    public ParsedXML(Map<String, List<Map<String, Optional<String>>>> elementData) {
        this.elementData = elementData;
    }

    public Stream<ElementData> element(String localName) {
        return element(null, localName);
    }
    public Stream<ElementData> element(JPMCRObjXMLElementName localName) {
        return element(localName.toString());
    }

    public Stream<ElementData> element(String prefix, String localName) {
        return elementData.getOrDefault(qNameStr(prefix, localName), Collections.emptyList())
                          .stream()
                          .map(map -> newElementData(qNameStr(prefix, localName), map));
    }

    private ElementData newElementData(String elementName, Map<String, Optional<String>> valMap){
        return new ElementData() {
            @Override
            public Stream<String> getAttr(String p, String n) {
                Optional<String> attrVal = valMap.getOrDefault(qNameStr(p, n), Optional.empty());
                return Stream.of(attrVal)
                             .filter(Optional::isPresent)
                             .map(Optional::get);
            }

            @Override
            public Stream<String> getText() {
                Optional<String> text = valMap.getOrDefault("text", Optional.empty());
                return Stream.of(text)
                             .filter(Optional::isPresent)
                             .map(Optional::get);
            }
        };
    }

    public Stream<String> getChildIDs() {
        return element(child).flatMap(elem -> elem.getAttr("xlink", "href"));
    }

    public interface ElementData {
        Stream<String> getAttr(String prefix, String localName);

        Stream<String> getText();

        default Stream<String> getAttr(String localName){
            return getAttr(null, localName);
        }
    }
}
