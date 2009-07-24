package org.mycore.dataimport;

import java.util.List;

import org.jdom.Document;
import org.mycore.datamodel.metadata.MCRObject;

public interface MCRDataConverter {
    public List<MCRObject> convertData(Document xmlData);
}
