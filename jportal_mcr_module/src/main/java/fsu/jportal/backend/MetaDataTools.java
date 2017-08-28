package fsu.jportal.backend;

import org.mycore.common.content.MCRContent;
import org.mycore.common.content.transformer.MCRXSLTransformer;
import org.mycore.common.xsl.MCRParameterCollector;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

import java.io.IOException;
import java.util.Date;

public class MetaDataTools {
    /**
     * Retrieve the MCR object, xsl transform with given style sheet and update object
     * @param mcrObjId
     * @param xslTemplate
     * @param parameter xsl parameter
     */
    public static void updateWithXslt(String mcrObjId, String xslTemplate, MCRParameterCollector parameter) {
        updateWithXslt(MCRObjectID.getInstance(mcrObjId), xslTemplate, parameter);
    }
    
    /**
     * Retrieve the MCR object, xsl transform with given style sheet and update object
     * @param mcrObjId
     * @param xslTemplate
     * @param parameter xsl parameter
     */
    public static void updateWithXslt(MCRObjectID mcrObjId, String xslTemplate, MCRParameterCollector parameter) {
        MCRXSLTransformer transformer = new MCRXSLTransformer(xslTemplate);
        MCRXMLMetadataManager mcrxmlMetadataManager = MCRXMLMetadataManager.instance();
        
        try {
            MCRContent source = mcrxmlMetadataManager.retrieveContent(mcrObjId);
            MCRContent fixedObj = transformer.transform(source, parameter);
            mcrxmlMetadataManager.update(mcrObjId, fixedObj, new Date());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
