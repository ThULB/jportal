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
    protected Element createNotesElement() {
        Element defNotes = super.createNotesElement();
        if(defNotes != null) {
            Element note = defNotes.getChild("note");
            if(note != null) {
                note.setAttribute("type", "visible");
            }
        }
        return defNotes;
    }

    @Override
    protected String getGenderClassification() {
        return "urmel_class_00000001";
    }

}
