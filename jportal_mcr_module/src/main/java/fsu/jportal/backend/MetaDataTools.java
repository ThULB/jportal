package fsu.jportal.backend;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.InternalServerErrorException;

import org.apache.logging.log4j.LogManager;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.content.transformer.MCRParameterizedTransformer;
import org.mycore.common.content.transformer.MCRXSLTransformer;
import org.mycore.common.xml.MCRLayoutService;
import org.mycore.common.xsl.MCRParameterCollector;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

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
            LogManager.getLogger().error("Unable to update object {} with {}", mcrObjId, xslTemplate);
        }
    }

    public static byte[] transformMCRWebPage(HttpServletRequest request, String xmlFile)
        throws Exception {
        InputStream is = MetaDataTools.class.getResourceAsStream(xmlFile);
        if (is == null) {
            throw new InternalServerErrorException("Unable to locate xmlFile of move object resource");
        }
        SAXBuilder saxBuilder = new SAXBuilder();
        Document webPage = saxBuilder.build(is);
        MCRJDOMContent source = new MCRJDOMContent(webPage);
        MCRParameterCollector parameter = new MCRParameterCollector(request);
        MCRContentTransformer transformer = MCRLayoutService.getContentTransformer("MyCoReWebPage", parameter);
        MCRContent result;
        if (transformer instanceof MCRParameterizedTransformer) {
            result = ((MCRParameterizedTransformer) transformer).transform(source, parameter);
        } else {
            result = transformer.transform(source);
        }
        return result.asByteArray();
    }

}
