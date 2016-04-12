package fsu.jportal.urn;

import java.util.UUID;

import org.mycore.common.config.MCRConfiguration;
import org.mycore.urn.services.MCRAbstractURNProvider;
import org.mycore.urn.services.MCRURN;

/**
 * @author chi
 */
public class UrmelURNProvider extends MCRAbstractURNProvider {

    public static final String NISS;

    static {
        NISS = MCRConfiguration.instance().getString("MCR.URN.NISS", "urmel");
    }

    /** Generates a single URN */
    public MCRURN generateURN() {
        return MCRURN.create("urn:nbn:de:" + UrmelURNProvider.NISS + "-" + UrmelURNProvider.getUUID().toString());
    }

    /**
     * Generates multiple urns
     * 
     * @param int the amount of urn to generate, must be &gt;= 1
     */
    public MCRURN[] generateURN(int amount) {
        if (amount < 1)
            return null;
        MCRURN[] urn = new MCRURN[amount];

        for (int i = 1; i <= amount; i++) {
            urn[i - 1] = MCRURN.create("urn:nbn:de:",
                UrmelURNProvider.NISS + "-" + UrmelURNProvider.getUUID().toString() + "-" + i);
        }
        return urn;
    }

    /**
     * Generates multiple urns. The generated urns have the following structure
     * <code>&lt;base-urn&gt;-setId-1</code> up to
     * <code>&lt;base-urn&gt;-setId-amount</code>
     * 
     * @param amount
     *            the amount of urn to generate, must be &gt;= 1
     * @param base
     *            the base urn
     * @param setId
     *            must represent an integer &gt;= 0, e.g. 1, 001 or 00004
     * @return an Array of {@link MCRURN} or <code>null</code> if the base urn
     *         is null or amount &lt;1 or the setID &lt;0
     */
    public MCRURN[] generateURN(int amount, MCRURN base, String setId) {
        if (base == null || amount < 1 || setId == null)
            return null;

        if (amount == 1) {
            MCRURN[] urn = new MCRURN[1];
            urn[0] = MCRURN.create(base.getSubNamespaces(), base.getNamespaceSpecificString() + "-" + setId);
            return urn;
        }

        return super.generateURN(amount, base, setId);
    }

    /** Central method to generate uuids */
    private static UUID getUUID() {
        return UUID.randomUUID();
    }

    /* (non-Javadoc)
     * @see org.mycore.services.urn.MCRAbstractURNProvider#getNISS()
     */
    @Override
    public String getNISS() {
        return UrmelURNProvider.NISS;
    }

}
