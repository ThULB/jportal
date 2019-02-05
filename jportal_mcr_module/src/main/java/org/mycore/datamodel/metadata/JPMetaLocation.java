package org.mycore.datamodel.metadata;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jdom2.Element;
import org.mycore.common.MCRException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import fsu.jportal.backend.gnd.GNDLocation;

/**
 * Location metadata of jportal. Stores an id (can be a gnd), a label, a latitude longitude values for geographic
 * references and an area code.
 *
 * <p>Be aware that the geographic reference, in difference to the MCRMetaSpatial, is not required. An id
 * or label is enough.</p>
 *
 * @author Matthias Eichner
 */
public class JPMetaLocation extends MCRMetaDefault {

    protected String id;

    protected String label;

    protected List<String> areaCodes;

    protected MCRMetaSpatial spatial;

    public JPMetaLocation() {
        super();
    }

    public JPMetaLocation(String subtag, String defaultLanguage, String type, Integer inherited) {
        super(subtag, defaultLanguage, type, inherited);
        this.areaCodes = new ArrayList<>();
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

    public void addAreaCode(String areaCode) {
        this.areaCodes.add(areaCode);
    }

    public List<String> getAreaCodes() {
        return this.areaCodes;
    }

    /**
     * Returns the spatial data. Two entries build a point. The first is always the latitude and the second one
     * is always the longitude value.
     *
     * @return list of the spatial data
     */
    public List<BigDecimal> getData() {
        if (this.spatial == null) {
            this.spatial = new MCRMetaSpatial("temp", null, 0);
        }
        return this.spatial.getData();
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
        String areaCodeString = element.getAttributeValue("areaCode");
        if (areaCodeString != null) {
            this.areaCodes = Arrays.asList(areaCodeString.split(" "));
        }
    }

    /**
     * Creates the JSON representation. Extends the {@link MCRMetaDefault#createJSON()} method
     * with the following data.
     *
     * <pre>
     *   {
     *     id: 4028557-1,
     *     label: Jena,
     *     data: [50.92878, 11.5899],
     *     areaCode: [XB-IN, XB-PK]
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
        if (!this.areaCodes.isEmpty()) {
            JsonArray areaCodeArray = new JsonArray();
            this.areaCodes.forEach(areaCodeArray::add);
            json.add("areaCode", areaCodeArray);
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
        if (!this.areaCodes.isEmpty()) {
            xml.setAttribute("areaCode", String.join(" ", this.areaCodes));
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
        this.getAreaCodes().forEach(location::addAreaCode);
        return location;
    }

    /**
     * Creates a new JPMetaLocation using the GNDLocation class.
     *
     * @param subTag the subtag to use e.g. linkedLocation
     * @param gndLocation the gnd location as source
     * @return a new meta location object
     */
    public static JPMetaLocation of(String subTag, GNDLocation gndLocation) {
        JPMetaLocation metaLocation = new JPMetaLocation(subTag, null, null, 0);
        metaLocation.setId(gndLocation.getId());
        gndLocation.getLabel().ifPresent(metaLocation::setLabel);
        gndLocation.getAreaCodes().forEach(metaLocation::addAreaCode);
        gndLocation.getLocation().ifPresent(locationPair -> {
            metaLocation.getData().add(locationPair.getKey());
            metaLocation.getData().add(locationPair.getValue());
        });
        return metaLocation;
    }

}
