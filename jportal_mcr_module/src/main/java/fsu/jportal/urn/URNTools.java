package fsu.jportal.urn;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.backend.hibernate.MCRHIBConnection;
import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.metadata.*;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.pi.MCRPIRegistrationInfo;
import org.mycore.pi.MCRPIRegistrationService;
import org.mycore.pi.MCRPIRegistrationServiceManager;
import org.mycore.pi.MCRPersistentIdentifier;
import org.mycore.pi.MCRPersistentIdentifierManager;
import org.mycore.pi.backend.MCRPI;
import org.mycore.pi.urn.rest.MCRDerivateURNUtils;
import org.mycore.pi.urn.rest.MCREpicurLite;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.Optional;
import java.util.function.Function;

public class URNTools {
    private static Logger LOGGER = LogManager.getLogger();

    public static String SERVICEID = "DNBURNGranular";

    public static void updateURNFileName(MCRPI urn, MCRPath newName) {
        boolean registered = true;

        if (newName != null) {
            urn.setAdditional(newName.getOwnerRelativePath().toString());
            registered = false;
        }

        if (!registered) {
            urn.setRegistered(null);

            MCRHIBConnection.instance().getSession().update(urn);

            MCRObjectID id = MCRObjectID.getInstance(urn.getMycoreID());
            MCRDerivate derivate = MCRMetadataManager.retrieveMCRDerivate(id);

            MCRObjectDerivate objectDerivate = derivate.getDerivate();
            for (MCRFileMetadata mcrFileMetadata : objectDerivate.getFileMetadata()) {
                String urnStr = mcrFileMetadata.getUrn();
                if (urnStr != null && urnStr.equals(urn.getIdentifier())) {
                    mcrFileMetadata.setName(urn.getAdditional());
                }
            }
            MCRMetadataManager.updateMCRDerivateXML(derivate);
        }
    }

    public static MCRPI getURNForFile(MCRPath file) {
        MCRObjectID derivID = MCRObjectID.getInstance(file.getOwner());
        String additional = file.getOwnerRelativePath().toString();
        return getURNForFile(derivID, additional);
    }

    public static MCRPI getURNForFile(MCRObjectID derivID, String additional) {
        try {
            return MCRPersistentIdentifierManager
                .getInstance()
                .get(SERVICEID, derivID.toString(), additional);
        } catch (NoResultException e) {
            return null;
        }
    }

    public static String getURNForFile(String derivID, String path) {
        return Optional
            .ofNullable(getURNForFile(MCRObjectID.getInstance(derivID), path))
            .map(MCRPI::getIdentifier)
            .orElse("");
    }

    public static void updateURN(MCRPath sourceNode, MCRPath target) {
        if (!Files.exists(sourceNode) || !Files.exists(target)) {
            return;
        }

        MCRPI urn = getURNForFile(sourceNode);
        if (urn == null) {
            return;
        }

        updateURNFileName(urn, target);
    }

    public static Optional<UsernamePasswordCredentials> getUsernamePassword() {
        String username = MCRConfiguration.instance().getString("MCR.URN.DNB.Credentials.Login", null);
        String password = MCRConfiguration.instance().getString("MCR.URN.DNB.Credentials.Password", null);

        if (username == null || password == null || username.length() == 0 || password.length() == 0) {
            LOGGER.warn("Please set MCR.URN.DNB.Credentials.Login and MCR.URN.DNB.Credentials.Password");
            return Optional.empty();
        }

        return Optional.of(new UsernamePasswordCredentials(username, password));
    }

    public static Function<MCRPIRegistrationInfo, MCREpicurLite> getEpicureProvider(
        UsernamePasswordCredentials credentials) {
        return urn -> MCREpicurLite.instance(urn, MCRDerivateURNUtils.getURL(urn))
            .setCredentials(credentials);
    }

    public static MCRPIRegistrationService<MCRPersistentIdentifier> getURNServiceManager() {
        return MCRPIRegistrationServiceManager
            .getInstance().getRegistrationService(SERVICEID);
    }

    public static boolean hasURNAssigned(String derivID) {
        return hasURNAssigned(MCRObjectID.getInstance(derivID));
    }

    public static boolean hasURNAssigned(MCRObjectID derivateID) {
        return getURNServiceManager().isCreated(derivateID, "");
    }
}
