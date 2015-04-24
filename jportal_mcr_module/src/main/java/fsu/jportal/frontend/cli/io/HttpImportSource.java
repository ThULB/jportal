package fsu.jportal.frontend.cli.io;

import fsu.jportal.backend.io.ImportSource;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chi on 24.04.15.
 * @author Huu Chi Vu
 */
public class HttpImportSource implements ImportSource {
    private final String USER_AGENT = "Mozilla/5.0";

    private List<Document> objs;

    private URL url;

    public HttpImportSource(String objURL) {
        url = null;
        try {
            System.out.println("Get Obj: " + objURL);
            url = new URL(objURL);
            InputStream in = url.openStream();
            Document objXML = buildXML(in);
            getObjs().add(objXML);
        } catch (java.io.IOException | JDOMException e) {
            e.printStackTrace();
        }
    }

    private Document buildXML(InputStream in) throws JDOMException, IOException {
        SAXBuilder saxBuilder = new SAXBuilder();
        return saxBuilder.build(in);
    }

    public List<Document> getObjs() {
        if (objs == null) {
            objs = new ArrayList<Document>();
        }
        return objs;
    }

    public URL getUrl() {
        return url;
    }

    public Document getObj(String objID) {
        try {
            URL url = getUrl().toURI().resolve("/receive/" + objID + "?XSL.Style=xml").toURL();
            System.out.println("Get Obj: " + url);
            InputStream inputStream = url.openStream();
            return buildXML(inputStream);
        } catch (IOException | JDOMException | URISyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Document getClassification(String classID) {
        try {
            URL url = getUrl().toURI().resolve("/rsc/classifications/export/" + classID).toURL();
            System.out.println("Get Classi: " + url);

            InputStream inputStream = url.openStream();
            return buildXML(inputStream);
        } catch (IOException | JDOMException | URISyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }

//    public String getClassification(String classID) {
//        try {
//            URL url1 = getUrl().toURI().resolve("/rsc/classifications/" + classID).toURL();
//
//            HttpURLConnection con = (HttpURLConnection) url1.openConnection();
//
//            // optional default is GET
//            con.setRequestMethod("GET");
//
//            //add request header
//            con.setRequestProperty("User-Agent", USER_AGENT);
//
//            int responseCode = con.getResponseCode();
//            System.out.println("\nSending 'GET' request to URL : " + url);
//            System.out.println("Response Code : " + responseCode);
//
//            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
//            String inputLine;
//            StringBuffer response = new StringBuffer();
//
//            while ((inputLine = in.readLine()) != null) {
//                response.append(inputLine);
//            }
//            in.close();
//
//            return response.toString();
//        } catch (URISyntaxException | IOException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }
}
