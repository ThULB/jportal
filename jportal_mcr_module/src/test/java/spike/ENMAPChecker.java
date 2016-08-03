package spike;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.config.MCRConfigurationLoaderFactory;
import org.mycore.mets.model.Mets;
import org.mycore.mets.validator.METSValidator;
import org.mycore.mets.validator.validators.ValidationException;

import fsu.jportal.mets.ConvertException;
import fsu.jportal.mets.ENMAPConverter;
import fsu.jportal.mets.LLZMetsConverter;

public class ENMAPChecker {

    public List<Path> listAbgleich(Path base, String endsWith) throws IOException {
        MetsFileVisitor visitor = new MetsFileVisitor(endsWith);
        Files.walkFileTree(base, visitor);
        return visitor.getPaths();
    }

    public Document convert(Path metsFile, ENMAPConverter converter)
        throws JDOMException, IOException, ConvertException {
        SAXBuilder b = new SAXBuilder();
        Document document = b.build(metsFile.toFile());
        Mets mets = converter.convert(document, metsFile.getParent());
        return mets.asDocument();
    }

    public List<ValidationException> validate(InputStream in) throws JDOMException, IOException {
        METSValidator validator = new METSValidator(in);
        return validator.validate();
    }

    static class MetsFileVisitor extends SimpleFileVisitor<Path> {

        String endsWith;

        public MetsFileVisitor(String endsWith) {
            this.endsWith = endsWith;
        }

        List<Path> paths = new ArrayList<>();

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (file.getFileName().toString().endsWith(endsWith)) {
                paths.add(file);
                return FileVisitResult.SKIP_SIBLINGS;
            }
            return FileVisitResult.CONTINUE;
        }

        public List<Path> getPaths() {
            return paths;
        }
    }

    public static ByteArrayInputStream toByteStream(Document doc) throws IOException {
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
        ENMAPChecker enmapChecker = new ENMAPChecker();
        // check
        //        List<Path> listAbgleich = llzChecker.listAbgleich(Paths.get("/data/Dokumente/OCR/innsbruck/2015-09-29/"), "_mets_Abgleich.xml");
        //        for (Path p : listAbgleich) {
        //            try {
        //                Document doc = llzChecker.convert(p, new LLZMetsConverter());
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

        System.setProperty("MCR.Home", "/data/mcrHome");
        System.setProperty("MCR.AppName", "jportal");
        Map<String, String> properties = MCRConfigurationLoaderFactory.getConfigurationLoader().load();
        MCRConfiguration.instance().initialize(properties, true);

        // convert
        Path metsFile = Paths.get("/data/temp/test/test2.xml");
        LLZMetsConverter converter = new LLZMetsConverter();
        Document doc = enmapChecker.convert(metsFile, converter);
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        out.output(doc, new FileOutputStream(new File("/data/temp/mets.xml")));

        ByteArrayInputStream in = toByteStream(doc);
        List<ValidationException> excs = enmapChecker.validate(in);
        for (ValidationException e : excs) {
            System.out.println(e.toJSON());
        }
    }

}
