package org.mycore.dataimport.pica;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.jdom.Document;
import org.mycore.common.xml.MCRXMLHelper;
import org.mycore.dataimport.MCRDataRetriever;

public class MCRGbvSruImport implements MCRDataRetriever {
    private String url;

    public MCRGbvSruImport(String url) {
        this.url = url;
    }

    @Override
    public Document getXMLData() {
        Document doc = null;
        try {
            URL url = new URL(this.url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            doc = MCRXMLHelper.getParser().parseXML(conn.getInputStream());
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return doc;
    }

}
