package fsu.jportal.util;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xalan.extensions.ExpressionContext;
import org.apache.xpath.NodeSet;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.objects.XNodeSetForDOM;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.output.DOMOutputter;
import org.jdom2.transform.JDOMSource;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.gson.Gson;

import fsu.jportal.backend.GreetingsFS;
import fsu.jportal.backend.GreetingsManager;
import fsu.jportal.backend.ImprintFS;
import fsu.jportal.backend.ImprintManager;
import fsu.jportal.backend.JPObjectConfiguration;

public abstract class ImprintUtil {

    private static final Logger LOGGER = LogManager.getLogger(ImprintUtil.class);

    /**
     * Returns the imprint of the given object id or throws a 404 not
     * found web application exception.
     * 
     * @param objID mycore object id
     * @return id of imprint
     */
    public static String getImprintID(String objID, String fsType) {
        return getJournalConf(objID).get(fsType);
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

    public static JPObjectConfiguration getJournalConf(String objID) {
        try {
            return new JPObjectConfiguration(objID, "imprint.partner");
        } catch (Exception exc) {
            LOGGER.error("Unable ot load imprint config for {}", objID);
            return null;
        }
    }

    public static XNodeSet getLinks(ExpressionContext context, String objID) {
        String prop = getJournalConf(objID).get("link");
        Gson gson = new Gson();
        Map<String, String> map = gson.fromJson(prop, HashMap.class);
        XNodeSet result = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
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
            result = new XNodeSetForDOM((NodeList) ns, context.getXPathContext());
        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static org.jdom2.Element getDefaultGreeting(String objID, String lang) {
        JDOMSource xmlSource = null;
        try {
            xmlSource = GreetingsManager.createFS(objID).receiveDefault();
        } catch (JDOMException jdomExc) {
            throw new InternalServerErrorException("unable to parse imprint greeting of " + objID, jdomExc);
        } catch (Exception exc) {
            throw new InternalServerErrorException("while retrieving greeting " + objID, exc);
        }
        if (xmlSource != null) {
            return getContentSection(xmlSource, lang, objID, true);
        } else {
            return null;
        }
    }

    public static Element getDefaultGreetingXSL(String objID, String lang)
        throws WebApplicationException, JDOMException {
        DOMOutputter out = new DOMOutputter();
        org.jdom2.Element element = getDefaultGreeting(objID, lang);
        return out.output(element);
    }

    public static Element getImprintContent(String objID, String fsType, String lang)
        throws WebApplicationException, JDOMException {
        DOMOutputter out = new DOMOutputter();
        String imprintID = getImprintID(objID, fsType);
        if (imprintID == null) {
            imprintID = "master";
        }
        if (imprintID.equals("") && fsType.equals("greeting") && !objID.equals("index")) {
            return null;
        }
        if (fsType.equals("greeting")) {
            org.jdom2.Element element = getGreetingContent(GreetingsManager.createFS(objID), lang);
            return out.output(element);
        }
        org.jdom2.Element element = getImprintContent(imprintID, ImprintManager.createFS(fsType), lang);
        return out.output(element);
    }

    public static org.jdom2.Element getImprintContent(String imprintID, ImprintFS imprintFS, String lang)
        throws WebApplicationException {
        JDOMSource xmlSource;
        try {
            xmlSource = imprintFS.receive(imprintID);
        } catch (JDOMException jdomExc) {
            throw new InternalServerErrorException(String.format("unable to parse imprint webpage of %s", imprintID),
                jdomExc);
        } catch (Exception exc) {
            throw new InternalServerErrorException(String.format("while retrieving imprint %s", imprintID), exc);
        }
        if (xmlSource != null) {
            return getContentSection(xmlSource, lang, imprintID, true);
        } else {
            throw new InternalServerErrorException(String.format("while retrieving imprint %s", imprintID));
        }
    }

    public static org.jdom2.Element getGreetingContent(GreetingsFS greetingsFS, String lang)
        throws WebApplicationException {
        JDOMSource xmlSource;
        try {
            xmlSource = greetingsFS.receive();
        } catch (JDOMException jdomExc) {
            throw new InternalServerErrorException("unable to parse imprint greeting of " + greetingsFS.getjournalID(),
                jdomExc);
        } catch (Exception exc) {
            throw new InternalServerErrorException("while retrieving greeting " + greetingsFS.getjournalID(), exc);
        }
        if (xmlSource != null) {
            return getContentSection(xmlSource, lang, greetingsFS.getjournalID(), false);
        } else {
            return null;
        }
    }

    private static org.jdom2.Element getContentSection(JDOMSource xmlSource, String lang, String id,
        Boolean withoutTitle) {
        String notTitle = "";
        if (withoutTitle) {
            notTitle = " and not(@title)";
        }
        XPathExpression<org.jdom2.Element> xpathExpression = XPathFactory.instance()
            .compile("//*[@xml:lang=\"" + lang + "\"" + notTitle + "]", Filters.element());
        org.jdom2.Element section = xpathExpression.evaluateFirst(xmlSource.getDocument());
        if (section == null) {
            xpathExpression = XPathFactory.instance().compile("//*[@xml:lang=\"all\"" + notTitle + "]",
                Filters.element());
            section = xpathExpression.evaluateFirst(xmlSource.getDocument());
        }
        if (section == null) {
            xpathExpression = XPathFactory.instance().compile("//*[@xml:lang=\"de\"" + notTitle + "]",
                Filters.element());
            section = xpathExpression.evaluateFirst(xmlSource.getDocument());
        }
        if (section == null) {
            throw new InternalServerErrorException("unable to get section of " + id);
        }
        return section;
    }
}
