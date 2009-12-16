package org.mycore.common.xml;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.mycore.common.xml.MCRURIResolver.MCRResolver;

public class FileDeletedResolver implements MCRResolver {
    Logger LOGGER = Logger.getLogger(this.getClass());

    @Override
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

}
