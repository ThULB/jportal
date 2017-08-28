package fsu.jportal.backend.marc;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPJournal;
import fsu.jportal.backend.JPVolume;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.junit.Test;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.Record;
import org.mycore.common.MCRStoreTestCase;
import org.mycore.datamodel.metadata.MCRObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

public class Marc21ConverterTest extends MCRStoreTestCase {

    private MCRObject article, volume, journal;

    @Override
    protected Map<String, String> getTestProperties() {
        Map<String, String> testProperties = super.getTestProperties();
        testProperties.put("MCR.Metadata.Type.person", "true");
        testProperties.put("MCR.Metadata.Type.jparticle", "true");
        testProperties.put("MCR.Metadata.ObjectID.NumberPattern", "00000000");
        return testProperties;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        loadResource("/marc/jportal_person_00000001.xml");
        loadResource("/marc/jportal_jpinst_00000001.xml");
        this.article = loadResource("/marc/jportal_jparticle_00000001.xml");
        this.volume = loadResource("/marc/jportal_jpvolume_00000001.xml");
        this.journal = loadResource("/marc/jportal_jpjournal_00000001.xml");
    }

    public MCRObject loadResource(String path) throws IOException, JDOMException {
        SAXBuilder builder = new SAXBuilder();
        InputStream is = getClass().getResourceAsStream(path);
        MCRObject mcrObj = new MCRObject(builder.build(is));
        getStore().create(mcrObj.getId(), mcrObj.createXML(), new Date());
        return mcrObj;
    }

    @Test
    public void convert() throws Exception {
        JPArticle article = new JPArticle(this.article);
        Record marcArticle = Marc21Converter.convert(article);

        JPVolume volume = new JPVolume(this.volume);
        Record marcVolume = Marc21Converter.convert(volume);

        JPJournal journal = new JPJournal(this.journal);
        Record marcJournal = Marc21Converter.convert(journal);

        /*MarcTxtWriter txtWriter = new MarcTxtWriter(System.out);
        txtWriter.write(marcRecord);
        txtWriter.close();*/

        MarcXmlWriter xmlWriter = new MarcXmlWriter(System.out, true);
        xmlWriter.write(marcArticle);
        xmlWriter.close();
    }

}
