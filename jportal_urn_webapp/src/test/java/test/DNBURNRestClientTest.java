package test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import fsu.thulb.urn.DNBURNRestClient;
import fsu.thulb.urn.EpicurLite;

public class DNBURNRestClientTest {
    @Test
    public void registerNew() throws MalformedURLException, URISyntaxException {
        URI serverURL = new URI("http://localhost:9090/dnb");
        URI dnbURL = UriBuilder.fromUri(serverURL).build();
        DNBTestServer dnbTestServer = DNBTestServer.start(serverURL);

        URL url = new URL("http://localhost:8080/jportal/rsc/viewer/jportal_derivate_00000002/EM121061.JPG");
        String urn = "urn:nbn:de:frontend-46c4a860-4d4a-4e5f-b8f1-2c020ff730be2-00000002";
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("test", "secret");
        EpicurLite epicurLite = EpicurLite.instance(credentials, urn, url);

        DNBURNRestClient.register(epicurLite, dnbURL);

        URL newUrl = new URL("http://localhost:8080/jportal/rsc/viewer/jportal_derivate_00000003/EM121061.JPG");
        EpicurLite newEpicurLite = EpicurLite.instance(credentials, urn, newUrl);
        DNBURNRestClient.register(newEpicurLite, dnbURL);

        sendGet(serverURL);
        dnbTestServer.stop();
    }

    private void sendGet(URI uri){

        HttpGet request = new HttpGet(uri);

        // add request headers
        request.addHeader("custom-key", "junit");
        request.addHeader(HttpHeaders.USER_AGENT, "Test");
        request.addHeader("Content-Type", MediaType.APPLICATION_JSON);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        try (CloseableHttpResponse response = httpClient.execute(request)) {

            // Get HttpResponse Status
            System.out.println(response.getStatusLine().toString());

            HttpEntity entity = response.getEntity();
            Header headers = entity.getContentType();
            System.out.println(headers);

            if (entity != null) {
                // return it as a String
                String result = EntityUtils.toString(entity);
                System.out.println(result);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
