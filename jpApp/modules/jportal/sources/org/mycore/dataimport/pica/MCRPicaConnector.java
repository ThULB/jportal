package org.mycore.dataimport.pica;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;
import org.mycore.importer.MCRImportConnector;

/**
 * This class uses a simple <code>HttpURLConnection</code> to connect to
 * the data source.
 *
 * @author Matthias Eichner
 */
public class MCRPicaConnector implements MCRImportConnector<HttpURLConnection> {

    private static final Logger LOGGER = Logger.getLogger(MCRPicaConnector.class);
    
    private URL url;

    private HttpURLConnection httpConnection;

    public MCRPicaConnector(URL url) {
        this.url = url;
    }

    @Override
    public HttpURLConnection connect() {
        try {
            httpConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException ioExc) {
            LOGGER.error(ioExc);
        }
        return httpConnection;
    }

    @Override
    public void close() {
        httpConnection.disconnect();
    }

}