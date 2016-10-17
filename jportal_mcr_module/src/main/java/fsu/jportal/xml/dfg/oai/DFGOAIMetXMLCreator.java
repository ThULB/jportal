package fsu.jportal.xml.dfg.oai;


import fsu.jportal.xml.stream.DerivateFileInfo;
import fsu.jportal.xml.stream.ParsedMCRObj;
import fsu.jportal.xml.stream.ParserUtils;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
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

    public static Consumer<XMLStreamWriter> oaiRecord(String oaiIdentifier,
                                                      String rootID,
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
                                       .map(t -> t.substring(0,9))
                                       .map(date -> element("datestamp", text(date)))
                                       .reduce(Consumer::andThen)
                                       .orElse(noDatestamp -> {})
                        ),
                        element("metadata",
                                dmdSecXMLFragment(rootObjectWithChildren, objSupplier),
                                amdSecXMLFragment(rootObjectWithChildren),
                                fileSecXMLFragment(fileInfos),
                                structMapPhysXMLFragment(rootObj, fileInfos),
                                structMapLogXMLFragment(rootObjectWithChildren),
                                structLinkXMLFragment(rootObjectWithChildren, fileInfos)
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
                                        attr("MIMETYPE", fileInfo.getMimeType()),
                                        element("mets", "FLocat",
                                                attr("LOCTYPE", "URL"),
                                                attr("xlink", "href", fileInfo.getHref())
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

    public static Consumer<XMLStreamWriter> structLinkXMLFragment(List<ParsedMCRObj> rootObjectWithChildren,
                                                                  List<DerivateFileInfo> fileInfos) {
        Function<String, Stream<String>> linkToUUID = linkID -> fileInfos
                .stream()
                .filter(fileInfo -> linkID.contains(fileInfo.getFileName()))
                .map(DerivateFileInfo::getUuid);

        Function<ParsedMCRObj, Stream<Consumer<XMLStreamWriter>>> createSmLinkXML = obj -> obj
                .element(derivateLink)
                .flatMap(o -> o.getAttr("xlink", "href"))
                .flatMap(linkToUUID)
                .map(uuid ->
                        element("mets", "smLink",
                                attr("xlink", "from", obj.getID()),
                                attr("xlink", "to", "phys_master_" + uuid)
                        )
                );

        Predicate<ParsedMCRObj> hasDerivateLink = obj -> obj
                .element(derivateLink)
                .flatMap(o -> o.getAttr("xlink", "href"))
                .findAny()
                .isPresent();

        return rootObjectWithChildren.stream()
                                     .filter(hasDerivateLink)
                                     .flatMap(createSmLinkXML)
                                     .reduce(Consumer::andThen)
                                     .map(smLinks -> element("mets", "structLink",
                                             smLinks
                                     ))
                                     .orElse(noStructLink -> {});
    }
}
