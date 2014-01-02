package fsu.jportal.resolver;

import java.io.File;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.config.MCRConfiguration;

public class ListFolderResolver implements URIResolver{

    private static final String PREFIX = "templates:";
    private static final String PROPPREFIX = "prop:";

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        href = getPath(href);
        Element folderList = new Element("folderList");
        for (File file : new File(href).listFiles()) {
            String folderName = file.getName();
            folderList.addContent(createItemElement(folderName));
        }
        return new JDOMSource(new Document(folderList));
    }

    private String getPath(String href) {
        if(href.startsWith(PREFIX)){
            href = href.replaceAll(PREFIX, "");
        }
        
        if(href.startsWith(PROPPREFIX)){
            String property = href.replaceAll(PROPPREFIX, "");
            href = MCRConfiguration.instance().getString(property);
        }
        return href;
    }

    private Element createItemElement(String folderName) {
        Element item = new Element("item").setAttribute("value", folderName);
        Element label = new Element("label").setAttribute("lang", "de", Namespace.XML_NAMESPACE);
        label.setText(folderName);
        item.addContent(label);
        return item;
    }

}
