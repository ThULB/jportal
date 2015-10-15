package fsu.jportal.frontend.cli;

import java.io.InputStream;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.ifs.MCRFile;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPComponent;
import fsu.jportal.laws.common.xml.LawsXMLFunctions;

@MCRCommandGroup(name = "Law Commands")
public class LawCommands {

    private static final Namespace NS = Namespace.getNamespace("http://www.thulb.uni-jena.de/gesetzessammlung/");

    @MCRCommand(help = "Imports a laws.xml from an object and generates articles", syntax = "law import {0}")
    public static void importLaws(String id) throws Exception {
        MCRDerivate xmlDerivate = LawsXMLFunctions.getXMLDerivate(id);
        if (xmlDerivate == null) {
            return;
        }
        MCRFilesystemNode mainDoc = LawsXMLFunctions.getMainDoc(xmlDerivate);
        if (mainDoc == null) {
            return;
        }
        if (!(mainDoc instanceof MCRFile)) {
            return;
        }
        String imgDerivateId = LawsXMLFunctions.getImageDerivate(id);

        InputStream is = ((MCRFile) mainDoc).getContentAsInputStream();
        SAXBuilder b = new SAXBuilder();
        Document document = b.build(is);
        Element root = document.getRootElement();
        Element register = root.getChild("register", NS);
        Element gesetze = register.getChild("gesetze", NS);
        for (Element gesetz : gesetze.getChildren("gesetz", NS)) {
            JPArticle article = buildJPArticle(gesetz, imgDerivateId);
            article.setParent(id);
            article.importComponent();
        }
    }

    private static JPArticle buildJPArticle(Element gesetz, String imgDerivateId) throws MCRActiveLinkException {
        String inhalt = gesetz.getChildText("inhalt", NS);
        String nummer = gesetz.getChildText("nummer", NS);
        String erlass = null;
        String ausgabe = null;
        String seiteVon = null;
        String seiteBis = null;

        Element datum = gesetz.getChild("datum", NS);
        if (datum != null) {
            erlass = datum.getChildText("erlass", NS);
            ausgabe = datum.getChildText("ausgabe", NS);
        }
        Element seite = gesetz.getChild("seite", NS);
        if (seite != null) {
            seiteVon = seite.getChildText("von", NS);
            seiteBis = seite.getChildText("bis", NS);
        }

        JPArticle article = new JPArticle();
        // inhalt
        inhalt = nummer != null ? nummer + ". " + inhalt : inhalt;
        article.setTitle(inhalt);
        // size
        if (seiteVon != null) {
            seiteVon = JPComponent.FOUR_DIGIT_FORMAT.format(Integer.valueOf(seiteVon));
            seiteBis = seiteBis != null ? JPComponent.FOUR_DIGIT_FORMAT.format(Integer.valueOf(seiteBis)) : null;
            String size = seiteVon + ((seiteBis != null && !seiteBis.equals(seiteVon)) ? (" - " + seiteBis) : "");
            article.setSize(size);
        }
        // dates
        if (erlass != null) {
            article.setDate(erlass, null);
        }
        // Note
        StringBuilder sb = new StringBuilder();
        if (nummer != null) {
            sb.append("Gesetzesnummer: ").append(nummer);
        }
        if (erlass != null) {
            sb.append(sb.length() != 0 ? "; " : "").append("Erlass: ").append(erlass);
        }
        if (ausgabe != null) {
            sb.append(sb.length() != 0 ? "; " : "").append("Ausgabe: ").append(ausgabe);
        }
        if (!sb.toString().isEmpty()) {
            article.addNote(sb.toString(), true);
        }
        // derivate link
        if (nummer != null) {
            String image = LawsXMLFunctions.getImageByLaw(nummer, imgDerivateId);
            if (image != null) {
                article.setDerivateLink(MCRMetadataManager.retrieveMCRDerivate(MCRObjectID.getInstance(imgDerivateId)),
                    image);
            }
        }
        return article;
    }

}
