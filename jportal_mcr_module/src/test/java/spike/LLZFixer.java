package spike;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.mets.model.IMetsElement;
import org.mycore.mets.validator.validators.ValidationException;

import fsu.jportal.mets.LLZMetsConverter;

public class LLZFixer {

    public Document load(String file) throws JDOMException, IOException {
        SAXBuilder b = new SAXBuilder();
        return b.build(new File(file));
    }

    public void fix(Document d) {
        Element mets = d.getRootElement();

        // fix fileSec
        XPathExpression<Element> fileSecXPath = XPathFactory.instance().compile(
            "mets:fileSec/mets:fileGrp/mets:fileGrp[@ID='ALTOFiles']/mets:file", Filters.element(), null,
            IMetsElement.METS);
        List<Element> files = fileSecXPath.evaluate(mets);
        for (Element file : files) {
            // fix id
            file.setAttribute("ID", file.getAttributeValue("ID").replace("-ABBYY", "-ALTO"));
            // fix path
            Element flocat = file.getChild("FLocat", IMetsElement.METS);
            if (flocat != null) {
                String href = flocat.getAttributeValue("href", IMetsElement.XLINK);
                href = href.replace("/ocr/", "/alto/");
                flocat.setAttribute("href", href, IMetsElement.XLINK);
            } else {
                throw new RuntimeException("Flocat of id is null (and shouldn't)! " + file.getAttributeValue("ID"));
            }
        }

        // fix struct map fptr
        XPathExpression<Element> physStructMapXPath = XPathFactory.instance().compile("mets:structMap//mets:fptr",
            Filters.element(), null, IMetsElement.METS);
        List<Element> fptrs = physStructMapXPath.evaluate(mets);
        for (Element fptr : fptrs) {
            fixFileID(fptr);
            Element area = fptr.getChild("area", IMetsElement.METS);
            if (area != null) {
                fixFileID(area);
            }
        }
    }

    private void fixFileID(Element e) {
        String fileID = e.getAttributeValue("FILEID");
        if (fileID != null) {
            if (fileID.contains("-ABBYY")) {
                e.setAttribute("FILEID", e.getAttributeValue("FILEID").replace("-ABBYY", "-ALTO"));
            }
        }
    }

    public void print(Document d) throws IOException {
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        out.output(d, System.out);
    }

    public void save(Document d, String to) throws FileNotFoundException, IOException {
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        out.output(d, new FileOutputStream(new File(to)));
    }

    public static void main(String[] args) throws Exception {
        String files[] = {"/data/Dokumente/OCR/innsbruck/2016-01-13/18332/THULB_00050_1833_wfs2_mets_Abgleich.xml" };
        LLZFixer llzFixer = new LLZFixer();
        LLZChecker llzChecker = new LLZChecker();

        for (String file : files) {
            // fix
            Document originalMets = llzFixer.load(file);
            llzFixer.fix(originalMets);
            String fixedFile = file.replace("mets_Abgleich.xml", "mets_Abgleich_fixed.xml");
            llzFixer.save(originalMets, fixedFile);
            // convert
            Path metsFile = Paths.get(fixedFile);
            Document convertedMets = llzChecker.convert(metsFile, new LLZMetsConverter());
            // validate
            ByteArrayInputStream in = LLZChecker.toByteStream(convertedMets);
            List<ValidationException> errors = llzChecker.validate(in);
            for (ValidationException exc : errors) {
                LLZChecker.out(metsFile, exc);
            }
        }
    }

}
