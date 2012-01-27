package fsu.thulb.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;

public class HttpTools {
    public static HttpURLConnection httpGET(String url, String mimeType) throws Exception  {
        return httpGET(new URI(url), mimeType);
    }
    
        public static HttpURLConnection httpGET(URI url, String mimeType) throws Exception  {
        try {
            URL connectionrURL = url.toURL();
            HttpURLConnection connection = (HttpURLConnection)connectionrURL.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", mimeType);
            return connection;
        } catch (MalformedURLException e) {
            log(e, url.toString());
            e.printStackTrace();
        } catch (IOException e) {
            log(e, url.toString());
            e.printStackTrace();
        }
        
        throw new Exception("Unknown Exception while retrieving MMCRObject " + url);
    }

    private static void log(Exception e, String msg) {
        System.out.println(e.getClass().getSimpleName() + ": " + msg);
    }
    
    public static HttpURLConnection httpPOST(String url, String mimeType) throws Exception {
        try {
            URL connectionURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection)connectionURL.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", mimeType);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            return connection;
        } catch (MalformedURLException e) {
            log(e, url);
            e.printStackTrace();
        } catch (ProtocolException e) {
            log(e, url);
            e.printStackTrace();
        } catch (IOException e) {
            log(e, url);
            e.printStackTrace();
        }
        throw new Exception("Unknown Exception while retrieving MMCRObject " + url);
    }
}
