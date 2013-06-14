package fsu.jportal.mycore.sru.impex.pica.producer;

import org.jdom2.Element;

import fsu.archiv.mycore.sru.impex.pica.producer.PersonProducer;

public class JPPersonProducer extends PersonProducer {

    @Override
    protected Element createMetadataElement() throws Exception {
        addBiographicHistoricalNotes();
        return super.createMetadataElement();
    }

    protected void addBiographicHistoricalNotes() {
        String value = this.record.getValue("050G", "b");
        if(value != null) {
            appendToNotes(value, "\n");
        }
    }

    @Override
    protected String getGenderClassification() {
        return "urmel_class_00000001";
    }

    @Override
    protected Element createNameContainer(String elementName, String personalName, String collocation, String lastName, String firstName,
            String nameAffix) {
        Element nameContainer = generateSubElement(elementName, "de", 0, null, null, null, null);
        /* family name */
        if (lastName != null) {
            /* name affix */
            if (nameAffix != null) {
                lastName = nameAffix + " " + lastName;
            }
            if(elementName.equals("alternative")) {
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
            nameContainer.addContent(new Element("personalName").setText(personalName));
            if(elementName.equals("alternative")) {
                nameContainer.setAttribute("type", "single");
            }
        }
        /* and its collocation (if any) */
        if (collocation != null) {
            nameContainer.addContent(new Element("collocation").setText(collocation));
        }
        return nameContainer;
    }

}