package fsu.jportal.backend.gnd.impl;

import java.math.BigDecimal;

import org.apache.logging.log4j.LogManager;

import fsu.archiv.mycore.sru.impex.pica.model.PicaRecord;
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
            String label = picaRecord.getValue("065A", "a");
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

}
