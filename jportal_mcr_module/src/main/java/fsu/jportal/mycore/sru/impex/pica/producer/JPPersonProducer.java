package fsu.jportal.mycore.sru.impex.pica.producer;

import org.jdom2.Element;

import fsu.archiv.mycore.sru.impex.pica.producer.PersonProducer;

public class JPPersonProducer extends PersonProducer {

    @Override
    protected Element createNotesElement() {
        Element defNotes = super.createNotesElement();
        if (defNotes != null) {
            Element note = defNotes.getChild("note");
            if (note != null) {
                note.setAttribute("type", "visible");
            }
        }
        return defNotes;
    }

    @Override
    protected String getGenderClassification() {
        return "urmel_class_00000001";
    }

    @Override
    protected Element createNameContainer(String elementName, String personalName, String collocation, String lastName,
        String firstName, String nameAffix) {
        Element nameContainer = generateSubElement(elementName, "de", 0, null, null, null, null);
        /* family name */
        if (lastName != null) {
            if (elementName.equals("alternative")) {
                nameContainer.setAttribute("type", "complete");
            }
            nameContainer.addContent(new Element("lastName").setText(lastName));
        }
        /* forename */
        if (firstName != null) {
            nameContainer.addContent(new Element("firstName").setText(firstName));
        }
        /* add the personal name (if any) */
        if (personalName != null) {
            nameContainer.addContent(new Element("name").setText(personalName));
            if (elementName.equals("alternative")) {
                nameContainer.setAttribute("type", "single");
            }
        }
        /* and its collocation (if any) */
        if (collocation != null) {
            nameContainer.addContent(new Element("collocation").setText(collocation));
        }
        if (nameAffix != null) {
            nameContainer.addContent(new Element("nameAffix").setText(nameAffix));
        }
        return nameContainer;
    }

}
