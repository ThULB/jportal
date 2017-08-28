package fsu.jportal.backend.marc;

import fsu.jportal.backend.*;
import fsu.jportal.util.JPComponentUtil;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRObjectID;

import java.util.List;
import java.util.Map;

public abstract class Marc21Converter {

    public static Record convert(JPPeriodicalComponent component) {
        if(JPComponentUtil.is(component, JPObjectType.jpjournal)) {
            return convert((JPJournal) component);
        } else if(JPComponentUtil.is(component, JPObjectType.jpvolume)) {
            return convert((JPVolume) component);
        } else if(JPComponentUtil.is(component, JPObjectType.jparticle)) {
            return convert((JPArticle) component);
        }
        throw new IllegalArgumentException("Invalid component " + component + " has to be jpjournal, jpvolume or jparticle!");
    }

    public static Record convert(JPJournal journal) {
        MarcFactory marcFactory = MarcFactory.newInstance();
        Record marcRecord = marcFactory.newRecord();
        List<DataField> fields = marcRecord.getDataFields();

        addCommon(journal, marcFactory, fields, null, "mainPublisher");

        // 260 Publication, Distribution, etc. (Imprint)
        journal.getDate(JPJournal.DateType.published_from.name()).ifPresent(from -> {
            final StringBuilder date = new StringBuilder(from.getISOString()).append(" -");
            DataField dataField = marcFactory.newDataField("260", '#', '#');
            journal.getDate(JPJournal.DateType.published_until.name()).ifPresent(until -> {
                date.append(" ").append(until.getISOString());
            });
            dataField.addSubfield(marcFactory.newSubfield('c', date.toString()));
            fields.add(dataField);
        });

        return marcRecord;
    }

    public static Record convert(JPVolume volume) {
        MarcFactory marcFactory = MarcFactory.newInstance();
        Record marcRecord = marcFactory.newRecord();
        List<DataField> fields = marcRecord.getDataFields();
        addCommon(volume, marcFactory, fields, null, null);
        // 260 Publication, Distribution, etc. (Imprint)
        addPublishedDate(volume, marcFactory, fields);
        return marcRecord;
    }

    public static Record convert(JPArticle article) {
        MarcFactory marcFactory = MarcFactory.newInstance();
        Record marcRecord = marcFactory.newRecord();
        List<DataField> fields = marcRecord.getDataFields();

        addCommon(article, marcFactory, fields, "author", null);

        // 260 Publication, Distribution, etc. (Imprint)
        addPublishedDate(article, marcFactory, fields);

        // 300 - Physical Description
        article.getSize().ifPresent(size -> {
            fields.add(getField(marcFactory, "300", '#', '#', 'a', size));
        });

        // 650 - Subject Added Entry-Topical Term
        for (String keyword : article.getKeywords()) {
            fields.add(getField(marcFactory, "650", '#', '4', 'g', keyword));
        }

        return marcRecord;
    }


    /**
     * Helper method to add a field with exactly one subfield.
     *
     * @param tag   the tag e.g. 300, 246 ...
     * @param ind1  first indicator
     * @param ind2  second indicator
     * @param code  the subfield code
     * @param value the subfield value
     * @return the new datafield
     */
    private static DataField getField(MarcFactory marcFactory, String tag, char ind1, char ind2, char code, String value) {
        DataField dataField = marcFactory.newDataField(tag, ind1, ind2);
        dataField.addSubfield(marcFactory.newSubfield(code, value));
        return dataField;
    }

