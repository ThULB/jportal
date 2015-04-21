package fsu.jportal.urn;

import java.util.UUID;

import org.mycore.urn.services.MCRAbstractURNProvider;
import org.mycore.urn.services.MCRURN;

/**
 * @author shermann
 */
public class UrmelURNProvider extends MCRAbstractURNProvider {
    private static final String URMEL = "urmel";

    /** Generates a single URN */
    public MCRURN generateURN() {
        return new MCRURN(MCRURN.getDefaultNamespaceIdentifiers(), UrmelURNProvider.URMEL + "-" + UrmelURNProvider.getUUID().toString());
    }

    /**
     * Generates multiple urns
     * 
     * @param int the amount of urn to generate, must be &gt;= 1
     */
    public MCRURN[] generateURN(int amount) {
        if (amount < 1) {
            return null;
        }

        MCRURN[] urn = new MCRURN[amount];

        for (int i = 1; i <= amount; i++) {
            urn[i - 1] = new MCRURN(MCRURN.getDefaultNamespaceIdentifiers(), UrmelURNProvider.URMEL + "-"
                    + UrmelURNProvider.getUUID().toString() + "-" + i);
        }
        return urn;
    }

    /** Central method to generate uuids */
    private static UUID getUUID() {
        return UUID.randomUUID();
    }

}
