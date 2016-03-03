package fsu.jportal.mets;

import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.mets.model.IMetsElement;

/**
 * Simple converter class which replaces all '-ABBYY' references with '-ALTO'.
 * 
 * @author Matthias Eichner
 */
public class ABBYYtoALTOConverter {

    /**
     * Does the convertion on the given document. Be aware that the document
     * is changed!
     * 
     * @param abbyENMAPMets an enmap mets with ABBYY file references
     */
    public static void convert(Document abbyENMAPMets) {
        Element mets = abbyENMAPMets.getRootElement();

        handleFileSec(mets);
        handleStructMap(mets);
    }

    private static void handleFileSec(Element mets) {
        // fileSec
        XPathExpression<Element> fileSecXPath = XPathFactory.instance().compile(
            "mets:fileSec/mets:fileGrp/mets:fileGrp/mets:file[ends-with(@ID, '-ABBYY')]", Filters.element(), null,
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
    }

    private static void handleStructMap(Element mets) {
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

    private static void fixFileID(Element e) {
        String fileID = e.getAttributeValue("FILEID");
        if (fileID != null) {
            if (fileID.contains("-ABBYY")) {
                e.setAttribute("FILEID", e.getAttributeValue("FILEID").replace("-ABBYY", "-ALTO"));
            }
        }
    }

}
