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
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.mets.validator.validators.ValidationException;

import fsu.jportal.mets.ABBYYtoALTOConverter;
import fsu.jportal.mets.LLZMetsConverter;

public class LLZFixer {

    public Document load(String file) throws JDOMException, IOException {
        SAXBuilder b = new SAXBuilder();
        return b.build(new File(file));
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
        ENMAPChecker llzChecker = new ENMAPChecker();

        for (String file : files) {
            // fix
            Document originalMets = llzFixer.load(file);
            ABBYYtoALTOConverter.convert(originalMets);
            String fixedFile = file.replace("mets_Abgleich.xml", "mets_Abgleich_fixed.xml");
            llzFixer.save(originalMets, fixedFile);
            // convert
            Path metsFile = Paths.get(fixedFile);
            Document convertedMets = llzChecker.convert(metsFile, new LLZMetsConverter());
            // validate
            ByteArrayInputStream in = ENMAPChecker.toByteStream(convertedMets);
            List<ValidationException> errors = llzChecker.validate(in);
            for (ValidationException exc : errors) {
                ENMAPChecker.out(metsFile, exc);
            }
        }
    }

}
