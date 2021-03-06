package fsu.jportal.xml.dfg.oai;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static fsu.jportal.xml.JPMCRObjXMLElementName.child;
import static fsu.jportal.xml.JPMCRObjXMLElementName.component;
import static fsu.jportal.xml.JPMCRObjXMLElementName.date;
import static fsu.jportal.xml.JPMCRObjXMLElementName.derivateLink;
import static fsu.jportal.xml.JPMCRObjXMLElementName.derobject;
import static fsu.jportal.xml.JPMCRObjXMLElementName.identi;
import static fsu.jportal.xml.JPMCRObjXMLElementName.journalType;
import static fsu.jportal.xml.JPMCRObjXMLElementName.keyword;
import static fsu.jportal.xml.JPMCRObjXMLElementName.language;
import static fsu.jportal.xml.JPMCRObjXMLElementName.maintitle;
import static fsu.jportal.xml.JPMCRObjXMLElementName.mycoreobject;
import static fsu.jportal.xml.JPMCRObjXMLElementName.note;
import static fsu.jportal.xml.JPMCRObjXMLElementName.participant;
import static fsu.jportal.xml.JPMCRObjXMLElementName.servdate;
import static fsu.jportal.xml.JPMCRObjXMLElementName.size;
import static fsu.jportal.xml.JPMCRObjXMLElementName.subtitle;
import static fsu.jportal.xml.dfg.oai.DmdSec.dmdSecXMLFragment;
import fsu.jportal.xml.stream.DerivateFileInfo;
import fsu.jportal.xml.stream.ParsedMCRObj;
import fsu.jportal.xml.stream.ParsedXML.ElementData;
import fsu.jportal.xml.stream.ParserUtils;
import static fsu.jportal.xml.stream.XMLStreamReaderUtils.at;
import static fsu.jportal.xml.stream.XMLStreamReaderUtils.hasType;
import static fsu.jportal.xml.stream.XMLStreamReaderUtils.isInherited;
import static fsu.jportal.xml.stream.XMLStreamReaderUtils.matchElement;
import static fsu.jportal.xml.stream.XMLStreamReaderUtils.parse;
import static fsu.jportal.xml.stream.XMLStreamWriterUtils.attr;
import static fsu.jportal.xml.stream.XMLStreamWriterUtils.defaultNamespace;
import static fsu.jportal.xml.stream.XMLStreamWriterUtils.document;
import static fsu.jportal.xml.stream.XMLStreamWriterUtils.element;
import static fsu.jportal.xml.stream.XMLStreamWriterUtils.namespace;
import static fsu.jportal.xml.stream.XMLStreamWriterUtils.text;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * Created by chi on 09.10.16.
 */
public class DFGOAIMetsXMLCreator {
    
    private static Logger LOGGER = LogManager.getLogger();

    private static Function<XMLStreamReader, Map<String, Map<String, Optional<String>>>> xmlStreamParser() {
        return parse(
                matchElement(mycoreobject).getAttr("ID"),
                matchElement(child).getAttr("xlink", "href"),
                matchElement(derobject).getAttr("xlink", "href"),
                matchElement(maintitle, isInherited("0")).getText(),
                matchElement(subtitle, isInherited("0")).getText(),
                matchElement(date, isInherited("0")).getAttr("type").getText(),
                matchElement(note, hasType("annotation"), isInherited("0")).getText(),
                matchElement(language, isInherited("0")).getAttr("categid"),
                matchElement(size).getText(),
                matchElement(keyword).getText(),
                matchElement(journalType, at("classid").hasValue("jportal_class_00000200"))
                        .getAttr("categid"),
                matchElement(participant).getAttr("xlink", "href")
                                         .getAttr("type"),
                matchElement(derivateLink).getAttr("xlink", "href"),
                matchElement(identi, hasType("urn")).getAttr("type")
                                                    .getText(),
                matchElement(servdate).getText(),
                matchElement(component).getAttr("classid").getAttr("categid")
        );
    }

