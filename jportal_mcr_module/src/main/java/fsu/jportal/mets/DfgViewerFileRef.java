package fsu.jportal.mets;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.mycore.common.MCRException;
import org.mycore.common.xml.MCRXMLFunctions;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.mets.model.MCRMETSHierarchyGenerator;
import org.mycore.mets.model.files.FileGrp;
import org.mycore.mets.model.struct.PhysicalSubDiv;
import org.mycore.mets.tools.MCRMetsSave;

public class DfgViewerFileRef implements MCRMETSHierarchyGenerator.FileRef {

    public static Map<String, String> GROUP_TO_ZOOM_LEVEL_MAP;

    static {
        GROUP_TO_ZOOM_LEVEL_MAP = new HashMap<>();
        GROUP_TO_ZOOM_LEVEL_MAP.put(FileGrp.USE_MIN, "MIN");
        GROUP_TO_ZOOM_LEVEL_MAP.put(FileGrp.USE_DEFAULT, "MID");
    }

    private MCRPath path;

    private String contentType;

    public DfgViewerFileRef(MCRPath path, String contentType) {
        this.path = path;
        this.contentType = contentType;
    }

    @Override
    public String toFileId(FileGrp fileGrp) {
        return fileGrp.getUse() + "_" + MCRMetsSave.getFileBase(path);
    }

    @Override
    public String toFileHref(FileGrp fileGrp) {
        String path = getPath().getOwnerRelativePath().substring(1);
        try {
            String imagePath = MCRXMLFunctions.encodeURIPath(path, true);
            String zoomLevel = GROUP_TO_ZOOM_LEVEL_MAP.get(fileGrp.getUse());
            return MCRFrontendUtil.getBaseURL() + "servlets/MCRTileCombineServlet/"
                + zoomLevel + "/" + getPath().getOwner() + "/" + imagePath;
        } catch (URISyntaxException exc) {
            throw new MCRException("Unable to encode " + path, exc);
        }
    }

    public String toPhysId() {
        return PhysicalSubDiv.ID_PREFIX + MCRMetsSave.getFileBase(path);
    }

    public MCRPath getPath() {
        return path;
    }

    public String getContentType() {
        return contentType;
    }

}
