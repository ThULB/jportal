package fsu.jportal.resolver;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.LogManager;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.Record;
import org.mycore.datamodel.metadata.MCRObjectID;

import fsu.jportal.backend.marc.Marc21Converter;
import fsu.jportal.util.JPComponentUtil;

/**
 * Converts jportal objects (jparticle, jpvolume, jpjournal) to their marcxml version.
 *
 * @author Matthias Eichner
 */
@URIResolverSchema(schema = "marcxml")
public class Marc21Resolver implements URIResolver {

    @Override
    public Source resolve(final String href, String base) throws TransformerException {
        String id = href.substring(href.indexOf(":") + 1);
        MCRObjectID mcrID = MCRObjectID.getInstance(id);
        return JPComponentUtil.getPeriodical(mcrID).map(periodical -> {
            Record record = Marc21Converter.convert(periodical);
            try {
                return getRecordAsString(record);
            } catch (Exception exc) {
                LogManager.getLogger().error("Unable to convert to marc21 record " + href);
                return null;
            }
        }).map(recordAsString -> new StreamSource(new StringReader(recordAsString))).orElse(null);

    }

    private String getRecordAsString(Record record) throws UnsupportedEncodingException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MarcXmlWriter xmlWriter = new MarcXmlWriter(out, "UTF-8", true);
        xmlWriter.write(record);
        xmlWriter.close();
        return new String(out.toByteArray(), StandardCharsets.UTF_8.name());
    }

}
