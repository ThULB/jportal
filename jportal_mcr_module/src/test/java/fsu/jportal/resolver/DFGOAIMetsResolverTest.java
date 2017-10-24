package fsu.jportal.resolver;

import fsu.jportal.mocks.DerivateXMLToParsedData;
import fsu.jportal.mocks.FakeInputSourceFromZS;
import fsu.jportal.mocks.TransformerList;
import fsu.jportal.xml.dfg.oai.DFGOAIMetsXMLHandler;
import fsu.jportal.xml.stream.DerivateFileInfo;
import fsu.jportal.xml.stream.XMLStreamReaderUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * Created by chi on 14.11.16.
 * @author Huu Chi Vu
 */
@Ignore("Ignore cause it takes to long.")
public class DFGOAIMetsResolverTest {

    private static Logger LOGGER = LogManager.getLogger();

    String volID = "jportal_jparticle_00157238";

    Function<String, Optional<XMLStreamReader>> ZServer;

    TransformerList<XMLStreamReader, DerivateFileInfo> xmlDerivateToParsedData;

    Function<String, Stream<DerivateFileInfo>> derivateSupplier;

    @Before
    public void setUp() throws Exception {
        ZServer = FakeInputSourceFromZS::getReaderFor;

        xmlDerivateToParsedData = new TransformerList<>();
        xmlDerivateToParsedData.add(new DerivateXMLToParsedData());

        derivateSupplier = id -> FakeInputSourceFromZS.getReaderFor(id)
                                                      .map(XMLStreamReaderUtils::toStream)
                                                      .orElseGet(Stream::empty)
                                                      .flatMap(xmlDerivateToParsedData::transformToStream);
    }

    @Test
    public void testResolver() throws Exception {
        UnaryOperator<String> fileSectionHref = s -> Optional.of(s.replaceFirst("ifs", ""))
                                               .map(url -> url.replaceAll("\\:/", "/"))
                                               .map("http://localhost"::concat)
                                               .orElse("noHref");
        DFGOAIMetsXMLHandler consumer = new DFGOAIMetsXMLHandler("testOAICreator", ZServer, derivateSupplier,
                                                                 fileSectionHref);
        Optional<Consumer<XMLStreamWriter>> o = consumer.handle("dfgOai:" + volID);

        Assert.assertTrue(o.isPresent());

        o.ifPresent(c -> {
            try {
                print(c);
            } catch (Exception exc) {
                LOGGER.error("exc while printing", exc);
            }
        });

    }

    private static void print(Consumer<XMLStreamWriter> consumer) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(byteArrayOutputStream);
        consumer.accept(xmlStreamWriter);

        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));

        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        LOGGER.info(out.outputString(document));
    }
}
