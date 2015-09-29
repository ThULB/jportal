package spike;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.SAXOutputter;
import org.jdom2.output.XMLOutputter;
import org.mycore.mets.model.Mets;
import org.mycore.mets.validator.METSValidator;
import org.mycore.mets.validator.validators.AltoValidator;
import org.mycore.mets.validator.validators.ValidationException;

import fsu.jportal.mets.ConvertException;
import fsu.jportal.mets.LLZMetsConverter;

public class LLZChecker {

    public List<Path> listAbgleich(Path base) throws IOException {
        MetsFileVisitor visitor = new MetsFileVisitor();
        Files.walkFileTree(base, visitor);
        return visitor.getPaths();
    }

    public Document convert(Path metsFile) throws JDOMException, IOException, ConvertException {
        SAXBuilder b = new SAXBuilder();
        Document document = b.build(metsFile.toFile());
        LLZMetsConverter c = new LLZMetsConverter();
        Mets mets = c.convert(document);
        return mets.asDocument();
    }

    public List<ValidationException> validate(InputStream in) throws JDOMException, IOException {
        METSValidator validator = new METSValidator(in);
        validator.getValidators().add(new AltoValidator());
        return validator.validate();
    }

    static class MetsFileVisitor extends SimpleFileVisitor<Path> {
        List<Path> paths = new ArrayList<>();

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (file.getFileName().toString().endsWith("mets_Abgleich.xml")) {
                paths.add(file);
                return FileVisitResult.SKIP_SIBLINGS;
            }
            return FileVisitResult.CONTINUE;
        }

        public List<Path> getPaths() {
            return paths;
        }
    }

    private static ByteArrayInputStream toByteStream(Document doc) throws IOException {
        byte[] bytes = getByteArray(doc, Format.getPrettyFormat());
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        return in;
    }
    
    public static byte[] getByteArray(org.jdom2.Document jdom, Format format) throws IOException {
        ByteArrayOutputStream outb = new ByteArrayOutputStream();
        XMLOutputter outp = new XMLOutputter(format.setEncoding("UTF-8"));
        outp.output(jdom, outb);
        return outb.toByteArray();
    }

    public static void out(Path p, Exception exc) {
        System.out.println(p.toAbsolutePath().toString() + ": " + exc.getMessage());
    }

    public static void main(String[] args) throws Exception {
        LLZChecker llzChecker = new LLZChecker();
        // check
//        List<Path> listAbgleich = llzChecker.listAbgleich(Paths.get("/data/Dokumente/OCR/innsbruck/2015-09-29/"));
//        for (Path p : listAbgleich) {
//            try {
//                Document doc = llzChecker.convert(p);
//                ByteArrayInputStream in = toByteStream(doc);
//                List<ValidationException> excs = llzChecker.validate(in);
//                for (Exception e : excs) {
//                    out(p, e);
//                }
//            } catch (Exception exc) {
//                out(p, exc);
//                exc.printStackTrace();
//            }
//        }

        // convert
        Document doc = llzChecker.convert(Paths.get("/data/Dokumente/OCR/innsbruck/2015-09-29/THULB_00011/1804/THULB_00011_1804_wfs2_mets_Abgleich.xml"));
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        out.output(doc, System.out);
        
        ByteArrayInputStream in = toByteStream(doc);
        List<ValidationException> excs = llzChecker.validate(in);
        for (Exception e : excs) {
            e.printStackTrace();
        }
    }

}
