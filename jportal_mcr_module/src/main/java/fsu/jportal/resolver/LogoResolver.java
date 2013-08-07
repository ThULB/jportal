package fsu.jportal.resolver;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.jdom2.transform.JDOMSource;

import fsu.jportal.resources.MODSLogoResource;

public class LogoResolver implements URIResolver {

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        /*
         * well, this is more like a hack, im pretty sure the MODSLogoResource
         * is not used otherwise, so we should merge the code
         */
        String journalID = href.substring(href.indexOf(":") + 1);
        return new JDOMSource(MODSLogoResource.get(journalID));
    }

}