    public static Consumer<XMLStreamWriter> oaiRecord(String rootID,
                                                      String oaiIdentifier,
                                                      Function<String, Optional<XMLStreamReader>> objSupplier,
                                                      Function<String, Stream<DerivateFileInfo>> derivateSupplier,
                                                      UnaryOperator<String> fileSectionHref
                                                      ) {

        long startTime = System.currentTimeMillis();

        ParsedMCRObj rootObj = ParserUtils
                .getXMLForObj(rootID)
                .from(objSupplier)
                .parseDataUsing(xmlStreamParser());

        LOGGER.info("getXMLForObj (" + rootID + ") " + (System.currentTimeMillis() - startTime) + "ms");
        startTime = System.currentTimeMillis();
        
        List<ParsedMCRObj> rootObjectWithChildren = ParserUtils
                .getObjectWithChildrenFor(rootObj)
                .from(objSupplier)
                .parseDataUsing(xmlStreamParser())
                .collect(toList());

        LOGGER.info("getObjectWithChildrenFor (" + rootID + ") " + (System.currentTimeMillis() - startTime) + "ms");
        startTime = System.currentTimeMillis();

        List<DerivateFileInfo> fileInfos = rootObj
                .element(derobject)
                .flatMap(o -> o.getAttr("xlink", "href"))
                .flatMap(derivateSupplier)
                .filter(file -> !file.getFileName().equals("mets.xml"))
                .collect(toList());

        LOGGER.info("fileInfos (" + rootID + ") " + (System.currentTimeMillis() - startTime) + "ms");
        startTime = System.currentTimeMillis();

        try {
            return document(
                    defaultNamespace("http://www.openarchives.org/OAI/2.0/"),
                    namespace("mets", "http://www.loc.gov/METS/"),
                    namespace("mods", "http://www.loc.gov/mods/v3"),
                    namespace("xlink", "http://www.w3.org/1999/xlink"),
                    element("record",
                            element("header",
                                    element("identifier",
                                            text("oai:" + oaiIdentifier + ":" + rootObj.getID())
                                    ),
                                    rootObj.element(component)
                                           .map(e -> Stream.of("classid", "categid")
                                                           .flatMap(e::getAttr)
                                                           .collect(Collectors.joining(":")))
                                           .map(id -> element("setSpec", text(id)))
                                           .reduce(Consumer::andThen)
                                           .orElse(noSetSpec -> {}),
    
                                    rootObj.element(servdate)
                                           .flatMap(ElementData::getText)
                                           .map(t -> t.substring(0, 9))
                                           .map(date -> element("datestamp", text(date)))
                                           .reduce(Consumer::andThen)
                                           .orElse(noDatestamp -> {})
                            ),
                            element("metadata",
                                    element("mets", "mets",
                                            dmdSecXMLFragment(rootObjectWithChildren, objSupplier),
                                            amdSecXMLFragment(rootObjectWithChildren),
                                            fileSecXMLFragment(fileInfos, fileSectionHref),
                                            structMapPhysXMLFragment(rootObj, fileInfos),
                                            structMapLogXMLFragment(rootObjectWithChildren),
                                            structLinkXMLFragment(rootObjectWithChildren, fileInfos)
                                    )
                            )
                    )
            );
        } finally {
            LOGGER.info("document (" + rootID + ") " + (System.currentTimeMillis() - startTime) + "ms");
        }
    }

    public static Consumer<XMLStreamWriter> amdSecXMLFragment(List<ParsedMCRObj> rootObjectWithChildren) {
        return rootObjectWithChildren
                .stream()
                .map(obj -> element("mets", "amdSec", attr("ID", "amd_" + obj.getID())))
                .reduce(Consumer::andThen)
                .orElse(noAmdSec -> {});

    }

    public static Consumer<XMLStreamWriter> fileSecXMLFragment(List<DerivateFileInfo> fileInfos,
                                                               UnaryOperator<String> fileSectionHref) {
        return element("mets", "fileSec",
                       element("mets", "fileGrp", attr("USE", "DEFAULT"), fileInfos
                               .stream()
                               .map(fileInfo ->
                                            element("mets", "file",
                                                    attr("ID", "DEFAULT_" + fileInfo.getUuid()),
                                                    attr("MIMETYPE", "image/jpeg"),
                                                    element("mets", "FLocat",
                                                            attr("LOCTYPE", "URL"),
                                                            attr("xlink", "href", fileSectionHref.apply(fileInfo.getUri()))
                                                    )
                                            )
                               ).reduce(Consumer::andThen)
                               .orElse(noFiles -> {})
                       )
        );
    }

    public static Consumer<XMLStreamWriter> structMapPhysXMLFragment(ParsedMCRObj rootObj,
                                                                     List<DerivateFileInfo> fileInfos) {
        return element("mets", "structMap", attr("TYPE", "PHYSICAL"),
                       element("mets", "div",
                               attr("ID", rootObj.element(derobject)
                                                 .flatMap(o -> o.getAttr("xlink", "href"))
                                                 .map("phys_jportal_derivate_"::concat)
                                                 .findFirst().orElse("noDerivateID")),

                               attr("TYPE", "physSequence"),

                               fileInfos.stream()
                                        .map(fileInfo ->
                                                     element("mets", "div",
                                                             attr("ID", "phys_master_" + fileInfo.getUuid()),
                                                             attr("TYPE", "page"),
                                                             element("mets", "fptr",
                                                                     attr("FILEID", "DEFAULT_" + fileInfo.getUuid())
                                                             )
                                                     )

                                        ).reduce(Consumer::andThen)
                                        .orElse(noFiles -> {})

                       )
        );
    }

