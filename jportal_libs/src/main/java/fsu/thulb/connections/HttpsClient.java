/*
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */

package fsu.thulb.connections;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Created by chi on 08.05.17.
 *
 * @author Huu Chi Vu
 */
public class HttpsClient {
    private static Logger LOGGER = LogManager.getLogger();

    private static RequestConfig noRedirect() {
        return RequestConfig
                .copy(RequestConfig.DEFAULT)
                .setRedirectsEnabled(false)
                .build();
    }

    public static CloseableHttpClient getHttpsClient() {
        return getHttpClientBuilder()
                .setSSLContext(SSLContexts.createSystemDefault())
                .build();
    }

    public static CloseableHttpClient getHttpClient() {
        return getHttpClientBuilder()
                .build();
    }

    private static HttpClientBuilder getHttpClientBuilder() {
        return HttpClientBuilder
                .create()
                .setConnectionTimeToLive(1, TimeUnit.MINUTES);
    }

    public static CloseableHttpResponse head(URI url) {
        Supplier<CloseableHttpClient> httpClientSupplier = getHttpClientSupplier(url);
        if (httpClientSupplier == null) {
            return null;
        }

        HttpHead httpHead = new HttpHead(url);
        try (CloseableHttpClient httpClient = httpClientSupplier.get()) {
            return httpClient.execute(httpHead);
        } catch (IOException e) {
            LOGGER.error("There is a problem or the connection was aborted for URL: {}", url, e);
        }

        return null;
    }

    private static Supplier<CloseableHttpClient> getHttpClientSupplier(URI url) {
        String scheme = url.getScheme();
        if ("https".equals(scheme)) {
            return () -> getHttpsClient();
        } else if ("http".equals(scheme)) {
            return () -> getHttpClient();
        } else {
            return null;
        }
    }

    public static CloseableHttpResponse head(String url) {
        try {
            URI uri = new URI(url);
            return head(uri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <T> T put(URI uri, String contentType, String data, ResponseHandler<T> responseHandler) {
        return request(HttpPut::new, uri, contentType, new StringEntity(data, "UTF-8"), responseHandler);
    }

    public static <T> T post(URI url, String contentType, String data, ResponseHandler<T> responseHandler) {
        return request(HttpPost::new, url, contentType, new StringEntity(data, "UTF-8"), responseHandler);
    }

    public static <R extends HttpEntityEnclosingRequestBase, T> T request(
            Supplier<R> requestSupp, URI uri,
            String contentType, HttpEntity entity, ResponseHandler<T> responseHandler) {

        Supplier<CloseableHttpClient> httpClientSupplier = getHttpClientSupplier(uri);
        if (httpClientSupplier == null) {
            return null;
        }

        CloseableHttpClient httpClient = httpClientSupplier.get();
        try {
            R request = requestSupp.get();
            request.setURI(uri);
            request.setHeader("content-type", contentType);
            request.setConfig(noRedirect());
            request.setEntity(entity);

            return httpClient.execute(request, responseHandler);
        } catch (ClientProtocolException e) {
            LOGGER.error("There is a HTTP protocol error for URL: {}", uri, e);
        } catch (IOException e) {
            LOGGER.error("There is a problem or the connection was aborted for URL: {}", uri, e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
