package fsu.jportal.frontend.cli;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.pi.backend.MCRPI;
import org.mycore.pi.urn.MCRDNBURN;
import org.mycore.pi.urn.MCRDNBURNParser;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by chi on 09.11.17.
 */
@MCRCommandGroup(name = "URN CLI")
public class URNMigration {
    private static Logger LOGGER = LogManager.getLogger();

    @MCRCommand(syntax = "native sql {0}", help = "native sql [query]")
    public static void nativeSQL(String query) {
        int i = getNativeQuery(query)
            .executeUpdate();

        LogManager.getLogger().info("executing '" + query + "' with " + i);
    }

    private static Query getNativeQuery(String query) {
        return MCREntityManagerProvider
            .getCurrentEntityManager()
            .createNativeQuery(query);
    }

    @MCRCommand(syntax = "migrate diff to {0}", help = "migrate diff to [serviceID]")
    public static void migrateDiff(String serviceID) {
        String query = "SELECT A.mcrid, A.mcrurn, A.path, A.filename, A.registered, A.dfg "
            + "from mcrurn A LEFT JOIN mcrpi B ON (A.mcrurn = B.identifier) where B.identifier is NULL";
        List<Object[]> resultList = getNativeQuery(query).getResultList();
        migrateResultList(serviceID, resultList);
    }

    /**
     *
     * @param mcrurnCol as Object[] from SELECT u.mcrid, u.mcrurn, u.path, u.filename, u.registered, u.dfg from mcrurn u
     * @param serviceID
     * @return
     */
    private static Stream<MCRPI> toMCRPI(Object[] mcrurnCol, String serviceID) {
        String mcrid = mcrurnCol[0].toString();
        String mcrurn = mcrurnCol[1].toString();
        String path = mcrurnCol[2].toString();
        String filename = mcrurnCol[3].toString();
        boolean isDfg = (boolean)mcrurnCol[5];

        String derivID = mcrid;
        String additional = Optional
            .ofNullable(path)
            .flatMap(p -> Optional.ofNullable(filename)
                .map(f -> Paths.get(path, f)))
            .map(Path::toString)
            .orElse("");

        MCRPI mcrpi = new MCRPI(mcrurn, MCRDNBURN.TYPE, derivID, additional, serviceID, null);
        String suffix = "-dfg";

        return parse(mcrurn)
            .filter(u -> isDfg)
            .map(dnbURN -> dnbURN.withSuffix(suffix))
            .map(MCRDNBURN::asString)
            .map(dfgURN -> new MCRPI(dfgURN, MCRDNBURN.TYPE + suffix, derivID, additional, serviceID + suffix, null))
            .map(dfgMcrPi -> Stream.of(mcrpi, dfgMcrPi))
            .orElse(Stream.of(mcrpi));
    }

    private static Optional<MCRDNBURN> parse(String urn) {
        return new MCRDNBURNParser().parse(urn);
    }

    private static void logInfo(MCRPI urn) {
        String urnStr = urn.getIdentifier();
        String mycoreID = urn.getMycoreID();
        String path = urn.getAdditional();
        LOGGER.info("Migrating: {} - {}:{}", urnStr, mycoreID, path);
    }

    @MCRCommand(syntax = "migrate urn with serveID {0}")
    public static void migrateURN(String serviceID) {
        migrateURN(serviceID, null);
    }

    @MCRCommand(syntax = "migrate urn with serveID {0} derivID {1}")
    public static void migrateURN(String serviceID, String derivID) {

        String qlString = "SELECT u.mcrid, u.mcrurn, u.path, u.filename, u.registered, u.dfg from mcrurn u";
        if (derivID != null && derivID != "") {
            qlString = qlString + " where mcrid = '" + derivID + "'";
        }

        List<Object[]> resultList = getNativeQuery(qlString).getResultList();
        migrateResultList(serviceID, resultList);
    }

    private static void migrateResultList(String serviceID, List<Object[]> resultList) {
        EntityManager entityManager = MCREntityManagerProvider.getCurrentEntityManager();
        resultList.stream()
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
