package fsu.jportal.frontend.cli.io;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

import fsu.jportal.backend.io.ImportSource;

public class LocalExportSource implements ImportSource {
    private List<Document> objs;

    public LocalExportSource(String id) {
        Document objXML = getObj(id);
        getObjs().add(objXML);
    }

    @Override
    public List<Document> getObjs() {
        if (objs == null) {
            objs = new ArrayList<Document>();
        }
        return objs;
    }

    @Override
    public Document getObj(String objID) {
        try {
            return MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(objID));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Document getClassification(String classID) {
        return null;
    }

    @Override
    public Document getDerivateFiles(String deriID) {
        return getObj(deriID);
    }
}
