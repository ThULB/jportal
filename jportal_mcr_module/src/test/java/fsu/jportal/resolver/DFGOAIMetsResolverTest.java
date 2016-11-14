package fsu.jportal.resolver;

import fsu.jportal.mocks.DerivateXMLToParsedData;
import fsu.jportal.mocks.FakeInputSourceFromZS;
import fsu.jportal.mocks.TransformerList;
import fsu.jportal.xml.stream.DerivateFileInfo;
import fsu.jportal.xml.stream.XMLStreamReaderUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by chi on 14.11.16.
 * @author Huu Chi Vu
 */
public class DFGOAIMetsResolverTest {
    /*String volID = "jportal_jpvolume_00220746";

    Function<String, Optional<XMLStreamReader>> ZServer;

    TransformerList<XMLStreamReader, DerivateFileInfo> xmlDerivateToParsedData;

    Function<String, Stream<DerivateFileInfo>> derivateSupplier;

    @Before
    public void setUp() throws Exception {
        ZServer = id -> FakeInputSourceFromZS.getReaderFor(id);

        xmlDerivateToParsedData = new TransformerList<>();
        xmlDerivateToParsedData.add(new DerivateXMLToParsedData());

        derivateSupplier = id -> FakeInputSourceFromZS
                .getReaderFor(id)
                .map(XMLStreamReaderUtils::toStream)
                .orElseGet(Stream::empty)
                .flatMap(xmlDerivateToParsedData::transformToStream);
    }

    @Test
    public void testResolver() throws Exception {
        DFGOAIMetsResolver dfgOaiResolver = new DFGOAIMetsResolver("testOAICreator", ZServer, derivateSupplier,
                                                                   id -> "TestPublishedIsoDate");

        Source testOAICreator = dfgOaiResolver.resolve("dfgOai:"+volID, null);

        Assert.assertNotNull(testOAICreator);

    }*/
}
