package org.mycore.dataimport.pica.gbv;

import java.net.MalformedURLException;
import java.net.URL;

import org.mycore.dataimport.pica.MCRPicaCatalog;
import org.mycore.dataimport.pica.MCRPicaConnector;

/**
 * The gbv catalog. The connection is made to "http://gso.gbv.de/sru/DB=2.1".
 *
 * @author Matthias Eichner
 */
public class MCRGbvCatalog extends MCRPicaCatalog {

    @Override
    protected MCRPicaConnector getConnector(String query) throws MalformedURLException {
        StringBuffer urlBuffer = new StringBuffer("http://gso.gbv.de/sru/DB=2.1?");
        urlBuffer.append("query=").append(query);
        urlBuffer.append("&recordSchema=").append(getRecordSchema().toString());
        urlBuffer.append("&maximumRecords=").append(getMaximumRecords());
        urlBuffer.append("&startRecord=").append(getStartRecord());
        URL url = new URL(urlBuffer.toString());
        return new MCRPicaConnector(url);
    }

}
