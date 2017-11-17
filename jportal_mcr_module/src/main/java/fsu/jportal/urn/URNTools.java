package fsu.jportal.urn;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.output.DOMOutputter;
import org.mycore.backend.hibernate.MCRHIBConnection;
import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRFileMetadata;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectDerivate;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.pi.MCRPIRegistrationInfo;
import org.mycore.pi.MCRPIRegistrationService;
import org.mycore.pi.MCRPIRegistrationServiceManager;
import org.mycore.pi.MCRPersistentIdentifier;
import org.mycore.pi.MCRPersistentIdentifierManager;
import org.mycore.pi.backend.MCRPI;
import org.mycore.pi.backend.MCRPI_;
import org.mycore.pi.urn.MCRDNBURN;
import org.mycore.pi.urn.rest.MCRDNBURNRestClient;
import org.mycore.pi.urn.rest.MCRDerivateURNUtils;
import org.mycore.pi.urn.rest.MCREpicurLite;
import org.w3c.dom.NodeList;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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


    public static String createAlternativeURN(String urn, String toAppend) {
        LOGGER.info("Base URN: " + urn + ", adding string '" + toAppend + "'");

        String[] parts = urn.split("-");
        StringBuilder b = new StringBuilder(parts[0] + "-" + toAppend);
        for (int i = 1; i < parts.length; i++) {
            b.append("-" + parts[i]);
        }

        return b.toString();
    }

    public static NodeList getURNsForMCRID(String mcrid) {
        EntityManager em = MCREntityManagerProvider.getCurrentEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery getQuery = cb.createQuery(MCRPIRegistrationInfo.class);
        Root pi = getQuery.from(MCRPI.class);
        List<MCRPIRegistrationInfo> mcrPiList = em
                .createQuery(getQuery.select(pi).where(cb.equal(pi.get(MCRPI_.mycoreID), mcrid)))
                .getResultList();

        List<Element> fileElemList = mcrPiList
                .stream()
                .map(piInfo -> {
                    Element fileElem = new Element("file");
                    fileElem.setAttribute("urn", piInfo.getIdentifier());
                    String additional = piInfo.getAdditional();
                    if(additional.startsWith("/")){
                        additional = additional.substring(1);
                    }
                    fileElem.setAttribute("name", additional);

                    return fileElem;
                })
                .collect(Collectors.toList());

        Element root = new Element("root");
        root.addContent(fileElemList);

        DOMOutputter domOutputter = new DOMOutputter();
        try {
            return domOutputter.output(root).getChildNodes();
        } catch (JDOMException e) {
            e.printStackTrace();
        }

        return null;
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

    /**
     * @param derivateID
     * @return Stream of registerd and not registered URNs
     */
    public static Stream<MCRPI> registerURNs(MCRObjectID derivateID) {
        MCRDNBURNRestClient urnRestClient = getUsernamePassword()
                .map(URNTools::getEpicureProvider)
                .map(MCRDNBURNRestClient::new)
                .orElseThrow(() -> new MCRException("Could not create URN Rest client."));

        return MCRPersistentIdentifierManager.getInstance()
                                             .getCreatedIdentifiers(derivateID, MCRDNBURN.TYPE, SERVICEID)
                                             .stream()
                                             .map(MCRPI.class::cast)
                                             .map(pi -> urnRestClient.register(pi)
                                                                     .map(date -> {
                                                                         pi.setRegistered(date);
                                                                         return pi;
                                                                     })
                                                                     .orElse(pi));
    }
}
