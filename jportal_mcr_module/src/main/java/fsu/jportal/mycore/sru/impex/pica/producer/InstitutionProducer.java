package fsu.jportal.mycore.sru.impex.pica.producer;

import java.util.List;

import org.jdom2.Element;
import org.mycore.datamodel.metadata.MCRMetaInstitutionName;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetaXML;

import fsu.archiv.mycore.sru.impex.pica.model.Datafield;
import fsu.archiv.mycore.sru.impex.pica.model.Subfield;
import fsu.archiv.mycore.sru.impex.pica.producer.CorporationProducer;

public class InstitutionProducer extends CorporationProducer {

    public InstitutionProducer() {
        super("jpinst");
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
        List<Subfield> subfields;
        for (int i = 0; i < datafields.size(); i++) {
            subfields = datafields.get(i).getSubfieldsByCode("a");
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

    protected Element createGndLinkElement() {
        // TODO: there is no gndlink entry in jpinst datamodel
        return null;
    }

}
