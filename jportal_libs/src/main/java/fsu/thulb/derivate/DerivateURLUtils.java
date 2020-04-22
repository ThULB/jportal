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

package fsu.thulb.derivate;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Created by chi on 07.02.17.
 *
 * @author Huu Chi Vu
 */
public class DerivateURLUtils {
    public static URL getDerivateURL(URL baseURL, String derivateId, String derivateOwnerId) throws MalformedURLException {
        return new URL(baseURL, "receive/" + derivateOwnerId + "?derivate=" + derivateId);
    }

    public static URL getNotSupportedFileURL(URL baseURL, String servletName, String filePath) throws MalformedURLException {
        return new URL(baseURL, "servlets/" + servletName + filePath);
    }

    public static URL getViewerURL(URL baseURL, String derivateId, String fileName) throws MalformedURLException {
        return new URL(baseURL, "rsc/viewer/" + derivateId + "/" + fileName);
    }

    public static URL getDFGViewerURL(URL baseURL, String derivateId, String mainDoc) throws MalformedURLException {
        String spec = "servlets/MCRDFGLinkServlet?deriv=" + derivateId;

        if (mainDoc != null && mainDoc.length() > 0) {
            String mainDocEnc = URLEncoder.encode(mainDoc, StandardCharsets.UTF_8);
            spec = spec + "&file=" + mainDocEnc;
        }

        return new URL(baseURL, spec);
    }
}
