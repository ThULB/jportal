package fsu.jportal.resolver;

import fsu.jportal.backend.marc.Marc21Converter;
import fsu.jportal.util.JPComponentUtil;
import org.jdom2.input.SAXBuilder;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.Record;
import org.mycore.datamodel.metadata.MCRObjectID;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class Marc21Resolver implements URIResolver {

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        /*
        try {
            href = href.substring(href.indexOf(":") + 1);
            MCRObjectID mcrID = MCRObjectID.getInstance(href);
            return JPComponentUtil.getPeriodical(mcrID).map(component -> {
                return Marc21Converter.convert(component);
            }).map(record -> {
                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    MarcXmlWriter xmlWriter = new MarcXmlWriter(out, "UTF-8", true);
                    xmlWriter.write(record);
                    xmlWriter.close();
                    return new String(out.toByteArray(), StandardCharsets.UTF_8.name());
                } catch(Exception exc) {

                }
            }).map(string -> {
                SAXBuilder builder = new SAXBuilder();
            });
        } catch(Exception exc) {
            throw new TransformerException("Unable to convert to marc21 record " + href, exc);
        }
        */
        return null;
    }

}
