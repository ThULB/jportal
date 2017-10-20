package fsu.jportal.mets;

import java.nio.file.Files;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.output.DOMOutputter;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRXSLTransformer;
import org.mycore.common.xsl.MCRParameterCollector;
import org.mycore.datamodel.niofs.MCRPath;

/**
 * Created by chi on 26.07.16.
 * @author Huu Chi Vu
 */
public class DFGOAIMetsXalan {

    protected final static Logger LOGGER = LogManager.getLogger();

    public static org.w3c.dom.Document getMets(String objID, String derivateID){
        try {
            MCRPath metsPath = MCRPath.getPath(derivateID, "/mets.xml");
            HashSet<MCRPath> ignoreNodes = new HashSet<>();
            if (Files.exists(metsPath)) {
                ignoreNodes.add(metsPath);
            }
            DFGOAIMetsGenerator metsGenerator = new DFGOAIMetsGenerator();
            metsGenerator.setDerivatePath(MCRPath.getPath(derivateID, "/"));
            metsGenerator.setIgnorePaths(ignoreNodes);
            Document mets = metsGenerator.generate().asDocument();
            MCRJDOMContent metsContent = new MCRJDOMContent(mets);
            MCRXSLTransformer transformer = new MCRXSLTransformer();
            transformer.setStylesheets("xsl/mets-dfg.xsl");
            MCRParameterCollector params = new MCRParameterCollector();
            params.setParameter("derivateID", derivateID);
            params.setParameter("objectID", objID);
            MCRContent result = transformer.transform(metsContent, params);

            DOMOutputter domOutputter = new DOMOutputter();
            return domOutputter.output(result.asXML());
        } catch (Exception e) {
            LOGGER.error("Unable to get met of derivate " + derivateID, e);
        }
        return null;
    }
}
