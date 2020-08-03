package spike;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.ElementFilter;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.junit.Test;

import fsu.archiv.mycore.sru.impex.pica.model.Datafield;
import fsu.archiv.mycore.sru.impex.pica.model.PicaRecord;
import fsu.archiv.mycore.sru.impex.pica.model.Subfield;
import static org.junit.Assert.assertFalse;

public class SRU {
    @Test
    public void test() {
        InputStream resourceAsStream = getClass().getResourceAsStream("/sru.xml");
        try {
            Document document = new SAXBuilder().build(resourceAsStream);
            
            List<PicaRecord> picaRecords = getPicaRecords(document);
            assertFalse(picaRecords.isEmpty());
        } catch (JDOMException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public List<PicaRecord> getPicaRecords(Document source) {
        List<PicaRecord> records = new ArrayList<>();
        if(source == null){
            return records;
        }
        
        Document doc = (Document) source;
        ArrayList<Namespace> namespaces = new ArrayList<Namespace>();
        namespaces.add(Namespace.getNamespace("zs", "http://www.loc.gov/zing/srw/"));
        namespaces.add(Namespace.getNamespace("pica", "info:srw/schema/5/picaXML-v1.0"));
        XPathExpression<Element> xp = XPathFactory.instance().compile("//pica:record", Filters.element(), null, namespaces);
        List<Element> recordElements = xp.evaluate(doc);
        for (Element recordElement : recordElements) {
            records.add(convertToPica(recordElement));
        }
        return records;
    }
    
    private PicaRecord convertToPica(Element record) {
        PicaRecord pr = new PicaRecord();
        Iterator<Element> it = record.getDescendants(new ElementFilter("datafield"));

        while (it.hasNext()) {
            Element dfElem = it.next();
            String tag = dfElem.getAttributeValue("tag");
            String occ = dfElem.getAttributeValue("occurrence");
            Datafield df = new Datafield(tag, occ);
            pr.addDatafield(df);

            Iterator<Element> subfieldIterator = dfElem.getDescendants(new ElementFilter("subfield"));
            while (subfieldIterator.hasNext()) {
                Element sfElem = subfieldIterator.next();
                String code = sfElem.getAttributeValue("code");
                String value = sfElem.getText();
                Subfield sf = new Subfield(code, value);
                df.addSubField(sf);
            }
        }

        return pr;
    }

}
