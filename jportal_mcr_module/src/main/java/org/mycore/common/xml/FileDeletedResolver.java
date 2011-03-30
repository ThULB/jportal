package org.mycore.common.xml;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.transform.JDOMSource;

public class FileDeletedResolver implements URIResolver {
    Logger LOGGER = Logger.getLogger(this.getClass());

    public Element resolveElement(String URI) throws Exception {
        if(URI.contains("_derivate_")){
            String uri = "mcrobject:" + URI.substring(URI.indexOf(":") + 1);;
            return MCRURIResolver.instance().resolve(uri);
        }
        
        return createFakeDerivate();
    }

    private Element createFakeDerivate() {
        Element mycorederivate = new Element("mycorederivate");
        Element service = new Element("service");
        mycorederivate.addContent(service);
        return mycorederivate;
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        try {
            return new JDOMSource(resolveElement(href));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }

}
