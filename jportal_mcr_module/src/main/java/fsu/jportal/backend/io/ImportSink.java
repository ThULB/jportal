package fsu.jportal.backend.io;

import org.jdom2.Document;

/**
 * Created by chi on 24.04.15.
 * @author Huu Chi Vu
 */
public interface ImportSink {
    void save(Document objXML);

    void saveClassification(Document classificationXML);
}
