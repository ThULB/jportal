package fsu.jportal.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaInterface;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetaXML;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

public class JPPerson extends JPBaseComponent {

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
        MCRMetaElement heading = object.getMetadata().getMetadataElement("def.heading");
        if (heading == null) {
            return Optional.empty();
        }
        return StreamSupport.stream(heading.spliterator(), false).filter(m -> m.getInherited() == 0)
            .map(c -> (MCRMetaXML) c).findFirst();
    }

    /**
     * Returns the alternative names as list.
     * 
     * @return list of <code>MCRMetaXML</code>
     */
    protected List<MCRMetaXML> getAlternative() {
        MCRMetaElement alternative = object.getMetadata().getMetadataElement("def.alternative");
        if (alternative == null) {
            return Collections.emptyList();
        }
        return StreamSupport.stream(alternative.spliterator(), false).filter(m -> m.getInherited() == 0)
            .map(c -> (MCRMetaXML) c).collect(Collectors.toList());
    }

    /**
     * Returns the logo url. If available the logo plain is returned,
     * if not the logo with text and otherwise null.
     * 
     * @return the logo url
     */
    public String getLogo() {
        return getLogoPlain().orElse(getLogoPlusText().orElse(null));
    }

    /**
     * Finds the logo by type.
     * 
     * @param type type of logo e.g. logoPlain.
     * @return logo if present
     */
    protected Optional<String> findLogo(String type) {
        MCRMetaElement logo = object.getMetadata().getMetadataElement("logo");
        if (logo == null) {
            return null;
        }
        return StreamSupport.stream(logo.spliterator(), false)
            .filter(m -> m.getInherited() == 0 && type.equals(m.getType())).map(c -> (MCRMetaLangText) c)
            .map(MCRMetaLangText::getText).findFirst();
    }

    /**
     * Returns the plain logo url.
     * 
     * @return logo url if present
     */
    public Optional<String> getLogoPlain() {
        return findLogo("logoPlain");
    }

    /**
     * Returns the logo with text url.
     * 
     * @return logo url if present
     */
    public Optional<String> getLogoPlusText() {
        return findLogo("logoPlusText");
    }

    @Override
    public String getType() {
        return "person";
    }

}
