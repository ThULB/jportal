package fsu.jportal.mets;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.mets.model.IMetsElement;

import java.util.List;

/**
 * Simple converter class which replaces all '-ABBYY' references with '-ALTO'.
 * 
 * @author Matthias Eichner
 */
public class ABBYYtoALTOConverter {

    /**
     * Does the conversion on the given document. Be aware that the document
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
        // fileGrp
        Element fileSec = mets.getChild("fileSec", IMetsElement.METS);
        XPathExpression<Element> abbyyFileGrpExp = XPathFactory.instance().compile(
            "mets:fileGrp/mets:fileGrp[@ID='ABBYYFiles']", Filters.element(), null,
            IMetsElement.METS);
        Element abbyyFileGrp = abbyyFileGrpExp.evaluateFirst(fileSec);
        if(abbyyFileGrp != null) {
            abbyyFileGrp.setAttribute("ID", "ALTOFiles");
        }
        // alto files
        XPathExpression<Element> altoFileSecXPath = XPathFactory.instance().compile(
            "mets:fileGrp/mets:fileGrp/mets:file[ends-with(@ID, '-ABBYY')]", Filters.element(), null,
            IMetsElement.METS);
        List<Element> altoFiles = altoFileSecXPath.evaluate(fileSec);
        for (Element file : altoFiles) {
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
        // img files
        XPathExpression<Element> imgFileSecXPath = XPathFactory.instance().compile(
            "mets:fileGrp/mets:fileGrp/mets:file/mets:FLocat[contains(@xlink:href, 'OCRmaster/')]", Filters.element(), null,
            IMetsElement.METS, IMetsElement.XLINK);
        List<Element> imgFiles = imgFileSecXPath.evaluate(fileSec);
        for (Element flocat : imgFiles) {
            String href = flocat.getAttributeValue("href", IMetsElement.XLINK);
            href = href.replace("OCRmaster/", "");
            flocat.setAttribute("href", href, IMetsElement.XLINK);
        }
    }

    private static void handleStructMap(Element mets) {
        XPathExpression<Element> structMapXPath = XPathFactory.instance().compile("mets:structMap//mets:fptr",
            Filters.element(), null, IMetsElement.METS);
        List<Element> fptrs = structMapXPath.evaluate(mets);
        for (Element fptr : fptrs) {
            fixFileID(fptr);
            List<Element> areas = fptr.getChildren("area", IMetsElement.METS);
            areas.forEach(area -> {
                fixFileID(area);
            });
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
