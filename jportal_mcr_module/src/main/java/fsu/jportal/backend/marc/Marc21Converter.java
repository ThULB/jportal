package fsu.jportal.backend.marc;

import java.util.List;
import java.util.Map;

import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.MCRFrontendUtil;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPInstitution;
import fsu.jportal.backend.JPJournal;
import fsu.jportal.backend.JPObjectType;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.backend.JPPerson;
import fsu.jportal.backend.JPVolume;
import fsu.jportal.util.JPComponentUtil;
import fsu.jportal.util.Pair;

public abstract class Marc21Converter {

    public static Record convert(JPPeriodicalComponent component) {
        if (JPComponentUtil.is(component, JPObjectType.jpjournal)) {
            return convert((JPJournal) component);
        } else if (JPComponentUtil.is(component, JPObjectType.jpvolume)) {
            return convert((JPVolume) component);
        } else if (JPComponentUtil.is(component, JPObjectType.jparticle)) {
            return convert((JPArticle) component);
        }
        throw new IllegalArgumentException(
            "Invalid component " + component + " has to be jpjournal, jpvolume or jparticle!");
    }

    public static Record convert(JPJournal journal) {
        MarcFactory marcFactory = MarcFactory.newInstance();
        Record marcRecord = marcFactory.newRecord();
        List<DataField> fields = marcRecord.getDataFields();

        addShared(journal, marcFactory, fields, null, "mainPublisher");

        // 260 Publication, Distribution, etc. (Imprint)
        addPublishedDate(journal, marcFactory, fields);

        journal.getDate(JPPeriodicalComponent.DateType.published).ifPresent(date -> {
            DataField dataField = marcFactory.newDataField("260", '#', '#');
            dataField.addSubfield(marcFactory.newSubfield('c', date.toString()));
            fields.add(dataField);
        });

        return marcRecord;
    }

    public static Record convert(JPVolume volume) {
        MarcFactory marcFactory = MarcFactory.newInstance();
        Record marcRecord = marcFactory.newRecord();
        List<DataField> fields = marcRecord.getDataFields();
        addShared(volume, marcFactory, fields, null, null);
        // 260 Publication, Distribution, etc. (Imprint)
        addPublishedDate(volume, marcFactory, fields);
        return marcRecord;
    }

    public static Record convert(JPArticle article) {
        MarcFactory marcFactory = MarcFactory.newInstance();
        Record marcRecord = marcFactory.newRecord();
        List<DataField> fields = marcRecord.getDataFields();

        addShared(article, marcFactory, fields, "author", null);

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
    private static DataField getField(MarcFactory marcFactory, String tag, char ind1, char ind2, char code,
        String value) {
        DataField dataField = marcFactory.newDataField(tag, ind1, ind2);
        dataField.addSubfield(marcFactory.newSubfield(code, value));
        return dataField;
    }

    /**
     * Shared fields for all components.
     *
     * @param component the compontent to parse
     * @param marcFactory marc factory
     * @param fields the fields to enhance
     * @param mainPersonRole name of the person role
     * @param mainInstitutionRole name of the institution role
     */
    private static void addShared(JPPeriodicalComponent component, MarcFactory marcFactory, List<DataField> fields,
        String mainPersonRole, String mainInstitutionRole) {

        // 024 - ID
        fields.add(getField(marcFactory, "024", '8', '0', 'a', component.getId().toString()));

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

        // 856 - URI
        String uri = MCRFrontendUtil.getBaseURL() + "receive/" + component.getId();
        fields.add(getField(marcFactory, "856", '4', '0', 'u', uri));

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
        for (Pair<MCRObjectID, String> person : component.getParticipants(JPObjectType.person)) {
            if (person.getKey().equals(mainPersonID)) {
                continue;
            }
            addPerson("700", marcFactory, fields, person.getKey());
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
        for (Pair<MCRObjectID, String> jpinst : component.getParticipants(JPObjectType.jpinst)) {
            if (jpinst.getKey().equals(mainInstitutionID)) {
                continue;
            }
            addInstitution("710", marcFactory, fields, jpinst.getKey());
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

    private static void addPublishedDate(JPPeriodicalComponent component, MarcFactory marcFactory,
        List<DataField> fields) {
        // 260 Publication, Distribution, etc. (Imprint)
        component.getDate(JPPeriodicalComponent.DateType.published).ifPresent(date -> {
            DataField dataField = marcFactory.newDataField("260", '#', '#');
            dataField.addSubfield(marcFactory.newSubfield('c', date.toString()));
            fields.add(dataField);
        });
    }

    private static void addLanguage(JPPeriodicalComponent component, MarcFactory marcFactory, List<DataField> fields) {
        component.getLanguageCode().ifPresent(language -> {
            DataField languageField = getField(marcFactory, "041", '#', '7', 'a', language);
            languageField.addSubfield(marcFactory.newSubfield('2', "iso639-1"));
            fields.add(languageField);
        });
    }

    private static void addPerson(String marcEntry, MarcFactory marcFactory, List<DataField> fields, MCRObjectID id) {
        JPPerson person = new JPPerson(id);
        Map<String, String> nameMap = person.metaXMLToMap(person.getHeading().orElse(null));
        char firstIndicator = nameMap.containsKey("lastName") ? '1' : '0';
        DataField dataField = marcFactory.newDataField(marcEntry, firstIndicator, '#');
        dataField.addSubfield(marcFactory.newSubfield('a', person.getTitle()));
        person.getId("gnd").ifPresent(gnd -> {
            dataField.addSubfield(marcFactory.newSubfield('0', "(DE-588)" + gnd));
        });
        fields.add(dataField);
    }

    private static void addInstitution(String marcEntry, MarcFactory marcFactory, List<DataField> fields,
        MCRObjectID id) {
        JPInstitution institution = new JPInstitution(id);
        DataField dataField = marcFactory.newDataField(marcEntry, '2', '#');
        dataField.addSubfield(marcFactory.newSubfield('a', institution.getTitle()));
        institution.getId("gnd").ifPresent(gnd -> {
            dataField.addSubfield(marcFactory.newSubfield('0', "(DE-588)" + gnd));
        });
        fields.add(dataField);
    }

    private static void addIdentis(String marcEntry, String idType, List<DataField> fields,
        JPPeriodicalComponent component, MarcFactory marcFactory) {
        component.getIdenti(idType).ifPresent(value -> {
            fields.add(getField(marcFactory, marcEntry, '#', '#', 'a', value));
        });
    }

}
