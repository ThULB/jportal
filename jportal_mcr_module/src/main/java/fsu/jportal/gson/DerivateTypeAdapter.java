package fsu.jportal.gson;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRJSONTypeAdapter;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.xml.sax.SAXException;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

public class DerivateTypeAdapter extends MCRJSONTypeAdapter<FileNodeWraper>{
    private static final String dateFormat = "dd.MM.yyyy HH:mm:ss";

    private static final DateFormat dateFormatter = new SimpleDateFormat(dateFormat);

    @Override
    public JsonElement serialize(FileNodeWraper deriv, Type typeOfSrc, JsonSerializationContext context) {
        MCRDirectory dir = deriv.getDir();
        JsonObject dirJSON = context.serialize(dir, MCRFilesystemNode.class).getAsJsonObject();
        MCRFilesystemNode[] children = dir.getChildren();
        JsonArray childrenJSON = new JsonArray();
        String derivID = deriv.getDerivID();
        try {
            Document derivXML = MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(derivID));
            XPathFactory xPathFactory = XPathFactory.instance();
            XPathExpression<Attribute> attr = xPathFactory.compile("/mycorederivate/derivate/internals/internal/@maindoc", Filters.attribute());
            String maindoc = attr.getExpression();
            
            for (MCRFilesystemNode childNode : children) {
                JsonElement childNodeJSON = context.serialize(childNode, MCRFilesystemNode.class);
                if(childNode.getName().equals(maindoc)){
                    childNodeJSON.getAsJsonObject().addProperty("maindoc", true);
                }
                
                childrenJSON.add(childNodeJSON);
            }
            
            dirJSON.add("children", childrenJSON);
        } catch (IOException | JDOMException | SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return dirJSON;
    }

    @Override
    public FileNodeWraper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        // TODO Auto-generated method stub
        return null;
    }
    
}