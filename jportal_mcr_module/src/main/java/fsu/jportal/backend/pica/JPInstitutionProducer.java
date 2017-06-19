package fsu.jportal.backend.pica;

import java.io.IOException;
import java.util.List;

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
    protected Element createMetadataElement() throws IOException {
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
        createInstitutionNameElement(names, "name");
        return names;
    }

    protected void createInstitutionNameElement(Element parent, String elementName) {
        Element nameElement = new Element("name").setAttribute("inherited", "0");
        parent.addContent(nameElement);
        String fullname = this.record.getValue("029A", "a");
        if (fullname != null) {
            nameElement.addContent(new Element("fullname").setText(fullname));
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
        createMetaXMLNameElement(alternatives, "alternative", tag);
        return alternatives;
    }

    protected void createMetaXMLNameElement(Element defElement, String elementName, String fieldName) {
        List<Datafield> datafields = this.record.getDatafieldsByName(fieldName);
        for(Datafield datafield : datafields) {
            List<Subfield> subfields = datafield.getSubfieldsByCode("a");
            if (!subfields.isEmpty()) {
                Element childElement = new Element(elementName).setAttribute("inherited", "0");
                Element nameElement = new Element("name").setText(subfields.get(0).getValue());
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
        String start = this.record.getValue("060R", "a", "4", "datb");
        String end = this.record.getValue("060R", "b", "4", "datb");
        if (start == null) {
            return null;
        }
        String value = start + (end != null ? " - " + end : "");
        Element timesOfActivity = generateDefElement("timesOfActivity", MCRMetaLangText.class, false, true);
        Element timeOfActivity = generateSubElement("timeOfActivity", null, 0, null, "plain", null, null).setText(value);
        timesOfActivity.addContent(timeOfActivity);
        return timesOfActivity;
    }

    protected Element createPlacesOfActivity() {
        String name = this.record.getValue("065R", "a", "4", "orta");
        if (name == null) {
            return null;
        }
        Element placesOfActivity = generateDefElement("placesOfActivity", MCRMetaLangText.class, false, true);
        Element placeOfActivity = generateSubElement("placeOfActivity", null, 0, null, "plain", null, null).setText(name);
        placesOfActivity.addContent(placeOfActivity);
        return placesOfActivity;
    }

}
