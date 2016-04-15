package fsu.jportal.urn;

import org.mycore.common.config.MCRConfiguration;
import org.mycore.urn.services.MCRIURNProvider;
import org.mycore.urn.services.MCRURN;

import java.util.UUID;
import java.util.function.IntFunction;

/**
 * @author chi
 */
public class UrmelURNProvider implements MCRIURNProvider {

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
     * @param amount the amount of urn to generate, must be &gt;= 1
     */
    public MCRURN[] generateURN(int amount) {
        if (amount < 1)
            return null;
        MCRURN[] urn = new MCRURN[amount];

        for (int i = 1; i <= amount; i++) {
            urn[i - 1] = MCRURN.create(NISS, UrmelURNProvider.getUUID().toString() + "-" + i);
        }
        return urn;
    }

    @Override
    public MCRURN[] generateURN(int amount, MCRURN base) {
        if (base == null) {
            return null;
        }

        String nsIdentfiersSpecPart = base.getNamespaceSpecificString();
        String format = leadingZeros(amount);

        return generateURN(amount, NISS,
                           i -> nsIdentfiersSpecPart + "-" + String.format(format, i));
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
        if (Integer.parseInt(setId) < 0) {
            throw new IllegalArgumentException(
                    "setId must represent an integer >= 0, e.g. 1, 001 or 00004, but was " + setId);
        }

        if (base == null || setId == null) {
            return null;
        }

        String nsIdentfiersSpecPart = base.getNamespaceSpecificString();
        String format = leadingZeros(amount);

        IntFunction<String> pattern = i -> nsIdentfiersSpecPart + "-" + setId + "-" + String.format(format, i);

        if (amount == 1) {
            pattern = i -> nsIdentfiersSpecPart + "-" + setId;
        }

        return generateURN(amount, NISS, pattern);
    }

    public MCRURN[] generateURN(int amount, String niss, IntFunction<String> pattern) {
        if (amount < 1) {
            return null;
        }

        MCRURN[] urns = new MCRURN[amount];

        for (int i = 0; i < amount; i++) {
            urns[i] = MCRURN.create(niss, pattern.apply(i + 1));
        }

        return urns;
    }

    private String leadingZeros(int i) {
        return "%0" + numDigits(i) + "d";
    }

    private long numDigits(long n) {
        if (n < 10)
            return 1;
        return 1 + numDigits(n / 10);
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
        return NISS;
    }

}
