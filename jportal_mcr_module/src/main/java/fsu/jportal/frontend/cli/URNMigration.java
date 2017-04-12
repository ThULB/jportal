package fsu.jportal.frontend.cli;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.pi.backend.MCRPI;
import org.mycore.pi.urn.MCRDNBURN;
import org.mycore.pi.urn.MCRDNBURNParser;
import org.mycore.urn.hibernate.MCRURN;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by chi on 06.04.17.
 *
 * @author Huu Chi Vu
 */
@MCRCommandGroup(name = "JP URN Migrating Commands")
public class URNMigration {
    private static Logger LOGGER = LogManager.getLogger();
    private static Stream<MCRPI> toMCRPI(MCRURN mcrurn, String serviceID) {
        String derivID = mcrurn.getId();
        String additional = Optional
                .ofNullable(mcrurn.getPath())
                .flatMap(path -> Optional.ofNullable(mcrurn.getFilename())
                                         .map(filename -> Paths.get(path, filename)))
                .map(Path::toString)
                .orElse("");

        MCRPI mcrpi = new MCRPI(mcrurn.getURN(), MCRDNBURN.TYPE, derivID, additional, serviceID, null);
        String suffix = "-dfg";

        return Optional.of(mcrurn)
                       .filter(u -> u.isDfg())
                       .flatMap(URNMigration::parse)
                       .map(dnbURN -> dnbURN.withSuffix(suffix))
                       .map(MCRDNBURN::asString)
                       .map(dfgURN -> new MCRPI(dfgURN, MCRDNBURN.TYPE + suffix, derivID, additional, serviceID, null))
                       .map(dfgMcrPi -> Stream.of(mcrpi, dfgMcrPi))
                       .orElse(Stream.of(mcrpi));
    }

    private static Optional<MCRDNBURN> parse(MCRURN urn) {
        return new MCRDNBURNParser().parse(urn.getURN());
    }

    private static void logInfo(MCRPI urn){
        String urnStr = urn.getIdentifier();
        String mycoreID = urn.getMycoreID();
        String path = urn.getAdditional();
        LOGGER.info("Migrating: {} - {}:{}", urnStr, mycoreID, path);
    }

    @MCRCommand(syntax = "migrate urn with serveID {0}")
    public static void migrateURN(String serviceID) {
        EntityManager entityManager = MCREntityManagerProvider.getCurrentEntityManager();

        entityManager.createQuery("select u from MCRURN u", MCRURN.class)
                     .getResultList()
                     .stream()
                     .flatMap(mcrurn -> toMCRPI(mcrurn, serviceID))
                     .peek(URNMigration::logInfo)
                     .forEach(entityManager::persist);

        EntityTransaction transaction = entityManager.getTransaction();

        if (!transaction.isActive()) {
            transaction.begin();
        }

        transaction.commit();
    }

}
