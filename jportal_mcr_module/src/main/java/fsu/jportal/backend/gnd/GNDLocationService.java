package fsu.jportal.backend.gnd;

/**
 * Location based service for the GND. Returns GNDLocation objects.
 *
 * @author Matthias Eichner
 */
public interface GNDLocationService {

    /**
     * Returns a GNDLocation object for the given gnd identifier.
     * 
     * @param gndId the gnd identifier
     * @return a GNDLocation object or null if there is no location with this gnd identifier
     * @throws GNDLocationServiceException something went wrong querying the service
     */
    GNDLocation get(String gndId) throws GNDLocationServiceException;

}
