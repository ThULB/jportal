package fsu.jportal.backend.gnd;

import java.util.Collection;
import java.util.Set;

/**
 * Service interface to resolve GND GEOGRAPHIC AREA CODES.
 * <p>
 *     See:
 *     <a href="https://d-nb.info/standards/vocab/gnd/geographic-area-code.html">geographic area codes documentation</a>
 * </p>
 * <p>The area codes using ISO3166-2</p>
 */
public interface GNDAreaCodesService {

    /**
     * Returns all gnd identifiers for a single area code.
     *
     * @param areaCode the area code e.g. XA-DE
     * @return list of gnd identifiers (e.g. XA-DE will return two gnd identifiers -> XA = europe & XA-DE = germany)
     */
    Set<String> get(String areaCode);

    /**
     * Returns a set of gnd identifiers for multiple area codes.
     * 
     * @param areaCodes collection of area codes e.g. XA-DE, XB-MN
     * @return list of gnd identifiers (e.g. XA-DE & XB-MN will return four gnd identifiers 
     * -> XA = europe & XA-DE = germany & XB = asia & XB-MN = mongolia)
     */
    Set<String> get(Collection<String> areaCodes);

}
