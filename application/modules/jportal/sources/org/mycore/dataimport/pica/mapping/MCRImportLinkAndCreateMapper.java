package org.mycore.dataimport.pica.mapping;

import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;
import org.mycore.common.MCRConstants;
import org.mycore.importer.MCRImportField;
import org.mycore.importer.MCRImportRecord;
import org.mycore.importer.mapping.MCRImportMappingManager;
import org.mycore.importer.mapping.MCRImportObject;
import org.mycore.importer.mapping.mapper.MCRImportAbstractMapper;
import org.mycore.importer.mapping.resolver.metadata.MCRImportMetadataResolver;

public class MCRImportLinkAndCreateMapper extends MCRImportAbstractMapper {

    private static final Logger LOGGER = Logger.getLogger(MCRImportLinkAndCreateMapper.class);

    private static Hashtable<String, Integer> objectCounterTable = new Hashtable<String, Integer>();

    public void map(MCRImportObject importObject, MCRImportRecord record, Element map) {
        super.map(importObject, record, map);

        MCRImportMetadataResolver resolver = createResolverInstance();
        Element metadataChild = resolver.resolve(map, fields);
        if(metadataChild == null)
            return;

        // create a new person object
        MCRImportObject personObject = createPerson();
        if(personObject == null)
            return;
    
        // get the new id
        String id = getId(metadataChild);
        personObject.setId(id);

        // save the person object in file system
        MCRImportMappingManager.getInstance().saveImportObject(personObject, "person");

        // update the link
        metadataChild.setAttribute("href", id, MCRConstants.XLINK_NAMESPACE);
        importObject.addMetadataChild(metadataChild);
    }

    protected String getId(Element metadataChild) {
        String id = metadataChild.getAttributeValue("href", MCRConstants.XLINK_NAMESPACE);

        if(id.equals("ppn_")) {
            Integer oldId = objectCounterTable.get(record.getName());
            if(oldId == null)
                oldId = 0;
            else
                oldId += 1;
            objectCounterTable.put(record.getName(), oldId);
            id += "generated_" + oldId;
        }
        return id;
    }

    protected MCRImportObject createPerson() {
        MCRImportObject newPersonObject = new MCRImportObject();
        newPersonObject.setDatamodelPath(MCRImportMappingManager.getInstance().getConfig().getDatamodelPath() + "datamodel/person.xml");

        Element headingElement = new Element("heading");
        headingElement.setAttribute("lang", "de", Namespace.XML_NAMESPACE);

        boolean someMetadataAdd = false;
        
        // set metadata
        for(MCRImportField field : getFields()) {
            // firstname
            if(field.getId().endsWith("_d")) {
                Element firstNameElement = new Element("firstName");
                firstNameElement.setText(field.getValue());
                headingElement.addContent(firstNameElement);
                someMetadataAdd = true;
            }
            // lastname
            if(field.getId().endsWith("_a")) {
                Element lastNameElement = new Element("lastName");
                lastNameElement.setText(field.getValue());
                headingElement.addContent(lastNameElement);
                someMetadataAdd = true;
            }
            // ppn
            if(field.getId().endsWith("_9")) {
                Element ppnElement = new Element("identifier");
                ppnElement.setAttribute("lang", "de", Namespace.XML_NAMESPACE);
                ppnElement.setAttribute("form", "plain");
                ppnElement.setAttribute("type", "ppn");
                ppnElement.setText(field.getValue());
                newPersonObject.addMetadataChild(ppnElement);
                someMetadataAdd = true;
            }
            // note
            if(field.getId().endsWith("_8")) {
                Element noteElement = new Element("note");
                noteElement.setAttribute("lang", "de", Namespace.XML_NAMESPACE);
                noteElement.setAttribute("form", "plain");
                noteElement.setAttribute("type", "hidden");
                noteElement.setText(field.getValue());
                newPersonObject.addMetadataChild(noteElement);
                someMetadataAdd = true;
            }
        }

        if(headingElement.getContentSize() > 0)
            newPersonObject.addMetadataChild(headingElement);

        if(someMetadataAdd)
            return newPersonObject;
        return null;
    }

    public String getType() {
        return "createAndLink";
    }
}