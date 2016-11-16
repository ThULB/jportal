package fsu.jportal.xml.stream;

import javax.xml.stream.XMLStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/**
 * Created by chi on 04.10.16.
 *
 * @author Huu Chi Vu
 */
public class ParserUtils {

    private static Logger LOGGER = LogManager.getLogger();
    
    public static long CREATE_OBJECT_TIME = 0;
    
    public interface ObjectSupplier<T> {
        ParserSupplier<T> from(Function<String, Optional<XMLStreamReader>> supplier);
    }

    public interface ParserSupplier<T> {
        T parseDataUsing(Function<XMLStreamReader, Map<String, Map<String, Optional<String>>>> xmlStreamReaderMapFunction);
    }

    public static ObjectSupplier<Stream<ParsedMCRObj>> getObjectWithChildrenFor(
            ParsedMCRObj rootObj) {
        return supplier -> parser -> createChildren(rootObj, supplier, parser);
    }

    public static ObjectSupplier<Stream<ParsedMCRObj>> getObjectWithChildrenFor(String id) {
        return supplier -> parser -> createChildren(id, null, supplier, parser);
    }

    public static ObjectSupplier<ParsedMCRObj> getXMLForObj(String id) {
        return getXMLForObj(id, null);
    }

    public static ObjectSupplier<ParsedMCRObj> getXMLForObj(String id, String parentID) {
        return supplier -> parser -> createObj(id, parentID, supplier, parser);

    }

    private static ParsedMCRObj createObj(String id, String parentId,
                                          Function<String, Optional<XMLStreamReader>> supplier,
                                          Function<XMLStreamReader, Map<String, Map<String, Optional<String>>>> parser) {
        
        long startTime = System.currentTimeMillis();
        try {
            Map<String, List<Map<String, Optional<String>>>> valMap = supplier
                    .apply(id)
                    .map(XMLStreamReaderUtils::toStream)
                    .orElseGet(Stream::empty)
                    .map(parser)
                    .filter(map -> !map.isEmpty())
                    .map(Map::entrySet)
                    .flatMap(Set::stream)
                    .collect(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, toList())));
    
            return new ParsedMCRObj(id, parentId, valMap);
        } finally {
            CREATE_OBJECT_TIME += (System.currentTimeMillis() - startTime);
//            LOGGER.info("createObj (" + id + ") " + (System.currentTimeMillis() - startTime) + "ms");
        }
    }

    private static Stream<ParsedMCRObj> createChildren(ParsedMCRObj rootObj,
                                                       Function<String, Optional<XMLStreamReader>> supplier,
                                                       Function<XMLStreamReader, Map<String, Map<String, Optional<String>>>> parser) {
        return Stream.concat(Stream.of(rootObj),
                rootObj.getChildIDs()
                       .map(childId -> createObj(childId, rootObj.getID(), supplier, parser))
                       .flatMap(childObj -> createChildren(childObj, supplier, parser))
        );
    }

    private static Stream<ParsedMCRObj> createChildren(String id, String parentId,
                                                       Function<String, Optional<XMLStreamReader>> supplier,
                                                       Function<XMLStreamReader, Map<String, Map<String, Optional<String>>>> parser) {
        ParsedMCRObj rootObj = createObj(id, parentId, supplier, parser);
        return createChildren(rootObj, supplier, parser);
    }
}
