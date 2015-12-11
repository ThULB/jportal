package fsu.jportal.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetaXML;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

/**
 * Person abstraction. Be aware this class is not fully implemented.
 * 
 * @author Matthias Eichner
 */
public class JPPerson extends JPLegalEntity {

    public static String TYPE = "person";

    public JPPerson() {
        super();
    }

    public JPPerson(String mcrId) {
        super(mcrId);
    }

    /**
     * Creates a new JPPerson container for the given mcrId.
     * 
     * @param mcrId a mycore object id
     */
    public JPPerson(MCRObjectID mcrId) {
        super(mcrId);
    }

    /**
     * Creates a new JPPerson container for the given mycore object.
     * 
     * @param mcrObject the mycore object
     */
    public JPPerson(MCRObject mcrObject) {
        super(mcrObject);
    }

    /**
     * Returns the name of the person.
     * 
     * @return name of the person
     */
    @Override
    public String getTitle() {
        Map<String, String> nameMap = metaXMLToMap(getHeading());
        return buildName(nameMap);
    }

    /**
     * Returns a list of alternative names.
     * 
     * @return list of alternative names
     */
    public List<String> getAlternativeNames() {
        List<String> returnList = new ArrayList<>();
        List<MCRMetaXML> xmlList = getAlternative();
        for (MCRMetaXML xml : xmlList) {
            Map<String, String> nameMap = metaXMLToMap(Optional.of(xml));
            returnList.add(buildName(nameMap));
        }
        return returnList;
    }

    /**
     * Builds the name of a person.
     * 
     * @param nameMap map containing different parts of the name
     * @return the full name
     */
    protected String buildName(Map<String, String> nameMap) {
        String name = nameMap.get("name");
        String lastName = nameMap.get("lastName");
        String firstName = nameMap.get("firstName");
        String nameAffix = nameMap.get("nameAffix");
        String collocation = nameMap.get("collocation");
        StringBuilder b = new StringBuilder();
        if (name != null) {
            b.append(name);
        } else {
            b.append(lastName != null ? lastName : "");
            if (firstName != null) {
                b.append(b.length() != 0 ? ", " : "");
                b.append(firstName);
            }
        }
        if (nameAffix != null) {
            b.append(" ").append(nameAffix);
        }
        if (collocation != null) {
            b.append(" <").append(collocation).append(">");
        }
        return b.toString();
    }

    /**
     * Flattens a meta xml structure to a simple map.
     * <ul>
     * <li>key = element name</li>
     * <li>value = element text</li>
     * </ul>
     * 
     * @param metaXML the meta xml to flatten
     * @return a map containing the element name and text's
     */
    protected Map<String, String> metaXMLToMap(Optional<MCRMetaXML> metaXML) {
        return metaXML.map(MCRMetaXML::getContent).orElseGet(Collections::emptyList).stream()
            .filter(Filters.element()::matches).map(c -> (Element) c)
            .collect(Collectors.toMap(Element::getName, Element::getTextTrim));
    }

    /**
     * Returns the heading element if present.
     * 
     * @return the heading.
     */
    protected Optional<MCRMetaXML> getHeading() {
        return metadataStreamNotInherited("def.heading", MCRMetaXML.class).findFirst();
    }

    /**
     * Returns the alternative names as list.
     * 
     * @return list of <code>MCRMetaXML</code>
     */
    protected List<MCRMetaXML> getAlternative() {
        return metadataStreamNotInherited("def.alternative", MCRMetaXML.class).collect(Collectors.toList());
    }

    @Override
    public Optional<String> getId(String type) {
        return metadataStreamNotInherited("def.identifier", MCRMetaLangText.class).filter(t -> t.getType().equals(type))
            .map(MCRMetaLangText::getText).findFirst();
    }

    @Override
    public String getType() {
        return TYPE;
    }

}
