package fsu.jportal.xml.stream;

import fsu.jportal.xml.JPMCRObjXMLElementName;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.*;

/**
 * Created by chi on 21.09.16.
 *
 * @author Huu Chi Vu
 */
public class XMLStreamReaderUtils {
    public static Stream<XMLStreamReader> toStream(XMLStreamReader reader) {
        return toStream(reader, r -> false);
    }

    public static Stream<XMLStreamReader> toStream(XMLStreamReader reader, Predicate<XMLStreamReader> limit) {
        final Iterator<XMLStreamReader> readerIterator = new Iterator<XMLStreamReader>() {
            @Override
            public boolean hasNext() {
                try {
                    boolean test = limit.test(reader);
                    return reader.hasNext() && !test;
                } catch (XMLStreamException e) {
                    e.printStackTrace();
                }

                return false;
            }

            @Override
            public XMLStreamReader next() {
                try {
                    reader.next();
                    return reader;
                } catch (XMLStreamException e) {
                    e.printStackTrace();
                }

                throw new NoSuchElementException();
            }
        };

        return StreamSupport
                .stream(Spliterators
                                .spliteratorUnknownSize(readerIterator, Spliterator.ORDERED | Spliterator.SUBSIZED),
                        false);
    }

    protected static class DataRetrievalImpl implements DataRetrieval {
        private final String elementName;
        private final Predicate<XMLStreamReader> predicate;
        private final Map<String, Instruction> getAttrValInstructions;
        private Instruction getTextInstruction;
        private List<DataRetrieval> childRetrievals;

        private DataRetrievalImpl(String elementName, Predicate<XMLStreamReader> predicate) {
            this.elementName = elementName;
            this.predicate = predicate;
            this.getAttrValInstructions = new HashMap<>();
            this.childRetrievals = new LinkedList<>();
        }

        @Override
        public DataRetrieval getAttr(String name) {
            return getAttr(null, name);
        }

        private Optional<String> getAttr(XMLStreamReader reader, String prefix, String localname) {
            String uri = getUri(reader, prefix);
            return Optional.ofNullable(reader.getAttributeValue(uri, localname));
        }

        @Override
        public DataRetrieval getAttr(String prefix, String localName) {
            this.getAttrValInstructions.put(qNameStr(prefix, localName), reader -> getAttr(reader, prefix, localName));
            return this;
        }

        private Optional<String> getTextInstruction(XMLStreamReader reader) {
            try {
                return Optional.of(reader.getElementText());
            } catch (XMLStreamException e) {
                // return Optional.empty() instead
            }

            return Optional.empty();
        }

        @Override
        public DataRetrieval getText() {
            this.getTextInstruction = this::getTextInstruction;
            return this;
        }

        private DataRetrieval wrapChild(DataRetrieval child) {
            return new DataRetrieval() {
                public DataRetrieval getAttr(String name) {
                    return child.getAttr(name);
                }

                public DataRetrieval getAttr(String prefix, String name) {
                    return child.getAttr(prefix, name);
                }

                public DataRetrieval getText() {
                    return child.getText();
                }

                public DataRetrieval and(DataRetrieval... childRetrieval) {
                    return child.and(childRetrieval);
                }

                public List<DataRetrieval> getChildRetrievals() {
                    return child.getChildRetrievals();
                }

                public String getElementName() {
                    return elementName + "/" + child.getElementName();
                }

                public boolean test(XMLStreamReader reader) {
                    return child.test(reader);
                }

                public Map<String, Instruction> getIntructionMap() {
                    return child.getIntructionMap();
                }
            };
        }

        @Override
        public DataRetrieval and(DataRetrieval... childRetrievals) {
            Arrays.stream(childRetrievals)
                  .map(this::wrapChild)
                  .collect(() -> this.childRetrievals, List::add, List::addAll);

            return this;
        }

        @Override
        public String getElementName() {
            return elementName;
        }

        @Override
        public boolean test(XMLStreamReader reader) {
            return predicate.test(reader);
        }

        @Override
        public Map<String, Instruction> getIntructionMap() {
            LinkedHashMap<String, Instruction> textInstMap = new LinkedHashMap<>();
            Optional.ofNullable(getTextInstruction)
                    .ifPresent(i -> textInstMap.put("text", i));

            return Stream.of(getAttrValInstructions, textInstMap)
                         .filter(l -> !l.isEmpty())
                         .collect(LinkedHashMap::new,
                                 Map::putAll,
                                 (BiConsumer<Map<String, Instruction>, Map<String, Instruction>>) Map::putAll);
        }

        public List<DataRetrieval> getChildRetrievals() {
            return childRetrievals;
        }
    }

