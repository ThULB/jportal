package fsu.jportal.frontend.cli;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPComponent;
import fsu.jportal.backend.JPVolume;
import fsu.jportal.laws.common.xml.LawsXMLFunctions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.mycore.access.MCRAccessException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.ifs.MCRFile;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;

import java.io.InputStream;

@MCRCommandGroup(name = "Law Commands")
public class LawCommands {

    private static final Logger LOGGER = LogManager.getLogger(LawCommands.class);
    
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
        Namespace ns = root.getNamespace();
        Element register = root.getChild("register", ns);
        // volume
        String title = register.getChildText("titel", ns);
        if (title != null) {
            JPVolume jpVolume = new JPVolume(id);
            jpVolume.addSubTitle(title, "misc");
            jpVolume.store();
        }

        // articles
        Element gesetze = register.getChild("gesetze", ns);
        for (Element gesetz : gesetze.getChildren("gesetz", ns)) {
            JPArticle article = buildJPArticle(gesetz, imgDerivateId, ns);
            article.setParent(id);
            article.store();
        }
    }

    private static JPArticle buildJPArticle(Element gesetz, String imgDerivateId, Namespace ns)
            throws MCRActiveLinkException, MCRAccessException {
        String inhalt = gesetz.getChildText("inhalt", ns);
        String nummer = gesetz.getChildText("nummer", ns);
        String erlass = null;
        String ausgabe = null;
        String seiteVon = null;
        String seiteBis = null;

        Element datum = gesetz.getChild("datum", ns);
        if (datum != null) {
            erlass = datum.getChildText("erlass", ns);
            ausgabe = datum.getChildText("ausgabe", ns);
        }
        Element seite = gesetz.getChild("seite", ns);
        if (seite != null) {
            seiteVon = seite.getChildText("von", ns);
            seiteBis = seite.getChildText("bis", ns);
        }

        JPArticle article = new JPArticle();
        article.getObject().setImportMode(true);
        // inhalt
        inhalt = nummer != null ? nummer + ". " + inhalt : inhalt;
        article.setTitle(inhalt);
        // size
        if (seiteVon != null) {
            try {
                seiteVon = JPComponent.FOUR_DIGIT_FORMAT.format(Integer.valueOf(seiteVon));
                seiteBis = seiteBis != null ? JPComponent.FOUR_DIGIT_FORMAT.format(Integer.valueOf(seiteBis)) : null;
                String size = seiteVon + ((seiteBis != null && !seiteBis.equals(seiteVon)) ? (" - " + seiteBis) : "");
                article.setSize(size);
            } catch(Exception exc) {
                LOGGER.warn("Unable to set size for register entry: " + inhalt, exc);
            }
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
