package fsu.jportal.frontend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRObjectUtils;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.frontend.cli.MCRClassification2Commands;
import org.mycore.frontend.cli.MCRDerivateCommands;
import org.mycore.frontend.cli.MCRObjectCommands;

import fsu.jportal.frontend.cli.ObjectTools;

public class RecursiveObjectExporter {
	private static Logger LOGGER = Logger.getLogger(ObjectTools.class.getName());
	private Set<String> storage = new HashSet<String>();
	
	public void start(String objectID, String dest){
		if(!storage.contains(objectID)){
			MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(objectID);
			saveObject(mcrObject, dest);
			storage.add(objectID);
		}
	}
	
	public void saveObject(MCRObject mcrObject, String dest) {
		String type = mcrObject.getId().getTypeId();
		String location = dest + "/" + type;
		createDir(location);
		MCRObjectCommands.export(mcrObject.getId().toString(), location, "");
		
		saveObjChildren(mcrObject, dest);
		saveExternObject(mcrObject, dest);
	}
	
	public void saveObjChildren(MCRObject parent, String dest) {
		List<MCRObject> mcrchildren = MCRObjectUtils.getDescendants(parent);
		for (MCRObject mcrchild : mcrchildren) {
			saveObject(mcrchild, dest);
		}
	}
	
	public void saveExternObject(MCRObject mcrObject, String dest) {
    Element metadataXML = mcrObject.getMetadata().createXML();
    Element structureXML = mcrObject.getStructure().createXML();
    
    saveMetaLinkChilds(dest, metadataXML);
    saveMetaLinkChilds(dest, structureXML);
    saveClassification(dest, metadataXML);
	}

	private void saveClassification(String dest, Element metadataXML) {
		XPathExpression<Attribute> participantXpath = XPathFactory.instance().compile("*[@class='MCRMetaClassification']/*/@classid", Filters.attribute(), null);
  	for (Attribute attribute : participantXpath.evaluate(metadataXML)) {
  		if(!storage.contains(attribute.getValue())) {
  			try {
  				MCRClassification2Commands.export(attribute.getValue(), dest + "/classification", "");
  			} catch (Exception e) {
  				LOGGER.error("Failed to export Classification: " + e.getMessage());
  			}
  			storage.add(attribute.getValue());
  		}
    }
	}

	private void saveMetaLinkChilds(String dest, Element metadataXML) {
		XPathExpression<Element> childElements = XPathFactory.instance().compile("*[@class='MCRMetaLinkID']/*", Filters.element(), null, MCRConstants.XLINK_NAMESPACE);
    for (Element element : childElements.evaluate(metadataXML)) {
    	String href = element.getAttributeValue("href", MCRConstants.XLINK_NAMESPACE);
			if(element.getName().equals("derobject")){
				String derivateDir = dest + "/derivate";
				createDir(derivateDir);
    		MCRDerivateCommands.export(href, derivateDir, "");
    	} else {
    		start(href, dest);
    	}
  		storage.add(href);
    }
	}

	public void createDir(String dest) {
		Path classiDir = Paths.get(dest);
		if(!Files.exists(classiDir)) {
			try {
				Files.createDirectories(classiDir);
			} catch (IOException e) {
				LOGGER.error("Unable to create Directory: " + e.getMessage());
			}
		}
	}
}