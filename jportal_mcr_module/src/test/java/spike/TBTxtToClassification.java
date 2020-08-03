package spike;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import static org.mycore.common.MCRConstants.XML_NAMESPACE;
import static org.mycore.common.MCRConstants.XSI_NAMESPACE;

/**
 * Converts the thueringen bibliography to a mycore classification. (One time use class)
 *
 * @author Matthias Eichner
 */
public class TBTxtToClassification {

    private static Document toXML(String path) throws IOException {
        // build document
        Document document = new Document(new Element("mycoreclass"));
        Element root = document.getRootElement();
        root.setAttribute("noNamespaceSchemaLocation", "MCRClassification.xsd", XSI_NAMESPACE);
        root.setAttribute("ID", "urmel_class_00000105");
        addLabel(root, "Thüringen-Bibliographie (Thuringica)");
        Element categories = new Element("categories");
        root.addContent(categories);

        // parse
        List<String> lines = Files.readAllLines(Paths.get(path));
        int index = 0;
        Element mainCategory = null;
        for (String line : lines) {
            if (line.equals(",")) {
                index++;
                continue;
            }
            System.out.println(line);
            String[] lineParts = line.split(",");
            if (lineParts.length == 1) {
                String text = getText(lineParts[0]);
                String categId = lines.get(index + 2).substring(1, 3);
                mainCategory = addCategory(categId, categId + " " + text);
                categories.addContent(mainCategory);
            } else if (mainCategory != null) {
                String categId = getText(lineParts[0]);
                String text = getText(lineParts[1]);
                Element subCategory = addCategory(categId, categId + " " + text);
                mainCategory.addContent(subCategory);
            } else {
                throw new RuntimeException("Invalid line " + line + " at index " + index);
            }
            index++;
        }
        return document;
    }

    private static Element addCategory(String categId, String text) {
        Element category = new Element("category");
        category.setAttribute("ID", categId);
        addLabel(category, text);
        return category;
    }

    private static String getText(String linePart) {
        return linePart.substring(1, linePart.length() - 1);
    }

    private static void addLabel(Element e, String text) {
        Element label = new Element("label");
        label.setAttribute("lang", "de", XML_NAMESPACE);
        label.setAttribute("text", text);
        e.addContent(label);
    }

    public static void main(String[] args) throws Exception {
        String path = args[0];
        Document doc = toXML(path);
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        out.output(doc, System.out);
    }

}
