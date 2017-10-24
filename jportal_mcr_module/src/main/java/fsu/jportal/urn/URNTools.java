package fsu.jportal.urn;

import fsu.jportal.resources.URNResource;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.metadata.*;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.pi.MCRPIRegistrationInfo;
import org.mycore.pi.urn.rest.MCRDerivateURNUtils;
import org.mycore.pi.urn.rest.MCREpicurLite;
import org.mycore.urn.hibernate.MCRURN;
import org.mycore.urn.services.MCRURNManager;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class URNTools {
    private static Logger LOGGER = LogManager.getLogger();

    public static void updateURNFileName(MCRURN urn, String path, String newName) {
        boolean registered = true;

        if (path != null && !"".equals(path.trim())) {
            urn.setPath(path);
            registered = false;
        }

        if (newName != null && !"".equals(newName.trim())) {
            urn.setFilename(newName);
            registered = false;
        }

        if (!registered) {
            urn.setRegistered(false);
            MCRURNManager.update(urn);

            MCRObjectID id = MCRObjectID.getInstance(urn.getId());
            MCRDerivate derivate = MCRMetadataManager.retrieveMCRDerivate(id);

            MCRObjectDerivate objectDerivate = derivate.getDerivate();
            for (MCRFileMetadata mcrFileMetadata : objectDerivate.getFileMetadata()) {
                String urnStr = mcrFileMetadata.getUrn();
                if (urnStr != null && urnStr.equals(urn.getURN())) {
                    mcrFileMetadata.setName(urn.getPath() + urn.getFilename());
                }
            }
            MCRMetadataManager.updateMCRDerivateXML(derivate);
        }
    }

    public static MCRURN getURNForFile(MCRPath file) {
        MCRObjectID derivID = MCRObjectID.getInstance(file.getOwner());
        // we need a method like MCRURNManager.get(MCRFile file) return MCRURN without a loop
        List<MCRURN> urnList = MCRURNManager.get(derivID);
        for (MCRURN urn : urnList) {
            String path = urn.getPath() + urn.getFilename();
            if (path.equals("/" + file.subpathComplete().toString())) {
                return urn;
            }
        }

        return null;
    }

    public static String getURNForFile(String derivID, String path) {
        //        MCRPersistentIdentifierManager manager = MCRPersistentIdentifierManager.getInstance();

        try {
            // TODO: fix manager
            EntityManager em = MCREntityManagerProvider.getCurrentEntityManager();
            return em.createQuery(
                    "select u from MCRPI u where u.service = :service and u.mycoreID = :mcrID and u.additional = :path",
                    MCRPIRegistrationInfo.class)
                     .setParameter("service", "DNBURNGranular")
                     .setParameter("mcrID", derivID)
                     .setParameter("path", URLDecoder.decode(path, "UTF-8"))
                     .getSingleResult()
                     .getIdentifier();
            //            return manager.get("DNBURNGranular", derivID, URLDecoder.decode(path, "UTF-8")).getIdentifier();
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Wrong encoding in derivate {} with xlink:href {}", derivID, path);
            e.printStackTrace();
        } catch (NoResultException e) {
            LOGGER.info("No URN for {}:{}.", derivID, path);
        }

        return "";
    }

    public static void updateURN(MCRPath sourceNode, MCRPath target) {
        if (!Files.exists(sourceNode) || !Files.exists(target)) {
            return;
        }

        MCRURN urn = getURNForFile(sourceNode);
        if (urn == null) {
            return;
        }

        String targetName = target.getFileName().toString();
        String targetPath = target.getParent().getOwnerRelativePath();
        if (!targetPath.endsWith("/")) {
            targetPath += "/";
        }

        updateURNFileName(urn, targetPath, targetName);
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
}
