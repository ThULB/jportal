package fsu.jportal.frontend.xeditor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.frontend.xeditor.MCRXEditorPostProcessor;

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

    private static final XPathExpression<Element> DATE_EXP;

    static {
        DATE_EXP = XPathFactory.instance().compile("metadata/dates", Filters.element());
    }

    public Document process(Document xml) {
        Element rootElement = xml.getRootElement();
        handleDates(rootElement);
        return xml;
    }

    @Override
    public void setAttributes(Map<String, String> attributeMap) {
    }

    private void handleDates(Element rootElement) {
        Element datesElement = DATE_EXP.evaluateFirst(rootElement);
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

}
