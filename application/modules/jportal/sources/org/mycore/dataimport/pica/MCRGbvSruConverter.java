package org.mycore.dataimport.pica;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.mycore.importer.MCRImportConverter;
import org.mycore.importer.MCRImportField;
import org.mycore.importer.MCRImportRecord;

public class MCRGbvSruConverter implements MCRImportConverter<Element> {

    @SuppressWarnings("unchecked")
    public List<MCRImportRecord> convert(Element toConvert) {
        MCRImportRecord record = new MCRImportRecord("volume");
        List<Element> datafieldList = toConvert.getContent(new ElementFilter("datafield"));
        for(Element datafieldElement : datafieldList) {
            record.addFields(getFields(datafieldElement));
        }
        List<MCRImportRecord> recordList = new ArrayList<MCRImportRecord>();
        recordList.add(record);
        return recordList;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<MCRImportField> getFields(Element datafieldElement) {
        ArrayList<MCRImportField> fields = new ArrayList<MCRImportField>();
        String dataFieldTag = datafieldElement.getAttributeValue("tag");
        String occurrence = datafieldElement.getAttributeValue("occurrence");
        String additional = getAdditionalFields(datafieldElement);
        
        String id = dataFieldTag;
        if(occurrence != null && !occurrence.equals(""))
            id += "/" + occurrence;
        if(additional != null && !additional.equals(""))
            id += "_" + additional;

        List<Element> subfieldList = datafieldElement.getContent(new ElementFilter("subfield"));
        for(Element subfieldElement : subfieldList) {
            String code = subfieldElement.getAttributeValue("code");
            String value = subfieldElement.getText();
            fields.add(new MCRImportField(id + "_" + code, value));
        }
        return fields;
    }

    protected String getAdditionalFields(Element datafieldElement) {
        StringBuffer additionalTag = new StringBuffer();

        // specaial k subtag for persons
        if(datafieldElement.getAttributeValue("tag").equals("028L")) {
            String kAdditional = getKSubtag(datafieldElement);
            if(kAdditional != null)
                additionalTag.append(kAdditional);
        }
        
        return additionalTag.toString();
    }

    @SuppressWarnings("unchecked")
    protected String getKSubtag(Element datafieldElement) {
        List<Element> subfieldList = datafieldElement.getContent(new ElementFilter("subfield"));
        for(Element subfieldElement : subfieldList) {
            String code = subfieldElement.getAttributeValue("code");
            String text = subfieldElement.getText();
            if(code != null && text != null && code.equals("B") && (text.equals("k.") || text.equals("k"))) {
                return "k";
            }
        }
        return null;
    }
    
}