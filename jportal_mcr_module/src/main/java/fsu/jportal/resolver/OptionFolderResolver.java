package fsu.jportal.resolver;

import java.io.IOException;
import java.nio.file.Path;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.transform.JDOMSource;

import fsu.jportal.nio.JarResource;

public class OptionFolderResolver implements URIResolver{

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        Element folderList = new Element("folderList");
        try {
            JarResource jarResource = new JarResource("/META-INF/resources/jp_templates");
            for (Path child : jarResource.listFiles()) {
                String folderName = child.getFileName().getFileName().toString();
                folderList.addContent(createItemElement(folderName));
                
            }
            jarResource.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new JDOMSource(new Document(folderList));
    }

    private Element createItemElement(String folderName) {
        Element item = new Element("option").setAttribute("value", folderName);
        Element label = new Element("label").setAttribute("lang", "de", Namespace.XML_NAMESPACE);
        label.setText(folderName);
        item.addContent(label);
        return item;
    }
}
