package fsu.jportal.mets;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.files.FileGrp;
import org.mycore.mets.model.struct.LogicalDiv;

import fsu.jportal.mets.LLZMetsUtils.AltoHrefStrategy;
import fsu.jportal.mets.LLZMetsUtils.TiffHrefStrategy;

public class JVBMetsConverter extends ENMAPConverter {

    private static Logger LOGGER = LogManager.getLogger(JVBMetsConverter.class);

    private LogicalDiv lastSerialNovel;

    @Override
    public Mets convert(Document enmap, Path basePath) throws ConvertException {
        lastSerialNovel = null;
        return super.convert(enmap, basePath);
    }

    protected void handleFileSection(Element enmap, Mets mcr) throws URISyntaxException {
        FileGrp masterGroup = handleFileGroup(enmap, "MASTER",
            "mets:fileSec//mets:fileGrp[@ID='OCRMasterFiles']/mets:file", "image/tiff", new TiffHrefStrategy());
        FileGrp altoGroup = handleFileGroup(enmap, "ALTO", "mets:fileSec//mets:fileGrp[@ID='ABBYYFiles']/mets:file",
            "text/xml", new AltoHrefStrategy());
        mcr.getFileSec().addFileGrp(masterGroup);
        mcr.getFileSec().addFileGrp(altoGroup);
    }

    @Override
    protected LogicalDiv getLogicalSubDiv(Element enmap, Element enmapDiv, Mets mcrMets, List<ALTO> altoReferences,
        List<String> emptyBlocks) {
        LogicalDiv logicalSubDiv = this.buildLogicalSubDiv(enmapDiv);
        String type = enmapDiv.getAttributeValue("TYPE").toLowerCase();
        if (type.equals("issue")) {
            if (enmapDiv.getChildren().isEmpty()) {
                LOGGER.warn("Issue has no content! " + logicalSubDiv.getId());
                return null;
            }
            handleLogicalDivs(enmap, enmapDiv, logicalSubDiv, mcrMets, altoReferences, emptyBlocks);
            return logicalSubDiv;
        }
        if (type.equals("article") || type.equals("serialnovel")) {
            logicalSubDiv.setType("article");
            if (type.equals("serialnovel")) {
                lastSerialNovel = logicalSubDiv;
            }
        } else if (type.equals("serialnovelcontinue")) {
            if (lastSerialNovel == null) {
                throw new ConvertException(
                    "There is no serial novel defined before SerialNovelContinue appears " + logicalSubDiv.getId());
            }
        } else {
            return null;
        }
        handleLogicalFilePointer(enmapDiv, logicalSubDiv, mcrMets, altoReferences, emptyBlocks);
        return logicalSubDiv;
    }

}
