package fsu.jportal.xml.stream;

import javax.xml.stream.XMLStreamReader;
import java.util.List;
import java.util.Map;

/**
 * Created by chi on 11.10.16.
 */
public interface DataRetrieval {
    DataRetrieval getAttr(String name);

    DataRetrieval getAttr(String prefix, String name);

    DataRetrieval getText();

    DataRetrieval and(DataRetrieval... childRetrieval);

    List<DataRetrieval> getChildRetrievals();

    String getElementName();

    boolean test(XMLStreamReader reader);

    Map<String, Instruction> getIntructionMap();
}
