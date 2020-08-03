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

package fsu.jportal.backend.pi.urn;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.datamodel.ifs.MCRFileNodeServlet;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaIFS;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectDerivate;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRContentTypes;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.frontend.MCRFrontendUtil;

import fsu.jportal.backend.mcr.JPConfig;

/**
 * Created by chi on 07.02.17.
 *
 * @author Huu Chi Vu
 */
public class DerivateURNUtils {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String SUPPORTED_CONTENT_TYPE ;

    static {
        SUPPORTED_CONTENT_TYPE = JPConfig.getString("MCR.URN.URNGranular.SupportedContentTypes", "");
    }

    public static URL getURL(String derivateID, String additional, String urn) {
        try {
            // the base urn, links to frontpage (metadata + viewer)

            if (additional == null || additional.trim().length() == 0) {
                MCRDerivate derivate = MCRMetadataManager.retrieveMCRDerivate(MCRObjectID.getInstance(derivateID));
                return new URL(
                    MCRFrontendUtil.getBaseURL() + "receive/" + derivate.getOwnerID() + "?derivate=" + derivateID);
            } else /* an urn for a certain file, links to iview2 */ {
                MCRPath file = MCRPath.getPath(derivateID, additional);

                if (!Files.exists(file)) {
                    LOGGER.warn("File {} in object {} could NOT be found", file.getFileName().toString(), derivateID);
                    return null;
                }

                if (!isFileSupported(file)) {
                    LOGGER.info("File is not displayable within iView2. Use {} as url",
                        MCRFileNodeServlet.class.getSimpleName());
                    String filePath = "/" + file.getOwner() + "/" + file.getFileName();
                    return new URL(
                        MCRFrontendUtil.getBaseURL() + "servlets/" + MCRFileNodeServlet.class.getSimpleName()
                            + filePath);
                }

                return new URL(getViewerURL(file));
            }
        } catch (MalformedURLException e) {
            LOGGER.error("Malformed URL for URN {}", urn, e);
        }

        return null;
    }

    /**
     * @param file
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    private static String getViewerURL(MCRPath file) {
        return new MessageFormat("{0}rsc/viewer/{1}/{2}", Locale.ROOT).format(
            new Object[] { MCRFrontendUtil.getBaseURL(), file.getOwner(), file.getFileName().toString() });
    }

    public static URL getDFGViewerURL(String derivateIdStr, String urn) {
        try {
            MCRObjectID derivateId = MCRObjectID.getInstance(derivateIdStr);
            MCRDerivate derivate = MCRMetadataManager.retrieveMCRDerivate(derivateId);
            String mainDoc = Optional.ofNullable(derivate.getDerivate())
                .map(MCRObjectDerivate::getInternals)
                .map(MCRMetaIFS::getMainDoc)
                .orElseThrow(() -> new RuntimeException(
                    "Could not get main doc for " + derivateId));

            String spec;
            String baseURL = MCRFrontendUtil.getBaseURL();
            String id = URLEncoder.encode(derivateId.toString(), StandardCharsets.UTF_8);
            if (mainDoc != null && mainDoc.length() > 0) {
                String mainDocEnc = URLEncoder.encode(mainDoc, StandardCharsets.UTF_8);
                spec = String.format(Locale.ROOT, "%sservlets/MCRDFGLinkServlet?deriv=%s&file=%s", baseURL, id,
                    mainDocEnc);
            } else {
                spec = baseURL + "servlets/MCRDFGLinkServlet?deriv="
                    + id;
            }

            LOGGER.debug("Generated URL for urn {} is {}", urn, spec);
            return new URL(spec);
        } catch (MalformedURLException e) {
            LOGGER.error("Could not create dfg viewer url", e);
        }
        return null;
    }

    /**
     * @param file image file
     * @return if content type is in property <code>MCR.URN.URNGranular.SupportedContentTypes</code>
     * @see MCRContentTypes#probeContentType(Path)
     */
    private static boolean isFileSupported(MCRPath file) {
        try {
            return SUPPORTED_CONTENT_TYPE.contains(MCRContentTypes.probeContentType(file));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
        //        return true;
    }
}
