package fsu.jportal.util;

import com.google.gson.Gson;
import fsu.jportal.pref.JournalConfig;
import org.apache.xalan.extensions.ExpressionContext;
import org.apache.xpath.NodeSet;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.objects.XNodeSetForDOM;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.HashMap;
import java.util.Map;

public abstract class ImprintUtil {

    /**
     * Returns the imprint of the given object id or throws a 404 not
     * found web application exception.
     * 
     * @param objID mycore object id
     * @return id of imprint
     */
    public static String getImprintID(String objID, String fsType) {
        return getJournalConf(objID).getKey(fsType);
    }

    /**
     * Checks if the given object id is assigned to an imprint.
     * 
     * @param objID mycore object id to check
     * @return true if an imprint is assigned, otherwise false
     */
    public static boolean has(String objID, String fsType) {
        String imprintID = getImprintID(objID, fsType);
        return imprintID != null && !imprintID.equals("");
    }

    public static JournalConfig getJournalConf(String objID) {
        return new JournalConfig(objID, "imprint.partner");
    }

    public static XNodeSet getLinks(ExpressionContext context, String objID) {
        String prop = getJournalConf(objID).getKey("link");
        Gson gson = new Gson();
        Map<String, String> map = new HashMap<>();
        map = gson.fromJson(prop, map.getClass());
        XNodeSet result = null ;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance() ;
            DocumentBuilder dBuilder;
            dBuilder = dbf.newDocumentBuilder();
            Document doc = dBuilder.newDocument();
            NodeSet ns = new NodeSet();
            if (map != null) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    Element elm = doc.createElement("link");
                    elm.setAttribute("text", entry.getKey());
                    elm.setAttribute("href", entry.getValue());
                    ns.addNode(elm);
                }
            }
            result = new XNodeSetForDOM( (NodeList)ns, context.getXPathContext() );
        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
        return result;
    }
}
