package org.mycore.datamodel.metadata;

import java.util.ArrayList;

import com.google.gson.JsonObject;
import org.jdom2.Element;
import org.mycore.common.MCRException;

/**
 * Location metadata of jportal. Stores an id (can be a gnd), a text and latitude longitude values for geographic
 * references. This class is extends MCRMetaSpatial.
 *
 * @author Matthias Eichner
 */
public class JPMetaLocation extends MCRMetaSpatial {

    protected String id;

    protected String label;

    public JPMetaLocation() {
        super();
    }

    public JPMetaLocation(String subtag, String defaultLanguage, String type, Integer inherited) {
        super(subtag, defaultLanguage, type, inherited);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public void setFromDOM(Element element) throws MCRException {
        super.setFromDOM(element);
        this.id = element.getAttributeValue("id");
        this.label = element.getAttributeValue("label");
    }

    /**
     * Creates the JSON representation. Extends the {@link MCRMetaSpatial#createJSON()} method
     * with the following data.
     *
     * <pre>
     *   {
     *     id: 4028557-1,
     *     label: Jena
     *   }
     * </pre>
     *
     */
    @Override
    public JsonObject createJSON() {
        JsonObject json = super.createJSON();
        if (this.id != null) {
            json.addProperty("id", this.id);
        }
        if (this.label != null) {
            json.addProperty("label", this.label);
        }
        return json;
    }

    @Override
    public Element createXML() throws MCRException {
        Element xml = super.createXML();
        if (this.id != null) {
            xml.setAttribute("id", this.id);
        }
        if (this.label != null) {
            xml.setAttribute("label", this.label);
        }
        return xml;
    }

    @Override
    public MCRMetaSpatial clone() {
        JPMetaLocation location = new JPMetaLocation(getSubTag(), getLang(), getType(), getInherited());
        location.setData(new ArrayList<>(this.getData()));
        location.setId(this.getId());
        location.setLabel(this.getLabel());
        return location;
    }

}