    public static Map<String, List<Map<String, Optional<String>>>> parse(Stream<XMLStreamReader> readerStream,
                                                                         DataRetrieval... instructions) {
        return readerStream.map(r -> parse(instructions).apply(r))
                           .filter(map -> !map.isEmpty())
                           .map(Map::entrySet)
                           .flatMap(Set::stream)
                           .collect(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, toList())));
    }

    private static Stream<DataRetrieval> flatten(List<DataRetrieval> instructions) {
        return Stream.concat(instructions.stream(), instructions
                .stream()
                .map(DataRetrieval::getChildRetrievals)
                .flatMap(XMLStreamReaderUtils::flatten));
    }

    private static Map<String, Map<String, Optional<String>>> eval(DataRetrieval inst, XMLStreamReader reader) {
        Map<String, Map<String, Optional<String>>> evalRootInst = new HashMap<>();
        evalRootInst.put(inst.getElementName(), evalInstruction(inst, reader));

        Predicate<XMLStreamReader> limit = r -> r.isEndElement() && r.getLocalName().equals(inst.getElementName());

        Function<List<DataRetrieval>, Map<String, Map<String, Optional<String>>>> evalChildren = children -> toStream(reader, limit)
                .map(r -> evalChildren(children, r))
                .reduce(evalRootInst, (m1, m2) -> {
                    m1.putAll(m2);
                    return m1;
                });

        return Optional.of(inst.getChildRetrievals())
                       .filter(list -> !list.isEmpty())
                       .map(evalChildren)
                       .orElse(evalRootInst);
    }

    private static Map<String, Map<String, Optional<String>>> evalChildren(List<DataRetrieval> children, XMLStreamReader reader) {
        return flatten(children)
                .filter(inst -> inst.test(reader))
                .collect(toMap(DataRetrieval::getElementName, inst -> evalInstruction(inst, reader)));
    }

    public static Function<XMLStreamReader, Map<String, Map<String, Optional<String>>>> parse(DataRetrieval... instructions) {
        return reader -> Arrays.stream(instructions)
                               .filter(inst -> inst.test(reader))
                               .map(inst -> eval(inst, reader))
                               .collect(HashMap::new, Map::putAll, Map::putAll);
    }

    public static DataRetrieval matchElement(JPMCRObjXMLElementName elementName, Predicate<XMLStreamReader>... predicates) {
        return matchElement(null, elementName.toString(), predicates);
    }

    public static DataRetrieval matchElement(String elementName, Predicate<XMLStreamReader>... predicates) {
        return matchElement(null, elementName, predicates);
    }

    public static DataRetrieval matchElement(String prefix, String localname, Predicate<XMLStreamReader>... predicates) {
        Predicate<XMLStreamReader> isStartElement = XMLStreamReader::isStartElement;
        Predicate<XMLStreamReader> hasElementName = reader -> reader.getLocalName().equals(localname);
        Predicate<XMLStreamReader> hasPrefix = reader -> Optional.ofNullable(prefix)
                                                                 .map(reader.getPrefix()::equals)
                                                                 .orElse(true);

        Predicate<XMLStreamReader> mergedPredicates = Arrays
                .stream(predicates)
                .reduce(isStartElement.and(hasPrefix).and(hasElementName), Predicate::and);

        return new DataRetrievalImpl(qNameStr(prefix, localname), mergedPredicates);
    }

    public interface AttributePredicates {

        Predicate<XMLStreamReader> isPresent();

        Predicate<XMLStreamReader> hasValue(String value);

    }

    public static Predicate<XMLStreamReader> hasType(String value) {
        return at("type").hasValue(value);
    }

    public static Predicate<XMLStreamReader> isInherited(String value) {
        return at("inherited").hasValue(value);
    }

    public static AttributePredicates at(String attrName) {
        return at(null, attrName);
    }

    public static AttributePredicates at(String prefix, String attrName) {
        return new AttributePredicates() {
            @Override
            public Predicate<XMLStreamReader> isPresent() {
                return reader -> Optional.ofNullable(reader)
                                         .map(r -> r.getAttributeValue(getUri(r, prefix), attrName))
                                         .isPresent();
            }

            @Override
            public Predicate<XMLStreamReader> hasValue(String value) {
                return reader -> Optional.ofNullable(reader)
                                         .map(r -> r.getAttributeValue(getUri(r, prefix), attrName))
                                         .map(v -> v.equals(value))
                                         .orElse(false);
            }
        };
    }

    private static Map<String, Optional<String>> evalInstruction(DataRetrieval instruction, XMLStreamReader reader) {
        return instruction
                .getIntructionMap()
                .entrySet()
                .stream()
                .collect(toMap(Map.Entry::getKey, entry -> entry.getValue().apply(reader)));
    }

    public static String qNameStr(String prefix, String localname) {
        Objects.nonNull(localname);
        return Optional.ofNullable(prefix)
                       .map(p -> p + ":" + localname)
                       .orElse(localname);
    }

    public static String getUri(XMLStreamReader reader, String prefix) {
        return Optional.ofNullable(prefix)
                       .map(reader::getNamespaceURI)
                       .orElse(null);
    }
}
