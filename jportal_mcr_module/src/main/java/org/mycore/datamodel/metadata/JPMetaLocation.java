package org.mycore.datamodel.metadata;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jdom2.Element;
import org.mycore.common.MCRException;

/**
 * Location metadata of jportal. Stores an id (can be a gnd), a text and latitude longitude values for geographic
 * references.
 *
 * <p>Be aware that the geographic reference, in difference to the MCRMetaSpatial, is not required. An id
 * or label is enough.</p>
 *
 * @author Matthias Eichner
 */
public class JPMetaLocation extends MCRMetaDefault {

    protected String id;

    protected String label;

    protected MCRMetaSpatial spatial;

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

    /**
     * Returns the spatial data. Two entries build a point. The first is always the latitude and the second one
     * is always the longitude value.
     *
     * @return list of the spatial data
     */
    public List<BigDecimal> getData() {
        return this.spatial != null ? this.spatial.getData() : new ArrayList<>();
    }

    public void setData(List<BigDecimal> data) {
        this.spatial = new MCRMetaSpatial();
        this.spatial.setData(data);
    }

    @Override
    public void setFromDOM(Element element) throws MCRException {
        super.setFromDOM(element);
        if (element.getText() != null && !element.getText().isEmpty()) {
            this.spatial = new MCRMetaSpatial();
            this.spatial.setFromDOM(element);
        }
        this.id = element.getAttributeValue("id");
        this.label = element.getAttributeValue("label");
    }

    /**
     * Creates the JSON representation. Extends the {@link MCRMetaDefault#createJSON()} method
     * with the following data.
     *
     * <pre>
     *   {
     *     id: 4028557-1,
     *     label: Jena,
     *     data: [50.92878, 11.5899]
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
        if (this.spatial != null) {
            JsonArray dataArray = new JsonArray();
            getData().forEach(dataArray::add);
            json.add("data", dataArray);
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
        if (this.spatial != null) {
            xml.setText(this.spatial.createXML().getText());
        }
        return xml;
    }

    @Override
    public JPMetaLocation clone() {
        JPMetaLocation location = new JPMetaLocation(getSubTag(), getLang(), getType(), getInherited());
        location.setData(this.getData());
        location.setId(this.getId());
        location.setLabel(this.getLabel());
        return location;
    }

}