    public static Consumer<XMLStreamWriter> structMapLogXMLFragment(List<ParsedMCRObj> rootObjectWithChildren) {
        Map<String, List<ParsedMCRObj>> childrenMap = rootObjectWithChildren
                .stream()
                .collect(groupingBy(ParsedMCRObj::getParentID));

        return element("mets", "structMap", attr("TYPE", "LOGICAL"),
                       structMapLogXMLFragment("root", childrenMap)
        );
    }

    private static Consumer<XMLStreamWriter> structMapLogXMLFragment(String parentID,
                                                                     Map<String, List<ParsedMCRObj>> childrenMap) {
        UnaryOperator<String> getType = id -> Stream
                .of("jpjournal", "jpvolume", "jparticle")
                .filter(id::contains)
                .findFirst()
                .orElse("notSupportedType");

        return childrenMap.getOrDefault(parentID, Collections.emptyList())
                          .stream()
                          .map(ParsedMCRObj::getID)
                          .map(id ->
                                       element("mets", "div",
                                               attr("DMDID", "dmd_" + id),
                                               attr("ADMID", "amd_" + id),
                                               attr("ID", id),
                                               attr("TYPE", getType.apply(id)),
                                               structMapLogXMLFragment(id, childrenMap)
                                       ))
                          .reduce(Consumer::andThen)
                          .orElse(noChildElements -> {});
    }

    private interface UUIDMcrObj {
        String getUUID();

        String getObjID();
    }

    private static class UUIDToMcrObjIDMapper {
        private Map<String, String> uuidToMcrObjIDMap;

        private String currentMCRObjID;

        public UUIDToMcrObjIDMapper(String currentMCRObjID) {
            this.currentMCRObjID = currentMCRObjID;
            uuidToMcrObjIDMap = new LinkedHashMap<>();
        }

        public String getCurrentMCRObjID() {
            return currentMCRObjID;
        }

        public void add(String uuid, String objID) {
            currentMCRObjID = objID;
            uuidToMcrObjIDMap.put(uuid, objID);
        }

        public Stream<UUIDMcrObj> stream() {
            return uuidToMcrObjIDMap.entrySet().stream().<UUIDMcrObj>map(entry -> new UUIDMcrObj() {
                @Override
                public String getUUID() {
                    return entry.getKey();
                }

                @Override
                public String getObjID() {
                    return entry.getValue();
                }
            });
        }
    }

    public static Consumer<XMLStreamWriter> structLinkXMLFragment(List<ParsedMCRObj> rootObjectWithChildren,
                                                                  List<DerivateFileInfo> fileInfos) {
        Function<String, Predicate<ParsedMCRObj>> objHasLinkToFile = fileName -> obj -> obj
                .element(derivateLink)
                .flatMap(o -> o.getAttr("xlink", "href"))
                .filter(link -> link.contains(fileName))
                .findFirst()
                .isPresent();

        BiFunction<DerivateFileInfo, String, String> findObjID = (fileInfo, currentID) -> rootObjectWithChildren
                .stream()
                .filter(objHasLinkToFile.apply(fileInfo.getFileName()))
                .map(ParsedMCRObj::getID)
                .findFirst()
                .orElse(currentID);

        Supplier<UUIDToMcrObjIDMapper> sup = () -> rootObjectWithChildren
                .stream()
                .map(ParsedMCRObj::getID)
                .map(UUIDToMcrObjIDMapper::new)
                .findFirst()
                .orElseGet(() -> new UUIDToMcrObjIDMapper("noRoot"));

        BiConsumer<UUIDToMcrObjIDMapper, DerivateFileInfo> accu = (mapper, fileInfo) -> {
            String objID = findObjID.apply(fileInfo, mapper.getCurrentMCRObjID());
            mapper.add(fileInfo.getUuid(), objID);
        };
        BiConsumer<UUIDToMcrObjIDMapper, UUIDToMcrObjIDMapper> comb = (m1, m2) -> {};

        return fileInfos.stream()
                        .collect(sup, accu, comb)
                        .stream()
                        .map(mapper ->
                                     element("mets", "smLink",
                                             attr("xlink", "from", mapper.getObjID()),
                                             attr("xlink", "to", "phys_master_" + mapper.getUUID())
                                     )
                        )
                        .reduce(Consumer::andThen)
                        .map(smLinks -> element("mets", "structLink",
                                                smLinks
                        ))
                        .orElse(noStructLink -> {});
    }
}
