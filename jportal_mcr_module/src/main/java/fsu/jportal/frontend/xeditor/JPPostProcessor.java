package fsu.jportal.frontend.xeditor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.inject.MCRInjectorConfig;
import org.mycore.datamodel.metadata.JPMetaLocation;
import org.mycore.frontend.xeditor.MCRXEditorPostProcessor;

import fsu.jportal.backend.gnd.GNDAreaCodesService;
import fsu.jportal.backend.gnd.GNDLocation;
import fsu.jportal.backend.gnd.GNDLocationService;

/**
 * XEditor post processor for jportal.
 *
 * <ul>
 *     <li>removes date's with only the @type attribute and not @date or @from</li>
 *     <li>removes multiple metadata/dates/date with the same type. e.g. only one @type="published" is allowed</li>
 * </ul>
 *
 * @author Matthias Eichner
 */
public class JPPostProcessor implements MCRXEditorPostProcessor {

    private static final XPathExpression<Element> DATES_EXP;

    private static final XPathExpression<Element> LINKED_LOCATIONS_EXP;

    static {
        DATES_EXP = XPathFactory.instance().compile("metadata/dates", Filters.element());
        LINKED_LOCATIONS_EXP = XPathFactory.instance().compile("metadata/linkedLocations",
            Filters.element());
    }

    public Document process(Document xml) {
        Element rootElement = xml.getRootElement();
        handleDates(rootElement);
        handleLinkedLocations(rootElement);
        return xml;
    }

    @Override
    public void setAttributes(Map<String, String> attributeMap) {
    }

    private void handleDates(Element rootElement) {
        Element datesElement = DATES_EXP.evaluateFirst(rootElement);
        if (datesElement == null) {
            return;
        }
        List<Element> dates = datesElement.getChildren();
        Map<String, Element> typeMap = new HashMap<>();
        for (Element date : dates) {
            String dateValue = date.getAttributeValue("date");
            String fromValue = date.getAttributeValue("from");
            if ((dateValue == null || dateValue.equals("")) && (fromValue == null || fromValue.equals(""))) {
                continue;
            }
            typeMap.put(date.getAttributeValue("type"), date);
        }
        datesElement.removeContent();
        for (Element date : typeMap.values()) {
            datesElement.addContent(date);
        }
    }

    private void handleLinkedLocations(Element rootElement) {
        Element linkedLocations = LINKED_LOCATIONS_EXP.evaluateFirst(rootElement);
        if (linkedLocations == null) {
            return;
        }

        GNDLocationService locationService = MCRInjectorConfig.injector().getInstance(GNDLocationService.class);
        GNDAreaCodesService areaCodesService = MCRInjectorConfig.injector().getInstance(GNDAreaCodesService.class);

        // get all existing gnd ids of the current element
        Set<String> gndIds = linkedLocations.getChildren().stream()
            .map(linkedLocation -> linkedLocation.getAttributeValue("id"))
            .filter(Objects::nonNull).collect(Collectors.toSet());
        // get corresponding area codes for the gnd ids
        Set<String> areaCodes = gndIds.stream().map(locationService::get).filter(Objects::nonNull)
            .map(GNDLocation::getAreaCode).flatMap(code -> code.map(Stream::of).orElseGet(Stream::empty))
            .collect(Collectors.toSet());
        // use the gnd areaCodesService to get all parent gnd's for the area codes
        Set<String> areaCodeGndIds = areaCodesService.get(areaCodes);
        // build new gnd ids which needs to be add to the linkedLocations element
        Set<String> completeGNDIds = new HashSet<>(areaCodeGndIds);
        completeGNDIds.addAll(gndIds);

        // rebuild linked locations
        linkedLocations.removeContent();
        for (String gndId : completeGNDIds) {
            try {
                GNDLocation gndLocation = locationService.get(gndId);
                if (gndLocation == null) {
                    LogManager.getLogger().warn("Unable to get location for {}", gndId);
                    continue;
                }
                JPMetaLocation metaLocation = JPMetaLocation.of("linkedLocation", gndLocation);
                linkedLocations.addContent(metaLocation.createXML());
            } catch (Exception exc) {
                LogManager.getLogger()
                    .error("Unable to add gnd id '{}' to linked locations for '{}'", gndId,
                        rootElement.getAttributeValue("ID"),
                        exc);
            }
        }
    }

}
