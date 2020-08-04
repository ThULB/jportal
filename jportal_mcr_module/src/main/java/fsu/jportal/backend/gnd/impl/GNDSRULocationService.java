package fsu.jportal.backend.gnd.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;

import fsu.archiv.mycore.sru.impex.pica.model.Datafield;
import fsu.archiv.mycore.sru.impex.pica.model.PicaRecord;
import fsu.archiv.mycore.sru.impex.pica.model.Subfield;
import fsu.jportal.backend.gnd.GNDLocation;
import fsu.jportal.backend.gnd.GNDLocationService;
import fsu.jportal.backend.gnd.GNDLocationServiceException;
import fsu.jportal.util.GndUtil;

/**
 * GND location service using the SRU interface.
 */
public class GNDSRULocationService implements GNDLocationService {

    @Override
    public GNDLocation get(String gndId) throws GNDLocationServiceException {
        try {
            PicaRecord picaRecord = GndUtil.retrieveFromSRU(gndId, "065A");
            if (picaRecord == null) {
                return null;
            }
            GNDLocation location = new GNDLocation();
            location.setId(gndId);
            String label = getLabel(picaRecord);
            if (label != null) {
                location.setLabel(label);
            } else {
                LogManager.getLogger().warn("Unable to get label (065A a) of {}", gndId);
            }
            picaRecord.getValues("042B", "a").forEach(location::addAreaCode);
            String latString = picaRecord.getValue("037H", "f", "A", "dgx");
            String lngString = picaRecord.getValue("037H", "d", "A", "dgx");
            if (latString != null && lngString != null) {
                location.setLatitude(new BigDecimal(latString.substring(1)));
                location.setLongitude(new BigDecimal(lngString.substring(1)));
            }
            return location;
        } catch (Exception exc) {
            throw new GNDLocationServiceException("Unable to retrieve " + gndId, exc);
        }
    }

    private String getLabel(PicaRecord picaRecord) {
        List<Datafield> geographicFields = picaRecord.getDatafieldsByName("065A");
        if (geographicFields.isEmpty()) {
            return null;
        }
        final Datafield datafield = geographicFields.get(0);
        final StringBuffer label = new StringBuffer(get(datafield, "a").orElse(""));
        if (label.length() == 0) {
            return null;
        }
        get(datafield, "g").ifPresent(additional -> label.append(" (").append(additional).append(")"));
        get(datafield, "x").ifPresent(generalSubset -> label.append(" / ").append(generalSubset));
        get(datafield, "z").ifPresent(geographicSubset -> label.append(" (").append(geographicSubset).append(")"));
        return label.toString();
    }

    private Optional<String> get(Datafield datafield, String code) {
        Subfield subfield = datafield.getFirstSubfieldByCode(code);
        return subfield == null ? Optional.empty() : Optional.of(subfield.getValue());
    }

}
