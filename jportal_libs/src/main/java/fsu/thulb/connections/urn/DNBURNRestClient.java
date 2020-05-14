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

package fsu.thulb.connections.urn;

import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fsu.thulb.connections.HttpsClient;
import fsu.thulb.model.EpicurLite;
import static org.apache.http.entity.ContentType.APPLICATION_XML;

/**
 * Created by chi on 25.01.17.
 *
 * @author shermann
 * @author Huu Chi Vu
 */
public class DNBURNRestClient {
    private static final Logger LOGGER = LogManager.getLogger();


    /*private static String getBaseServiceURL(String urn) {
        return "https://restapi.nbn-resolving.org/urns/" + urn;
    }*/


    /**
     * Please see list of status codes and their meaning:
     * <br><br>
     * 204 No Content: URN is in database. No further information asked.<br>
     * 301 Moved Permanently: The given URN is replaced with a newer version.
     * This newer version should be used instead.<br>
     * 404 Not Found: The given URN is not registered in system.<br>
     * 410 Gone: The given URN is registered in system but marked inactive.<br>
     *
     * @return the status code of the request
     */
    public static Optional<Date> register(EpicurLite epicurLite, URI baseUrl) {
        String urn = epicurLite.getURN();

        UriBuilder uriBuilder = UriBuilder.fromUri(baseUrl);
        UriBuilder baseUriBuilder = uriBuilder.path("urns").path(urn);
        URI baseServiceURI = baseUriBuilder.build();
        URI updateURI = baseUriBuilder.path("links").build();
        CloseableHttpResponse response = HttpsClient.head(baseServiceURI);

        if(response == null){
            LOGGER.warn("Could not get a response for URL ", baseServiceURI.toString());
            return Optional.empty();
        }

        StatusLine statusLine = response.getStatusLine();

        if (statusLine == null) {
            LOGGER.warn("HEAD request for {} returns no status line.", baseServiceURI.toString());
            return Optional.empty();
        }

        int headStatus = statusLine.getStatusCode();

        switch (headStatus) {
            case HttpStatus.SC_NO_CONTENT:
                LOGGER.info("URN {} is in database. No further information asked.", urn);
                LOGGER.info("Performing update of url.");
                return update(epicurLite, updateURI);
            case HttpStatus.SC_NOT_FOUND:
                LOGGER.info("The given URN {} is not registered in system.", urn);
                return registerNew(epicurLite, baseServiceURI);
            case HttpStatus.SC_MOVED_PERMANENTLY:
                LOGGER.warn("The given URN {} is replaced with a newer version. \n "
                    + "This newer version should be used instead.", urn);
                break;
            case HttpStatus.SC_GONE:
                LOGGER.warn("The given URN {} is registered in system but marked inactive.", urn);
                break;
            default:
                LOGGER.warn("Could not handle request for urnInfo {} Status code {}.", urn, headStatus);
                break;
        }

        return Optional.empty();
    }

    /**
     * Registers a new URN.
     * <br><br>
     * 201 Created: URN-Record is successfully created.<br>
     * 303 See other: At least one of the given URLs is already registered under another URN,
     * which means you should use this existing URN instead of assigning a new one<br>
     * 409 Conflict: URN-Record already exists and can not be created again.<br>
     *
     * @return the status code of the request
     */
    private static Optional<Date> registerNew(EpicurLite epicurLite, URI baseServiceURI) {
        String urn = epicurLite.getURN();
        String url = epicurLite.getURL();

        String epicurLiteStr;
        try {
            epicurLiteStr = epicurLite.asString();
        } catch (JAXBException e) {
            LOGGER.error("Unexpected error while creating EpicurLite string for URN " + urn + ".", e);
            return Optional.empty();
        }

        return HttpsClient.put(baseServiceURI, APPLICATION_XML.toString(), epicurLiteStr,
                r -> registerNewHandler(r, baseServiceURI, urn, url, epicurLiteStr));
    }

    private static Optional<Date> registerNewHandler(HttpResponse response, URI baseServiceURI, String urn,
                                                     String url, String epicurLiteStr) {
        StatusLine statusLine = response.getStatusLine();

        if (statusLine == null) {
            LOGGER.warn("PUT request for {} returns no status line.", baseServiceURI);
            return Optional.empty();
        }

        int putStatus = statusLine.getStatusCode();

        switch (putStatus) {
            case HttpStatus.SC_CREATED:
                LOGGER.info("URN {} registered to {}", urn, url);
                return Optional.ofNullable(response.getFirstHeader("Last-Modified"))
                    .map(Header::getValue)
                    .map(DateTimeFormatter.RFC_1123_DATE_TIME::parse)
                    .map(Instant::from)
                    .map(Date::from);
            case HttpStatus.SC_SEE_OTHER:
                LOGGER.warn("At least one of the given URLs is already registered under another URN, "
                    + "which means you should use this existing URN instead of assigning a new one.");
                LOGGER.warn("URN {} could NOT registered to {}.", urn, url);
                break;
            case HttpStatus.SC_CONFLICT:
                LOGGER.warn("URN-Record already exists and can not be created again.");
                LOGGER.warn("URN {} could NOT registered to {}.", urn, url);
                break;
            default:
                LOGGER.warn("Could not handle urnInfo request: status={}, urn={}, url={}.", putStatus, urn, url);
                LOGGER.warn("Epicur Lite:");
                LOGGER.warn(epicurLiteStr);
                break;
        }

        return Optional.empty();
    }

    /**
     * Updates all URLS to a given URN.
     * <br><br>
     * 204 No Content: URN was updated successfully<br>
     * 301 Moved Permanently: URN has a newer version<br>
     * 303 See other: URL is registered for another URN<br>
     *
     * @return the status code of the request
     */

    private static Optional<Date> update(EpicurLite epicurLite, URI updateURI) {
        String urn = epicurLite.getURN();
        String url = epicurLite.getURL();

        String epicurLiteStr;
        try {
            epicurLiteStr = epicurLite.asString();
        } catch (JAXBException e) {
            LOGGER.error("Unexpected error while creating EpicurLite string for URN " + urn + ".", e);
            return Optional.empty();
        }

        return HttpsClient.post(updateURI, APPLICATION_XML.toString(), epicurLiteStr,
                r -> updateHandler(r, updateURI, urn, url));
    }

    private static Optional<Date> updateHandler(HttpResponse response, URI updateURI, String urn, String url) {
        StatusLine statusLine = response.getStatusLine();

        if (statusLine == null) {
            LOGGER.warn("POST request for {} returns no status line.", updateURI);
            return Optional.empty();
        }

        int postStatus = statusLine.getStatusCode();

        switch (postStatus) {
            case HttpStatus.SC_NO_CONTENT:
                LOGGER.info("URN {} updated to {}.", urn, url);
                return Optional.ofNullable(response.getFirstHeader("Last-Modified"))
                    .map(Header::getValue)
                    .map(DateTimeFormatter.RFC_1123_DATE_TIME::parse)
                    .map(Instant::from)
                    .map(Date::from);
            case HttpStatus.SC_MOVED_PERMANENTLY:
                LOGGER.warn("URN {} has a newer version.", urn);
                break;
            case HttpStatus.SC_SEE_OTHER:
                LOGGER.warn("URL {} is registered for another URN.", url);
                break;
            default:
                LOGGER.warn("URN {} could not be updated. Status {}.", urn, postStatus);
                break;
        }

        return Optional.empty();
    }
}
