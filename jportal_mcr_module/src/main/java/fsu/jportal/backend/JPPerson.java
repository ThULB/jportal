package fsu.jportal.backend;

import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.metadata.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Person abstraction. Be aware this class is not fully implemented.
 * 
 * @author Matthias Eichner
 */
public class JPPerson extends JPLegalEntity {

    public static String TYPE = JPObjectType.person.name();

    public enum Sex {
        male, female, unknown
    }

    public enum NoteType {
        visible, hidden
    }

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
        Map<String, String> nameMap = metaXMLToMap(getHeading().orElse(null));
        return buildName(nameMap);
    }

    /**
     * Sets the name/title of the person.
     * 
     * @param name including first and last name. (required)
     * @param affix name affix for noble's e.g. "von Guttenberg"
     * @param collocation name collocation 
     */
    public void setName(String name, String affix, String collocation) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name cannot be null or empty.");
        }
        MCRMetaXML xml = buildHeading();
        xml.addContent(new Element("name").setText(name));
        addAffixAndCollocation(xml, affix, collocation);
    }

    /**
     * Sets the name/title of the person.
     * 
     * @param lastName the last name (required)
     * @param firstName the first name
     * @param affix name affix for noble's e.g. "von Guttenberg"
     * @param collocation name collocation
     */
    public void setName(String lastName, String firstName, String affix, String collocation) {
        if (lastName == null || lastName.isEmpty()) {
            throw new IllegalArgumentException("lastName cannot be null or empty.");
        }
        MCRMetaXML xml = buildHeading();
        xml.addContent(new Element("lastName").setText(lastName));
        if (firstName != null && !firstName.isEmpty()) {
            xml.addContent(new Element("firstName").setText(firstName));
        }
        addAffixAndCollocation(xml, affix, collocation);
    }

    protected MCRMetaXML buildHeading() {
        MCRMetaElement heading = object.getMetadata().getMetadataElement("def.heading");
        if (heading == null) {
            heading = new MCRMetaElement(MCRMetaXML.class, "def.heading", true, true, null);
            object.getMetadata().setMetadataElement(heading);
        }
        MCRMetaXML xml = new MCRMetaXML("heading", null, 0);
        heading.addMetaObject(xml);
        return xml;
    }

    protected void addAffixAndCollocation(MCRMetaXML xml, String affix, String collocation) {
        if (affix != null && !affix.isEmpty()) {
            xml.addContent(new Element("nameAffix").setText(affix));
        }
        if (collocation != null && !collocation.isEmpty()) {
            xml.addContent(new Element("collocation").setText(collocation));
        }
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
            Map<String, String> nameMap = metaXMLToMap(xml);
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
    public Map<String, String> metaXMLToMap(MCRMetaXML metaXML) {
        return Optional.ofNullable(metaXML).map(MCRMetaXML::getContent).orElseGet(Collections::emptyList).stream()
                       .filter(Filters.element()::matches).map(c -> (Element) c)
                       .collect(Collectors.toMap(Element::getName, Element::getTextTrim));
    }

    /**
     * Returns the heading element if present.
     * 
     * @return the heading.
     */
    public Optional<MCRMetaXML> getHeading() {
        return metadataStreamNotInherited("def.heading", MCRMetaXML.class).findFirst();
    }

    /**
     * Returns the alternative names as list.
     * 
     * @return list of <code>MCRMetaXML</code>
     */
    public List<MCRMetaXML> getAlternative() {
        return metadataStreamNotInherited("def.alternative", MCRMetaXML.class).collect(Collectors.toList());
    }

    /**
     * Sets the sex for this person.
     * 
     * @param sex male, female or unknown
     */
    public void setGender(Sex sex) {
        if (sex == null) {
            object.getMetadata().removeMetadataElement("def.gender");
            return;
        }
        MCRMetaElement defGender = new MCRMetaElement(MCRMetaClassification.class, "def.gender", true, true, null);
        defGender.addMetaObject(
            new MCRMetaClassification("gender", 0, null, new MCRCategoryID("urmel_class_00000001", sex.name())));
        object.getMetadata().setMetadataElement(defGender);
    }

    /**
     * Returns the sex of this person.
     * 
     * @return null if no sex is specified
     */
    public Optional<Sex> getGender() {
        return metadataStreamNotInherited("def.gender", MCRMetaClassification.class)
                .map(MCRMetaClassification::getCategId).map(Sex::valueOf).findFirst();
    }

    /**
     * Adds a new role.
     * 
     * @param role the role to add
     */
    public void addRole(String role) {
        addText("def.role", "role", role, null, true, true);
    }

    /**
     * A list of roles. Changes on the list have no
     * effect on the mycore object.
     * 
     * @return a copied list of roles.
     */
    public List<String> getRoles() {
        return listText("def.role", null);
    }

    /**
     * Sets the place of birth for this person.
     * 
     * @param placeOfBirth the place as string
     */
    public void setPlaceOfBirth(String placeOfBirth) {
        setText("def.placeOfBirth", "placeOfBirth", placeOfBirth, null, true, true);
    }

    /**
     * Returns the place of birth for this person.
     * 
     * @return an optional containing the place of birth
     */
    public Optional<String> getPlaceOfBirth() {
        return getText("def.placeOfBirth", null);
    }

    /**
     * Sets the place of death for this person.
     * 
     * @param placeOfDeath the place as string
     */
    public void setPlaceOfDeath(String placeOfDeath) {
        setText("def.placeOfDeath", "placeOfDeath", placeOfDeath, null, true, true);
    }

    /**
     * Returns the place of death for this person.
     * 
     * @return an optional containing the place of death
     */
    public Optional<String> getPlaceOfDeath() {
        return getText("def.placeOfDeath", null);
    }

    /**
     * Sets the place of death for this person.
     *
     * @param placeOfActivity the place as string
     */
    public void addPlaceOfActivity(String placeOfActivity) {
        addText("def.placeOfActivity", "placeOfActivity", placeOfActivity, null, true, true);
    }

    /**
     * A list of place of activities. Changes on the list have no
     * effect on the mycore object.
     * 
     * @return a copied list of place of activities.
     */
    public List<String> listPlaceOfActivities() {
        return listText("def.placeOfActivity", null);
    }

    /**
     * Sets the date of birth. The date should be in the format of
     * YYYY-MM-DD or YYYY-MM or just YYYY.
     * 
     * @param dateOfBirth the date of birth to set
     */
    public void setDateOfBirth(String dateOfBirth) {
        if (dateOfBirth == null) {
            object.getMetadata().removeMetadataElement("def.dateOfBirth");
            return;
        }
        MCRMetaElement metaElement = new MCRMetaElement(MCRMetaISO8601Date.class, "def.dateOfBirth", true, true, null);
        metaElement.addMetaObject(buildISODate("dateOfBirth", dateOfBirth, null));
        object.getMetadata().setMetadataElement(metaElement);
    }

    /**
     * Returns the date of birth as {@link MCRMetaISO8601Date}.
     * 
     * @return date as mycore meta interface
     */
    public Optional<MCRMetaISO8601Date> getDateOfBirth() {
        return metadataStreamNotInherited("def.dateOfBirth", MCRMetaISO8601Date.class).findFirst();
    }

    /**
     * Sets the date of death. The date should be in the format of
     * YYYY-MM-DD or YYYY-MM or just YYYY.
     * 
     * @param dateOfDeath the date of death to set
     */
    public void setDateOfDeath(String dateOfDeath) {
        if (dateOfDeath == null) {
            object.getMetadata().removeMetadataElement("def.dateOfDeath");
            return;
        }
        MCRMetaElement metaElement = new MCRMetaElement(MCRMetaISO8601Date.class, "def.dateOfDeath", true, true, null);
        metaElement.addMetaObject(buildISODate("dateOfDeath", dateOfDeath, null));
        object.getMetadata().setMetadataElement(metaElement);
    }

    /**
     * Returns the date of birth as {@link MCRMetaISO8601Date}.
     * 
     * @return date as mycore meta interface
     */
    public Optional<MCRMetaISO8601Date> getDateOfDeath() {
        return metadataStreamNotInherited("def.dateOfDeath", MCRMetaISO8601Date.class).findFirst();
    }

    /**
     * Sets a note for this person. To remove the note use null values.
     * 
     * @param note the note text
     * @param type type of the note
     */
    public void setNote(String note, NoteType type) {
        if (note == null) {
            object.getMetadata().removeMetadataElement("def.note");
            return;
        }
        MCRMetaElement metaElement = new MCRMetaElement(MCRMetaLangText.class, "def.note", false, true, null);
        metaElement.addMetaObject(new MCRMetaLangText("note", null, type.name(), 0, "plain", note));
        object.getMetadata().setMetadataElement(metaElement);
    }

    /**
     * Returns the note.
     * 
     * @return optional of the note.
     */
    public Optional<MCRMetaLangText> getNote() {
        return metadataStreamNotInherited("def.note", MCRMetaLangText.class).findFirst();
    }

    @Override
    public Optional<String> getId(String type) {
        return getText("def.identifier", type);
    }

    @Override
    public String getType() {
        return TYPE;
    }

}
