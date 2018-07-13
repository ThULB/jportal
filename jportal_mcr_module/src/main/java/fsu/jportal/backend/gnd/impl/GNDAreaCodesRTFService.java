package fsu.jportal.backend.gnd.impl;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import fsu.jportal.backend.gnd.GNDAreaCodesService;

/**
 * <p>
 *     Reads and parses the GND GEOGRAPHIC AREA CODES of 
 *     <a href="https://d-nb.info/standards/vocab/gnd/geographic-area-code.rdf">this rdf file</a>
 * </p>
 * <p>
 *     For more information read:
 *     <a href="https://d-nb.info/standards/vocab/gnd/geographic-area-code.html">geographic area codes documentation</a>
 * </p>
 */
@Singleton
public class GNDAreaCodesRTFService implements GNDAreaCodesService {

    private Map<String, String> data;

    @Inject
    public GNDAreaCodesRTFService() {
        this.data = new HashMap<>();
        prefill();
        try {
            URL rdfURL = new URL("https://d-nb.info/standards/vocab/gnd/geographic-area-code.rdf");
            SAXBuilder saxBuilder = new SAXBuilder();
            Document rdfDocument = saxBuilder.build(rdfURL);
            Element rootElement = rdfDocument.getRootElement();
            Namespace skosNS = rootElement.getNamespace("skos");
            Namespace rdfNS = rootElement.getNamespace("rdf");
            Namespace rdfsNS = rootElement.getNamespace("rdfs");
            rootElement.getChildren("Concept", skosNS).forEach(concept -> {
                String areaCode = concept.getAttributeValue("about", rdfNS);
                String gndId = concept.getChildren("seeAlso", rdfsNS).stream()
                    .map(seeAlso -> seeAlso.getAttributeValue("resource", rdfNS))
                    .filter(resource -> resource.contains("//d-nb.info/gnd/"))
                    .map(resource -> resource.substring(resource.lastIndexOf('/') + 1)).findFirst().orElse(null);
                if (areaCode != null && gndId != null) {
                    this.data.put(areaCode.substring(areaCode.lastIndexOf('#') + 1), gndId);
                }
            });
        } catch (Exception exc) {
            LogManager.getLogger()
                .error("Unable to load or parse 'https://d-nb.info/standards/vocab/gnd/geographic-area-code.rdf'", exc);
        }
    }

    /**
     * The RDF file is not complete. There are some links missing between "area code" and "gnd id". This manual prefill
     * enhances those missing parts.
     */
    protected void prefill() {
        this.data.put("XE", "4044257-3");
        this.data.put("XA-AAAT", "4043271-3");
    }

    @Override
    public Set<String> get(String areaCode) {
        Set<String> gndIds = new HashSet<>();
        do {
            String gndId = this.data.get(areaCode);
            if (gndId != null) {
                gndIds.add(gndId);
            }
            int endIndex = areaCode.lastIndexOf('-');
            if (endIndex == -1) {
                break;
            }
            areaCode = areaCode.substring(0, endIndex);
        } while (true);
        return gndIds;
    }

    @Override
    public Set<String> get(Collection<String> areaCodes) {
        return areaCodes.stream().flatMap(code -> get(code).stream()).collect(Collectors.toSet());
    }

}
