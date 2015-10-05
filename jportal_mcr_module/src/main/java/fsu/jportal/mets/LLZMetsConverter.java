package fsu.jportal.mets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.LogicalSubDiv;

/**
 * Converts the the enmap llz format to mycore mets format. 
 *
 * @author Matthias Eichner
 */
public class LLZMetsConverter extends ENMAPConverter {

    private static Logger LOGGER = LogManager.getLogger(LLZMetsConverter.class);
    
    private String lastBibLabel;

    @Override
    protected LogicalDiv getLogicalDiv(Element enmap, Element enmapDiv) {
        LogicalDiv logicalDiv = super.getLogicalDiv(enmap, enmapDiv);
        logicalDiv.setType("volume");
        return logicalDiv;
    }

    @Override
    protected LogicalSubDiv getLogicalSubDiv(Element enmap, Element enmapDiv) {
        LogicalSubDiv logicalSubDiv = this.buildLogicalSubDiv(enmapDiv);
        String type = enmapDiv.getAttributeValue("TYPE").toLowerCase();
        String dmdID = LLZMetsUtils.getDmDId(enmapDiv);
        if (type.equals("issue") || type.equals("volumeparts")) {
            if(enmapDiv.getChildren().isEmpty()) {
                LOGGER.warn("Issue or volumepart has no content! " + logicalSubDiv.getId());
                return null;
            }
            handleLogicalDivs(enmap, enmapDiv, logicalSubDiv);
            return logicalSubDiv;
        }
        if (type.equals("rezension") && enmapDiv.getChildren().size() > 0) {
            handleRecension(enmap, enmapDiv, logicalSubDiv, dmdID);
        } else if (type.equals("tp")) {
            logicalSubDiv.setLabel("Titelblatt");
            logicalSubDiv.setType("title_page");
        } else if (type.equals("preface")) {
            logicalSubDiv.setLabel("Vorwort");
            logicalSubDiv.setType("preface");
        } else if (type.equals("toc")) {
            logicalSubDiv.setLabel("Register");
            logicalSubDiv.setType("index");
        } else {
            return null;
        }
        handleLogicalFilePointer(enmapDiv, logicalSubDiv);
        return logicalSubDiv;
    }

    protected void handleRecension(Element enmap, Element recensionDiv, LogicalSubDiv recension, String dmdID) {
        // type
        recension.setType("article");
        // label
        String bibLabel;
        if (dmdID != null) {
            bibLabel = LLZMetsUtils.getFullLabel(dmdID, enmap);
        } else {
            bibLabel = LLZMetsUtils.getBibLabel(recensionDiv);
        }
        if (bibLabel != null) {
            recension.setLabel(bibLabel);
            this.lastBibLabel = bibLabel;
        }
        if (recension.getLabel() == null) {
            if (lastBibLabel == null) {
                recension.setLabel("unknown " + recension.getType());
            } else {
                recension.setLabel(lastBibLabel);
            }
        }
    }

}