    private static void addCommon(JPPeriodicalComponent component, MarcFactory marcFactory, List<DataField> fields, String mainPersonRole, String mainInstitutionRole) {
        // 245 - Title Statement
        fields.add(getTitle(component.getTitle(), marcFactory));

        // 246 - Varying Form of Title
        List<MCRMetaLangText> subtitles = component.getSubtitles();
        if (!subtitles.isEmpty()) {
            DataField dataField = getSubtitle(subtitles, marcFactory);
            fields.add(dataField);
        }

        // 041 - Language Code
        addLanguage(component, marcFactory, fields);

        // 020 - ISBN, ISSN, Fingerprint
        addIdentis("020", "isbn", fields, component, marcFactory);
        addIdentis("022", "issn", fields, component, marcFactory);
        addIdentis("026", "fingerprint", fields, component, marcFactory);

        // 100 - Main Entry-Personal Name
        MCRObjectID mainPersonID = null;
        if (mainPersonRole != null) {
            List<MCRObjectID> mainPersonIDs = component.getParticipants(JPObjectType.person, mainPersonRole);
            if (!mainPersonIDs.isEmpty()) {
                // there can only be one main author
                addPerson("100", marcFactory, fields, mainPersonID = mainPersonIDs.get(0));
            }
        }

        // 700 - Added Entry-Personal Name
        for (MCRObjectID personID : component.getParticipants(JPObjectType.person)) {
            if (personID.equals(mainPersonID)) {
                continue;
            }
            addPerson("700", marcFactory, fields, personID);
        }

        // 110 - Author
        MCRObjectID mainInstitutionID = null;
        if (mainInstitutionRole != null) {
            List<MCRObjectID> publishers = component.getParticipants(JPObjectType.jpinst, mainInstitutionRole);
            if (!publishers.isEmpty()) {
                // there can only be one main author
                addInstitution("110", marcFactory, fields, mainInstitutionID = publishers.get(0));
            }
        }

        // 710 - Main Entry-Corporate Name (has no main entry )
        for (MCRObjectID jpinstID : component.getParticipants(JPObjectType.jpinst)) {
            if (jpinstID.equals(mainInstitutionID)) {
                continue;
            }
            addInstitution("710", marcFactory, fields, jpinstID);
        }

    }

    private static DataField getTitle(String title, MarcFactory marcFactory) {
        DataField dataField = getField(marcFactory, "245", '1', '0', 'a', Marc21Util.getMaintitle(title));
        Marc21Util.getRemainderTitle(title).ifPresent(remainderTitle -> {
            dataField.addSubfield(marcFactory.newSubfield('b', remainderTitle));
        });
        Marc21Util.getStatementOfResponsibility(title).ifPresent(res -> {
            dataField.addSubfield(marcFactory.newSubfield('c', res));
        });
        return dataField;
    }

    private static DataField getSubtitle(List<MCRMetaLangText> subtitles, MarcFactory marcFactory) {
        DataField dataField = marcFactory.newDataField("246", '3', '#');
        subtitles.forEach(subtitle -> {
            String type = subtitle.getType();
            String text = subtitle.getText();
            char code = 'g';
            if (type.equals("short")) {
                code = 'a';
            }
            dataField.addSubfield(marcFactory.newSubfield(code, text));
        });
        return dataField;
    }

    private static void addPublishedDate(JPPeriodicalComponent component, MarcFactory marcFactory, List<DataField> fields) {
        // 260 Publication, Distribution, etc. (Imprint)
        component.getDate(JPArticle.DateType.published.name()).ifPresent(date -> {
            String dateAsString = date.getISOString();
            DataField dataField = marcFactory.newDataField("260", '#', '#');
            dataField.addSubfield(marcFactory.newSubfield('c', dateAsString));
            fields.add(dataField);
        });
    }

    private static void addLanguage(JPPeriodicalComponent component, MarcFactory marcFactory, List<DataField> fields) {
        DataField languageField = getField(marcFactory, "041", '#', '7', 'a', component.getLanguageCode());
        languageField.addSubfield(marcFactory.newSubfield('2', "iso639-1"));
        fields.add(languageField);
    }

    private static void addPerson(String marcEntry, MarcFactory marcFactory, List<DataField> fields, MCRObjectID id) {
        JPPerson person = new JPPerson(id);
        Map<String, String> nameMap = person.metaXMLToMap(person.getHeading());
        char firstIndicator = nameMap.containsKey("lastName") ? '1' : '0';
        DataField dataField = marcFactory.newDataField(marcEntry, firstIndicator, '#');
        dataField.addSubfield(marcFactory.newSubfield('a', person.getTitle()));
        person.getId("gnd").ifPresent(gnd -> {
            dataField.addSubfield(marcFactory.newSubfield('0', "(DE-588)" + gnd));
        });
        fields.add(dataField);
    }

    private static void addInstitution(String marcEntry, MarcFactory marcFactory, List<DataField> fields, MCRObjectID id) {
        JPInstitution institution = new JPInstitution(id);
        DataField dataField = marcFactory.newDataField(marcEntry, '2', '#');
        dataField.addSubfield(marcFactory.newSubfield('a', institution.getTitle()));
        institution.getId("gnd").ifPresent(gnd -> {
            dataField.addSubfield(marcFactory.newSubfield('0', "(DE-588)" + gnd));
        });
        fields.add(dataField);
    }

    private static void addIdentis(String marcEntry, String idType, List<DataField> fields, JPPeriodicalComponent component, MarcFactory marcFactory) {
        component.getIdenti(idType).ifPresent(value -> {
            fields.add(getField(marcFactory, marcEntry, '#', '#', 'a', value));
        });
    }

}
