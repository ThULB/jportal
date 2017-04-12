package fsu.jportal.frontend.cli;

import org.junit.Test;
import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.common.MCRJPATestCase;
import org.mycore.pi.exceptions.MCRPersistentIdentifierException;
import org.mycore.pi.urn.MCRDNBURN;
import org.mycore.pi.urn.MCRUUIDURNGenerator;
import org.mycore.urn.hibernate.MCRURN;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.stream.IntStream;

/**
 * Created by chi on 06.04.17.
 *
 * @author Huu Chi Vu
 */
public class URNMigrationTest extends MCRJPATestCase {
    @Test
    public void migrateURN() throws Exception {
        EntityManager em = MCREntityManagerProvider.getCurrentEntityManager();
        IntStream.iterate(0, i -> i + 1)
                 .mapToObj(this::createMockMCRURN)
                 .limit(35067)
                 .forEach(em::persist);

        EntityTransaction transaction = em.getTransaction();

        if (!transaction.isActive()) {
            transaction.begin();
        }

        transaction.commit();

        URNMigration.migrateURN("TestService");

        EntityManager newEM = MCREntityManagerProvider.getEntityManagerFactory()
                                                      .createEntityManager();

        Number result = newEM.createQuery("select count(u) from MCRPI u", Number.class)
                             .getSingleResult();

        System.out.println("Number migrated MCRURN: " + result.toString());

    }

    private MCRURN createMockMCRURN(int i) {
        MCRUUIDURNGenerator testGen = new MCRUUIDURNGenerator("TestGen");

        try {
            MCRDNBURN testURN = testGen.generate("fooName-", null);

            MCRURN mcrurn = new MCRURN("foo-ID", testURN.asString());

            if (i == 1) {
                mcrurn.setDfg(true);
            } else {
                mcrurn.setPath("/");
                mcrurn.setFilename("file_" + i + ".jpg");
            }

            return mcrurn;
        } catch (MCRPersistentIdentifierException e) {
            e.printStackTrace();
        }

        return null;
    }
}