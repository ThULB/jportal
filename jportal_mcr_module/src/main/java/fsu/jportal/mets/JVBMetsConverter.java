package fsu.jportal.mets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.struct.LogicalDiv;

import java.nio.file.Path;
import java.util.List;

public class JVBMetsConverter extends ENMAPConverter {

    private static Logger LOGGER = LogManager.getLogger(JVBMetsConverter.class);

    private LogicalDiv lastSerialNovel;

    @Override
    public Mets convert(Document enmap, Path basePath) throws ConvertException {
        lastSerialNovel = null;
        return super.convert(enmap, basePath);
    }

    @Override
    protected LogicalDiv getLogicalSubDiv(Element enmap, Element enmapDiv, Mets mcrMets, List<ALTO> altoReferences) {
        LogicalDiv logicalSubDiv = this.buildLogicalSubDiv(enmapDiv);
        if(enmapDiv.getChildren().isEmpty()) {
            throw new ConvertException("Logical mets:div[@ID='" + logicalSubDiv.getId() + "'] has no children. This can"
                    + " happen if there is an empty issue or article in Structify.");
        }
        String type = logicalSubDiv.getType();
        if (type.equals("issue")) {
            if (enmapDiv.getChildren().isEmpty()) {
                LOGGER.warn("Issue has no content! " + logicalSubDiv.getId());
                return null;
            }
            handleLogicalDivs(enmap, enmapDiv, logicalSubDiv, mcrMets, altoReferences);
            return logicalSubDiv;
        } else if (type.equals("article") || type.equals("serialnovel")) {
            if (type.equals("serialnovel")) {
                logicalSubDiv.setType("article");
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
        handleLogicalFilePointer(getAreas(enmapDiv), logicalSubDiv, mcrMets, altoReferences);
        return logicalSubDiv;
    }

}
