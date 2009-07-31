package org.mycore.dataimport.pica;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.mycore.importer.MCRImportRetriever;

public class MCRGbvSruRetriever implements MCRImportRetriever<MCRGbvSruRecordElementIterator> {
    private String url;
    private HttpURLConnection connection;
    private Document document;

    public MCRGbvSruRetriever(String url) {
        this.url = url;
    }


    public void connect() {
        try {
            URL url = new URL(this.url);
            connection = (HttpURLConnection) url.openConnection();
            InputStream iStream = connection.getInputStream();
            SAXBuilder builder = new SAXBuilder();
            document = builder.build(iStream);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch( JDOMException e) {
            e.printStackTrace();
        }
    }

    public MCRGbvSruRecordElementIterator retrieve() {
        return new MCRGbvSruRecordElementIterator(document);
    }

    public void close() {
        connection.disconnect();
    }

}