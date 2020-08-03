package fsu.jportal.backend.event;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamSource;

import org.jdom2.Document;
import org.jdom2.transform.JDOMResult;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

import fsu.jportal.xml.XMLTools;

public class CreateJournaldHandler extends MCREventHandlerBase {
    @Override
    protected void handleObjectCreated(MCREvent evt, MCRObject obj) {
        MCRObjectID mcrId = obj.getId();
        if ("jpjournal".equals(mcrId.getTypeId())) {
            String idStr = mcrId.toString();
            Document objXML = obj.createXML();
            XMLTools xmlTools = new XMLTools();
            Source xmlSource = new JDOMSource(objXML);
            InputStream xslStream = getClass().getResourceAsStream("/editor/xsl/createJournal.xsl");
            Source xslSource = new StreamSource(xslStream);

            JDOMResult resultXML = new JDOMResult();

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("journalID", idStr);
            
            try {
                xmlTools.transform(xmlSource, xslSource, params, resultXML);
                MCRObject objWithHiddenJournalID = new MCRObject(resultXML.getDocument());
                evt.put("object", objWithHiddenJournalID);
            } catch (TransformerFactoryConfigurationError | TransformerException e) {
                e.printStackTrace();
            }
        }
    }
}
