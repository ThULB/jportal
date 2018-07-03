package fsu.jportal.backend.gnd;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import fsu.jportal.util.Pair;

/**
 * A GNDLocation object consists of a gnd identifier, a label, an area code and a geographic coordinate (lat/lng).
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class GNDLocation {

    private String id;

    private String label;

    private List<String> areaCodes;

    private BigDecimal latitude;

    private BigDecimal longitude;

    public void setId(String id) {
        this.id = id;
        this.areaCodes = new ArrayList<>();
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void addAreaCode(String areaCode) {
        this.areaCodes.add(areaCode);
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public Optional<String> getLabel() {
        return Optional.ofNullable(label);
    }

    public List<String> getAreaCodes() {
        return this.areaCodes;
    }

    public Optional<BigDecimal> getLatitude() {
        return Optional.ofNullable(latitude);
    }

    public Optional<BigDecimal> getLongitude() {
        return Optional.ofNullable(longitude);
    }

    public Optional<Pair<BigDecimal, BigDecimal>> getLocation() {
        return Optional.ofNullable(latitude != null && longitude != null ? new Pair<>(latitude, longitude) : null);
    }

}