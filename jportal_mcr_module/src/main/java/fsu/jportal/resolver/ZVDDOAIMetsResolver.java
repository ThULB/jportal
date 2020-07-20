package fsu.jportal.resolver;

import java.text.MessageFormat;
import java.util.HashMap;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.xml.MCRURIResolver;

import fsu.jportal.mets.ZVDDMetsGenerator;
import fsu.jportal.util.GroupPattern;

/**
 * Created by chi on 20.07.20
 *
 * @author Huu Chi Vu
 */
@URIResolverSchema(schema = "zvddOai")
public class ZVDDOAIMetsResolver implements URIResolver {
    private static Logger LOGGER = LogManager.getLogger();
    @Override
    public Source resolve(String href, String base) throws TransformerException {
        LOGGER.info("Generating zvdd mets " + href + "...");
        HashMap<String, String> parsedHref = parseHref(href);

        if(parsedHref.size() < 2){
            throw new TransformerException("URI not well formed: " + href);
        }

        String mcrId = parsedHref.get("id");
        String format = parsedHref.get("format");

        if(format == null || "".equals(format)){
            format = "mets";
        }

        if(!"mets".equals(format)){
            return resolveWithXSL(mcrId, format);
        }

        ZVDDMetsGenerator generator = new ZVDDMetsGenerator();

        try {
            Document mets = generator.generateMets(mcrId);
            return new JDOMSource(mets);
        } catch (Exception e) {
            throw new TransformerException(e);
        }
    }

    private Source resolveWithXSL(String mcrId, String format) throws TransformerException {
        String oaiId = MCRConfiguration.instance().getString("OAIRepositoryIdentifier", "noOaiIdentifier");
        String hrefTmp = "xslStyle:jp2{0}?identifier={1}:mcrobject:{2}";
        String href = MessageFormat.format(hrefTmp, format, oaiId, mcrId);
        return MCRURIResolver.instance().resolve(href, "");

    }

    private HashMap<String, String> parseHref(String href) {
        String scheme = "(?<scheme>zvddOai)";
        String id = "(?<id>jportal_(jpvolume|jparticle|journal)_[0-9]{8})";
        String format = "(?<format>[\\w\\W]+)";
        String regex = scheme + ":" + id + "\\?" + format;

        GroupPattern pattern = new GroupPattern(regex);

        return pattern.parse(href);
    }
}
