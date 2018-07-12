package fsu.jportal.backend.pica;

import java.util.List;
import java.util.Optional;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.mycore.datamodel.metadata.MCRMetaInstitutionName;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetaXML;

import fsu.archiv.mycore.sru.impex.pica.model.Datafield;
import fsu.archiv.mycore.sru.impex.pica.model.Subfield;
import fsu.archiv.mycore.sru.impex.pica.producer.CorporationProducer;

public class JPInstitutionProducer extends CorporationProducer {

    public JPInstitutionProducer() {
        super("jpinst");
    }

    @Override
    protected Element createMetadataElement() {
        Element metadata = new Element("metadata");
        metadata.setAttribute("lang", "de", Namespace.XML_NAMESPACE);
        Element meta;

        if ((meta = createDefHeadingElement("029A")) != null) {
            metadata.addContent(meta);
        }

        if ((meta = createDefAlternativeElement("029@")) != null) {
            metadata.addContent(meta);
        }

        // if standard fields 029A and 029@ are not available, we try 065A (due to migration to gnd format)
        // this may need further analysis :s
        if (meta == null) {
            if ((meta = createDefHeadingElement("065A")) != null) {
                metadata.addContent(meta);
            }
        }

        if ((meta = createDefIdentifierElement()) != null) {
            metadata.addContent(meta);
        }

        if ((meta = createTimesOfActivity()) != null) {
            metadata.addContent(meta);
        }

        if ((meta = createPlacesOfActivity()) != null) {
            metadata.addContent(meta);
        }

        /*
        if ((meta = createDefRoleElement()) != null) {
            metadata.addContent(meta);
        }
        */
        if ((meta = createNotesElement()) != null) {
            metadata.addContent(meta);
        }

        return metadata;
    }

    protected Element createDefHeadingElement(String tag) {
        if (this.record.getDatafieldsByName(tag).size() < 1) {
            return null;
        }
        Element names = generateDefElement("names", MCRMetaInstitutionName.class, true, false);
        createInstitutionNameElement(names);
        return names;
    }

    protected void createInstitutionNameElement(Element parent) {
        Element nameElement = new Element("name").setAttribute("inherited", "0");
        parent.addContent(nameElement);
        StringBuilder fullname = Optional.ofNullable(this.record.getValue("029A", "a")).map(StringBuilder::new)
            .orElse(null);
        if (fullname != null) {
            List<String> bList = this.record.getValues("029A", "b");
            for (String b : bList) {
                fullname.append(". ").append(b);
            }
            List<String> gList = this.record.getValues("029A", "g");
            for (String g : gList) {
                fullname.append(" (").append(g).append(")");
            }
            nameElement.addContent(new Element("fullname").setText(fullname.toString()));
        }
        String nickname = this.record.getValue("029@", "a", "4", "abku");
        if (nickname != null) {
            nameElement.addContent(new Element("nickname").setText(nickname));
        }
    }

    @Override
    protected Element createDefAlternativeElement(String tag) {
        if (this.record.getDatafieldsByName(tag).size() < 1) {
            return null;
        }
        Element alternatives = generateDefElement("alternatives", MCRMetaXML.class, true, true);
        createMetaXMLNameElement(alternatives, tag);
        return alternatives;
    }

    protected void createMetaXMLNameElement(Element defElement, String fieldName) {
        List<Datafield> datafields = this.record.getDatafieldsByName(fieldName);
        for (Datafield datafield : datafields) {
            Subfield subfield = datafield.getFirstSubfieldByCode("a");
            if (subfield != null) {
                Element childElement = new Element("alternative").setAttribute("inherited", "0");
                StringBuilder name = new StringBuilder(subfield.getValue());
                List<Subfield> bList = datafield.getSubfieldsByCode("b");
                for (Subfield b : bList) {
                    if (!"".equals(b.getValue())) {
                        name.append(". ").append(b.getValue());
                    }
                }
                List<Subfield> gList = datafield.getSubfieldsByCode("g");
                for (Subfield g : gList) {
                    if (!"".equals(g.getValue())) {
                        name.append(" (").append(g.getValue()).append(")");
                    }
                }
                Element nameElement = new Element("name").setText(name.toString());
                defElement.addContent(childElement.addContent(nameElement));
            }
        }
    }

    protected Element getDefaultIdentifierElement() {
        return generateDefElement("identifiers", MCRMetaLangText.class, true, false);
    }

    protected Element getDefaultNoteElement() {
        return generateDefElement("notes", MCRMetaLangText.class, true, false);
    }

    protected Element createTimesOfActivity() {
        List<Datafield> timeFields = this.record.getDatafieldsByName("060R");
        Element timesOfActivity = generateDefElement("timesOfActivity", MCRMetaLangText.class, false, true);
        for (Datafield timeField : timeFields) {
            Subfield condition = timeField.getFirstSubfieldByCode("4");
            if (condition == null || !condition.getValue().equals("datb")) {
                continue;
            }
            Subfield startField = timeField.getFirstSubfieldByCode("a");
            Subfield endField = timeField.getFirstSubfieldByCode("b");
            Subfield vField = timeField.getFirstSubfieldByCode("v");
            if (startField == null && endField == null) {
                continue;
            }
            String value = startField != null ? startField.getValue() + " " : "";
            value += "- ";
            value += endField != null ? endField.getValue() : "";
            value += vField != null ? " (" + vField.getValue() + ")" : "";
            Element timeOfActivity = generateSubElement("timeOfActivity", null, 0, null, "plain", null, null)
                .setText(value);
            timesOfActivity.addContent(timeOfActivity);
        }
        return timesOfActivity.getChildren().isEmpty() ? null : timesOfActivity;
    }

    protected Element createPlacesOfActivity() {
        String name = this.record.getValue("065R", "a", "4", "orta");
        if (name == null) {
            return null;
        }
        Element placesOfActivity = generateDefElement("placesOfActivity", MCRMetaLangText.class, false, true);
        Element placeOfActivity = generateSubElement("placeOfActivity", null, 0, null, "plain", null, null)
            .setText(name);
        placesOfActivity.addContent(placeOfActivity);
        return placesOfActivity;
    }

}
