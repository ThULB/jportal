package fsu.jportal.xml.dfg.oai;

import fsu.jportal.xml.JPMCRObjXMLElementName;
import fsu.jportal.xml.stream.ParsedMCRObj;
import fsu.jportal.xml.stream.ParsedXML.ElementData;
import fsu.jportal.xml.stream.ParserUtils;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fsu.jportal.xml.JPMCRObjXMLElementName.*;
import static fsu.jportal.xml.stream.XMLStreamReaderUtils.*;
import static fsu.jportal.xml.stream.XMLStreamWriterUtils.*;

/**
 * Created by chi on 09.10.16.
 */
public class DmdSec {
    public static Consumer<XMLStreamWriter> dmdSecXMLFragment(List<ParsedMCRObj> rootObjectWithChildren,
                                                              Function<String, Optional<XMLStreamReader>> objSupplier) {
        Function<ParsedMCRObj, Consumer<XMLStreamWriter>> dmdSecXML = obj ->
                element("mets", "dmdSec", attr("ID", "dmd_" + obj.getID()),
                        element("mets", "mdWrap", attr("MDTYPE", "MODS"),
                                element("mets", "xmlData",
                                        element("mods", "mods",
                                                dmdSecModsIdentifier(obj),
                                                dmdSecModsTileInfo(obj),
                                                dmdSecModsOriginInfo(obj),
                                                dmdSecModsName(obj, objSupplier)
                                        )
                                )
                        )
                );

        return rootObjectWithChildren
                .stream()
                .map(dmdSecXML)
                .reduce(Consumer::andThen)
                .orElse(noDmdSec -> {});
    }

    private static Consumer<XMLStreamWriter> dmdSecModsOriginInfo(ParsedMCRObj obj) {
        String inheritedZero = "inheritedZero";
        String others = "others";

        Function<ElementData, String> inheritedValue = elementData -> Optional
                .of(elementData)
                .map(e -> e.getAttr("inherited"))
                .orElseGet(Stream::empty)
                .filter("0"::equals)
                .findFirst()
                .map(s -> inheritedZero)
                .orElse(others);

        Map<String, List<ElementData>> dateMap = obj
                .element(date)
                .collect(Collectors.groupingBy(inheritedValue));

        UnaryOperator<String> typeMapper = type -> Stream
                .of("published_from:start", "published_until:end")
                .filter(s -> s.startsWith(type))
                .map(s -> s.split(":")[1])
                .findFirst()
                .orElse("notSupportedType");

        Function<ElementData, Consumer<XMLStreamWriter>> modsDateIssued = elementData ->
                element("mods", "dateIssued",
                        elementData.getAttr("type")
                                   .map(type -> attr("point", typeMapper.apply(type)))
                                   .findFirst()
                                   .orElse(noTypeAttribute -> {}),

                        text(elementData.getText().findFirst().orElse("noDate"))
                );

        Consumer<XMLStreamWriter> dateInheritedZeroXML = dateMap
                .getOrDefault(inheritedZero, Collections.emptyList())
                .stream()
                .map(modsDateIssued)
                .reduce(Consumer::andThen)
                .orElse(noInheritedZero -> {});

        Consumer<XMLStreamWriter> dateInheritedNotEqualsZeroXML = dateMap
                .getOrDefault(others, Collections.emptyList())
                .stream()
                .map(e ->
                        element("mods", "dateIssued",
                                text("OthersFoo")
                        )
                )
                .findFirst()
                .orElse(noInheritedNotEqualsZero -> {});

        return dateInheritedZeroXML.andThen(dateInheritedNotEqualsZeroXML);
    }

    private static Consumer<XMLStreamWriter> dmdSecModsTileInfo(ParsedMCRObj obj) {
        BiFunction<JPMCRObjXMLElementName, String, Consumer<XMLStreamWriter>> tileInfoXML = (elementName, type) -> obj
                .element(elementName)
                .flatMap(ElementData::getText)
                .map(title ->
                        element("mods", "titleInfo",
                                attr("type", type),
                                element("mods", "title", text(title))
                        )
                )
                .reduce(Consumer::andThen)
                .orElse(noMainTitle -> {});

        return tileInfoXML.apply(maintitle, "uniform")
                          .andThen(tileInfoXML.apply(subtitle, "alternative"));
    }

    public static Consumer<XMLStreamWriter> dmdSecModsIdentifier(ParsedMCRObj obj) {
        Supplier<String> objID = () -> obj.element(identi)
                                          .flatMap(ElementData::getText)
                                          .findFirst()
                                          .orElseGet(obj::getID);

        Supplier<String> objType = () -> obj.element(identi)
                                            .flatMap(o -> o.getAttr("type"))
                                            .findFirst()
                                            .orElse("mcrid");

        return element("mods", "identifier",
                attr("type", objID.get()),
                text(objType.get())
        );
    }

    public static Consumer<XMLStreamWriter> dmdSecModsName(ParsedMCRObj obj,
                                                           Function<String, Optional<XMLStreamReader>> objSupplier) {
        return obj.element(participant)
                  .map(p -> dmdSecModsName(p, objSupplier))
                  .reduce(Consumer::andThen)
                  .orElse(noModsName -> {});
    }

