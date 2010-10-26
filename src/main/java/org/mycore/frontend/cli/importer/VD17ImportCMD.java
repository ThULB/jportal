package org.mycore.frontend.cli.importer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.output.XMLOutputter;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.xml.MCRXMLHelper;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.frontend.cli.MCRAbstractCommands;
import org.mycore.frontend.cli.MCRCommand;
import org.xml.sax.SAXException;

public class VD17ImportCMD extends MCRAbstractCommands {
    private static Logger LOGGER;
    public VD17ImportCMD() {
        super();
        LOGGER = Logger.getLogger(getClass());
        command.add(new MCRCommand("vd17import {0}", this.getClass().getName() + ".importCal String", "import calenders"));
    }
    
    public static void importCal(String id) throws IOException, SAXException, ParserConfigurationException, TransformerException, MCRPersistenceException, MCRActiveLinkException{
        String baseURL = MCRConfiguration.instance().getString("MCR.baseurl");
        String vd17ImpURL = baseURL.replaceAll("8291", "8080") + "VD17-webapp/jersey/vd17Import";
        System.out.println("VD17URL: " +vd17ImpURL);
        
        URL url = new URL(vd17ImpURL);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setDoOutput(true);
        httpCon.setRequestMethod("PUT");
        httpCon.setRequestProperty("Content-Type", "application/xml");

        
        MCRObject mcrObject = new MCRObject();
        mcrObject.receiveFromDatastore(id);
        XMLOutputter xmlOutputter = new XMLOutputter();
//        
        xmlOutputter.output(mcrObject.createXML(), httpCon.getOutputStream());
//        OutputStreamWriter out = new OutputStreamWriter(
//            httpCon.getOutputStream(),"UTF-8");
//        out.write(xmlOutputter.outputString(mcrObject.createXML()));
//        out.flush();
//        out.close();
        
//        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        factory.setNamespaceAware(true);
//        org.w3c.dom.Document document = factory.newDocumentBuilder().parse(httpCon.getInputStream());
        Document mappedDoc = MCRXMLHelper.getParser().parseXML(httpCon.getInputStream());
        mcrObject.setFromJDOM(mappedDoc);
        mcrObject.updateInDatastore();
        
//        TransformerFactory transformerFactory = TransformerFactory.newInstance();
//        transformerFactory.newTransformer().transform(new DOMSource(document), new StreamResult(System.out));

        LOGGER.info("BaseURL: " + baseURL);
    }
}
