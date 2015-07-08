package fsu.jportal.backend.io;

import fsu.jportal.backend.ImportDerivateObject;
import org.jdom2.Document;

import java.util.List;

/**
 * Created by chi on 24.04.15.
 * @author Huu Chi Vu
 */
public interface ImportSource {
    List<Document> getObjs();

    Document getObj(String objID);

    Document getClassification(String classID);

    ImportDerivateObject getDerivate(String deriID);
}
