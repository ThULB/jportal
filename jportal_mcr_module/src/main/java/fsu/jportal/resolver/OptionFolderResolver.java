package fsu.jportal.resolver;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Enumeration;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.transform.JDOMSource;

import fsu.jportal.nio.JarResource;

@URIResolverSchema(schema = "templatesOption")
public class OptionFolderResolver implements URIResolver {

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        Element folderList = new Element("folderList");
        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/resources/jp_templates");
            while (resources.hasMoreElements()) {
                URL url = (URL) resources.nextElement();
                JarResource jarResource = new JarResource(url);
                
                for (Path child : jarResource.listFiles()) {
                    String folderName = child.getFileName().getFileName().toString().replace("/", "");
                    folderList.addContent(createItemElement(folderName));
                    
                }
                jarResource.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ContenComparator comparator = new ContenComparator();
        folderList.sortChildren(comparator);
        return new JDOMSource(new Document(folderList));
    }

    static class ContenComparator implements Comparator<Element> {
        @Override
        public int compare(Element elem1, Element elem2) {
            String value1 = elem1.getAttribute("value").getValue();
            String value2 = elem2.getAttribute("value").getValue();
            
            return value1.compareTo(value2);
        }

        

    }

    private Element createItemElement(String folderName) {
        Element item = new Element("option").setAttribute("value", folderName);
        Element label = new Element("label").setAttribute("lang", "de", Namespace.XML_NAMESPACE);
        label.setText(folderName);
        item.addContent(label);
        return item;
    }
}
