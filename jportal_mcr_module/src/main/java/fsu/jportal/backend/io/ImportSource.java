package fsu.jportal.backend.io;

import java.util.List;

import org.jdom2.Document;

/**
 * Created by chi on 24.04.15.
 * @author Huu Chi Vu
 */
public interface ImportSource {
    List<Document> getObjs();

    Document getObj(String objID);

    Document getClassification(String classID);

    Document getDerivateFiles(String path);
}