    public static Consumer<XMLStreamWriter> dmdSecModsName(ElementData participantData,
                                                           Function<String, Optional<XMLStreamReader>> objSupplier) {
        String participantID = participantData.getAttr("xlink", "href")
                                              .findFirst()
                                              .orElse("noparticipantID");

        ParsedMCRObj participantMcrObj = ParserUtils
                .getXMLForObj(participantID)
                .from(objSupplier)
                .parseDataUsing(parse(
                        matchElement("identifier",
                                isInherited("0"),
                                hasType("gnd").or(hasType("pnd")).or(hasType("ppn"))
                        ).getAttr("type")
                         .getText(),
                        matchElement("heading", isInherited("0")).and(
                                matchElement("lastName").getText(),
                                matchElement("firstName").getText(),
                                matchElement("nameAffix").getText()
                        ),
                        matchElement("dateOfBirth").getText(),
                        matchElement("dateOfDeath").getText()
                ));

        return element("mods", "name",
                attr("type", Optional.of(participantID)
                                     .filter(id -> id.contains("_jpinst_"))
                                     .map(id -> "corporate")
                                     .orElse("personal")),

                authorityAttr(participantMcrObj),
                modsRole(participantData),
                modsNamePart(participantMcrObj),
                modsNamePartDate(participantMcrObj),
                modsdisplayForm(participantMcrObj)

        );
    }

    private static Consumer<XMLStreamWriter> modsNamePartDate(ParsedMCRObj participantMcrObj) {
        String date = Stream.of("dateOfBirth", "dateOfDeath")
                            .flatMap(participantMcrObj::element)
                            .flatMap(ElementData::getText)
                            .collect(Collectors.joining(" - "));

        return Optional.ofNullable(date)
                       .filter(d -> !d.trim().equals("") && !d.trim().equals("-"))
                       .map(d -> element("mods", "namePart", attr("type", "date"), text(d)))
                       .orElse(noNamePartDate -> {});
    }

    private static Consumer<XMLStreamWriter> modsdisplayForm(ParsedMCRObj participantMcrObj) {
        String displayForm = Stream.of("lastName", "firstName", "nameAffix")
                                   .flatMap(nameType -> participantMcrObj.element("heading/" + nameType))
                                   .flatMap(ElementData::getText)
                                   .collect(Collectors.joining(", "));

        return element("mods", "displayForm", text(displayForm));
    }

    private static Consumer<XMLStreamWriter> modsNamePart(ParsedMCRObj participantMcrObj) {
        UnaryOperator<String> nameTypeMapping = nameType -> Stream
                .of("lastName:family", "firstName:given")
                .filter(n -> n.startsWith(nameType))
                .map(n -> n.split(":")[1])
                .findFirst().orElse("notSupportedNamePart");

        Function<String, Stream<Consumer<XMLStreamWriter>>> namePart = nameType -> participantMcrObj
                .element("heading/" + nameType)
                .flatMap(ElementData::getText)
                .map(text ->
                        element("mods", "namePart", attr("type", nameTypeMapping.apply(nameType)),
                                text(text)
                        )
                );

        return Stream.of("lastName", "firstName")
                     .flatMap(namePart)
                     .reduce(Consumer::andThen)
                     .orElse(noNameParts -> {});

    }

    public static Consumer<XMLStreamWriter> authorityAttr(ParsedMCRObj participant) {
        String authorityURI = "http://d-nb.info/gnd/";
        String ppnAuthorityURI = "https://kataloge.thulb.uni-jena.de/";

        return participant.element("identifier")
                          .sorted(Comparator.comparing(e -> e.getAttr("type").findFirst().orElse("noType")))
                          .findFirst()
                          .map(e ->
                                  fragment(
                                          attr("authority", e.getAttr("type").findFirst()
                                                             .filter(t -> !t.equals("ppn"))
                                                             .orElse("gvk-ppn")),

                                          attr("authorityURI", e.getAttr("type").findFirst()
                                                                .filter(t -> !t.equals("ppn"))
                                                                .map(t -> authorityURI)
                                                                .orElse(ppnAuthorityURI)),

                                          attr("valueURI", e.getAttr("type").findFirst()
                                                            .filter(t -> !t.equals("ppn"))
                                                            .map(t -> authorityURI + e.getText().findFirst().orElse("noText"))
                                                            .orElse(ppnAuthorityURI + "PPN?PPN=" + e.getText().findFirst().orElse("noText"))

                                          )
                                  )


                          )
                          .orElse(noIdentifier -> {});
    }

    private static Consumer<XMLStreamWriter> modsRole(ElementData participantData) {
        final String[] roleMappingConst = {
                "author:aut",
                "printer:prt",
                "other:oth",
                "employer:app", // Auftraggeber -> Applicant
                "person_charge:edt", //Bearbeiter -> Editor
                "owner:own", //Besitzer -> owner
                "previous_owner:fmo", //Besitzer -> former owner
                "recipient:rcp", //Empfänger -> recipient
                "artist:art", //Künstler -> artist
                "writer:ins", //Schreiber -> inscriber
                "translator:trl", //Übersetzer -> translator
                "corporation:own", //Institution -> owner
                "previous_organisation:fmo", //Vorbesitzende Institution -> former owner
                "patron:pat" //Förderer -> parton
        };

        UnaryOperator<String> roleMapping = type -> Arrays
                .stream(roleMappingConst)
                .filter(role -> role.startsWith(type))
                .findFirst()
                .map(found -> found.split(":")[1])
                .orElse("asn");

        return participantData.getAttr("type")
                              .findFirst()
                              .map(type ->
                                      element("mods", "role",
                                              element("mods", "roleTerm",
                                                      text(roleMapping.apply(type))
                                              )
                                      )
                              ).orElse(noModsRole -> {});
    }

}
