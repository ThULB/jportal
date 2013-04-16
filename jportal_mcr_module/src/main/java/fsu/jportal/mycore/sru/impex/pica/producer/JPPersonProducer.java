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
        Element nameContainer = super.createNameContainer(elementName, personalName, collocation, lastName, firstName, nameAffix);
        if(elementName.equals("alternative")) {
            if(personalName != null && !personalName.equals("")) {
                return nameContainer.setAttribute("type", "single");
            }
            return nameContainer.setAttribute("type", "complete");
        }
        return nameContainer;
    }

}
