package fsu.jportal.resolver;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;

import fsu.jportal.backend.JPObjectType;
import fsu.jportal.frontend.toc.JPTocResults;
import fsu.jportal.xml.JPXMLFunctions;
import org.apache.logging.log4j.LogManager;
import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.datamodel.metadata.MCRObjectID;

/**
 * Created by chi on 08.09.15.
 * @author Huu Chi Vu
 */
@URIResolverSchema(schema = "toc")
public class TOCResolver implements URIResolver {

    /*
     * toc:parentID:objectType:rows:start
     */
    @Override
    public Source resolve(String href, String base) {
        try {
            String[] uriParts = href.split(":");
            if (uriParts.length < 5) {
                throw new IllegalArgumentException("Invalid format of uri given to resolve: " + href);
            }
            String parentID = uriParts[1];
            String objectType = uriParts[2];
            int rows = Integer.valueOf(uriParts[3]);
            int start = getStart(href, parentID, objectType, rows, uriParts[4]);

            MCRObjectID objectId = MCRObjectID.getInstance(parentID);
            JPObjectType type = JPObjectType.valueOf(objectType);
            JPTocResults tocResult = new JPTocResults(objectId, type, start, rows);
            Element resultsElement = tocResult.toXML();
            return new JDOMSource(resultsElement);
        } catch (Exception exc) {
            LogManager.getLogger().error("Unable to resolve " + href, exc);
            return new JDOMSource(new Element("results"));
        }
    }

    private int getStart(String href, String parentID, String objectType, int rows, String startStr) {
        if (startStr.startsWith("ref=")) {
            String refID = startStr.substring(4);
            if (refID.equals("")) {
                throw new IllegalArgumentException("Invalid format of of referer: " + href);
            }
            return JPXMLFunctions.getRefererStart(parentID, objectType, refID, rows);
        } else {
            return Integer.valueOf(startStr);
        }
    }

}
