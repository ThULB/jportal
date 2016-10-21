package fsu.jportal.xml.dfg.oai;


import fsu.jportal.xml.stream.DerivateFileInfo;
import fsu.jportal.xml.stream.ParsedMCRObj;
import fsu.jportal.xml.stream.ParserUtils;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fsu.jportal.xml.JPMCRObjXMLElementName.*;
import static fsu.jportal.xml.dfg.oai.DmdSec.dmdSecXMLFragment;
import static fsu.jportal.xml.stream.ParsedXML.ElementData;
import static fsu.jportal.xml.stream.XMLStreamReaderUtils.*;
import static fsu.jportal.xml.stream.XMLStreamWriterUtils.*;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * Created by chi on 09.10.16.
 */
public class DFGOAIMetXMLCreator {
    private static Function<XMLStreamReader, Map<String, Map<String, Optional<String>>>> xmlStreamParser() {
        return parse(
                matchElement(mycoreobject).getAttr("ID"),
                matchElement(child).getAttr("xlink", "href"),
                matchElement(derobject).getAttr("xlink", "href"),
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
                                                      Function<String, Stream<DerivateFileInfo>> derivateSupplier) {

        ParsedMCRObj rootObj = ParserUtils
                .getXMLForObj(rootID)
                .from(objSupplier)
                .parseDataUsing(xmlStreamParser());

        List<ParsedMCRObj> rootObjectWithChildren = ParserUtils
                .getObjectWithChildrenFor(rootObj)
                .from(objSupplier)
                .parseDataUsing(xmlStreamParser())
                .collect(toList());

        List<DerivateFileInfo> fileInfos = rootObj
                .element(derobject)
                .flatMap(o -> o.getAttr("xlink", "href"))
                .flatMap(derivateSupplier)
                .filter(file -> !file.getFileName().equals("mets.xml"))
                .collect(toList());

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
                                        fileSecXMLFragment(fileInfos),
                                        structMapPhysXMLFragment(rootObj, fileInfos),
                                        structMapLogXMLFragment(rootObjectWithChildren),
                                        structLinkXMLFragment(rootObjectWithChildren, fileInfos)
                                )
                        )
                )
        );
    }

    public static Consumer<XMLStreamWriter> amdSecXMLFragment(List<ParsedMCRObj> rootObjectWithChildren) {
        return rootObjectWithChildren
                .stream()
                .map(obj -> element("mets", "amdSec", attr("ID", "amd_" + obj.getID())))
                .reduce(Consumer::andThen)
                .orElse(noAmdSec -> {});

    }

    public static Consumer<XMLStreamWriter> fileSecXMLFragment(List<DerivateFileInfo> fileInfos) {
        return element("mets", "fileSec",
                element("mets", "fileGrp", attr("USE", "DEFAULT"), fileInfos
                        .stream()
                        .map(fileInfo ->
                                element("mets", "file",
                                        attr("ID", "DEFAULT_" + fileInfo.getUuid()),
                                        attr("MIMETYPE", fileInfo.getContentType()),
                                        element("mets", "FLocat",
                                                attr("LOCTYPE", "URL"),
                                                attr("xlink", "href", fileInfo.getUri())
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

        return structMapLogXMLFragment("root", childrenMap);
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
                                          attr("Type", getType.apply(id)),
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
            return uuidToMcrObjIDMap.entrySet()
                                    .stream()
                                    .map(entry -> new UUIDMcrObj() {
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
