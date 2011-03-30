package org.mycore.common.xml;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.transform.JDOMSource;
import org.jdom.xpath.XPath;

public class PersonNameResolver implements URIResolver {

    public Element resolveElement(String URI) throws Exception {
        String uri = "mcrobject:" + URI.substring(URI.indexOf(":") + 1);
        Element personXML = getPersonXMl(uri);
        List<Element> nameList = new LinkedList<Element>();
        nameList.add(getHeading(personXML));
        nameList.addAll(getAlternative(personXML));
        return createNameListElem(nameList);
    }

    private Element createNameListElem(List<Element> nameList) {
        NameBuffer nameBuffer = new NameBuffer();
        for (Element name : nameList) {
            nameBuffer.addNamePart(createNameString(name));
        }
        
        return new Element("nameList").addContent(nameBuffer.toString());
    }

    private String createNameString(Element nameElem) {
        String lastName = nameElem.getChildText("lastName");
        String firstName = nameElem.getChildText("firstName");
        String name = nameElem.getChildText("name");
        
        NameBuffer nameBuffer = new NameBuffer();
        nameBuffer.addNamePart(lastName);
        nameBuffer.addNamePart(firstName);
        nameBuffer.addNamePart(name);
        
        return nameBuffer.toString();
    }

    private Collection<? extends Element> getAlternative(Element personXML) throws JDOMException {
        XPath xPath = XPath.newInstance("/mycoreobject/metadata/def.alternative/alternative");
        List<Element> alternativeList = xPath.selectNodes(personXML);
        return alternativeList;
    }

    private Element getHeading(Element personXML) throws JDOMException {
        XPath headingXPath = XPath.newInstance("/mycoreobject/metadata/def.heading/heading");
        Element name = (Element) headingXPath.selectSingleNode(personXML);
        return name;
    }

    protected Element getPersonXMl(String uri) {
        Element objXML = MCRURIResolver.instance().resolve(uri);
        return objXML;
    }

    class NameBuffer {
        StringBuffer strBuffer = new StringBuffer();
        
        public void addNamePart(String namePart) {
            if(namePart != null){
                strBuffer.append(namePart + " ");
            }
        }
        
        @Override
        public String toString() {
            return strBuffer.toString().trim();
        }
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        try {
            return new JDOMSource(resolveElement(href));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
