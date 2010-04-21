package org.fsu.jp.urn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.mycore.common.MCRConfiguration;
import org.mycore.datamodel.common.MCRLinkTableManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

public class URNXalanExtensions {

    private static Logger LOGGER = Logger.getLogger(URNXalanExtensions.class);

    public NodeIterator getParentDerivates(String objectId) throws ParserConfigurationException {
        return new ParentDerivateNodeIterator(objectId);
    }

    /**
     * @return true if the given object is allowed for urn assignment
     * */
    public static boolean isAllowedObjectForURNAssignment(String objId) {
        if (objId == null) {
            return false;
        }
        try {
            MCRObjectID obj = new MCRObjectID(objId);
            String type = obj.getTypeId();
            return isAllowedObject(type);

        } catch (Exception ex) {
            LOGGER.error("Error while checking object " + objId + " is allowed for urn assignment");
            return false;
        }
    }

    /**
     * Reads the property "URN.Enabled.Objects".
     * 
     * @param givenType
     *            the type of the mycore object to check
     * @return <code>true</code> if the given type is in the list of allowed
     *         objects, <code>false</code> otherwise
     */
    private static boolean isAllowedObject(String givenType) {
        if (givenType == null)
            return false;

        String propertyName = "URN.Enabled.Objects";
        String propertyValue = MCRConfiguration.instance().getString(propertyName, null);
        if (propertyValue == null || propertyValue.length() == 0) {
            LOGGER.info("URN assignment disabled as the property \"" + propertyName + "\" is not set");
            return false;
        }

        String[] allowedTypes = propertyValue.split(",");
        for (String current : allowedTypes) {
            if (current.trim().equals(givenType.trim())) {
                return true;
            }
        }
        LOGGER.info("URN assignment disabled as the object type " + givenType + " is not in the list of allowed objects. See property \""
                + propertyName + "\"");
        return false;
    }

    private static final class ParentDerivateNodeIterator implements NodeIterator {
        int currentPos = -1;

        List<String> parentIds;

        DocumentBuilderFactory factory;

        DocumentBuilder builder;

        Document document;

        public ParentDerivateNodeIterator(String objectID) throws ParserConfigurationException {
            parentIds = new ArrayList<String>();
            parentIds.add(objectID);
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            document = builder.newDocument();
        }

        public void detach() {
        }

        public boolean getExpandEntityReferences() {
            return true;
        }

        public NodeFilter getFilter() {
            return null;
        }

        public Node getRoot() {
            return null;
        }

        public int getWhatToShow() {
            return NodeFilter.SHOW_ALL;
        }

        public Node nextNode() throws DOMException {
            currentPos++;
            if (parentIds.size() <= currentPos) {
                //fetch node first
                MCRObject obj = new MCRObject();
                obj.receiveFromDatastore(parentIds.get(currentPos - 1));
                MCRObjectID parentId = obj.getStructure().getParentID();
                if (parentId != null) {
                    parentIds.add(parentId.getId());
                } else
                    return null;
            }
            return getNode();
        }

        public Node previousNode() throws DOMException {
            currentPos--;
            if (currentPos < 0)
                return null;
            return getNode();
        }

        private Node getNode() {
            String objectID = parentIds.get(currentPos);
            Collection<String> derivates = MCRLinkTableManager.instance().getDestinationOf(objectID, "derivate");
            Element object = document.createElement("mycoreobject");
            object.setAttribute("id", objectID);
            for (String derivate : derivates) {
                Element derElement = document.createElement("derivate");
                derElement.setAttribute("id", derivate);
                object.appendChild(derElement);
            }
            return object;
        }
    }
}
