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

    public MCRURN generateURN() {
        return new MCRURN(MCRURN.getDefaultNamespaceIdentifiers(),
                          getNISS() + "-" + UrmelURNProvider.getUUID().toString());
    }

    public MCRURN[] generateURN(int amount) {
        return generateURN(amount, MCRURN.getDefaultNamespaceIdentifiers(),
                           i -> getNISS() + "-" + UrmelURNProvider.getUUID().toString() + "-" + i);
    }

    @Override
    public MCRURN[] generateURN(int amount, MCRURN base) {
        if (base == null) {
            return null;
        }

        String nsIdentfiersSpecPart = base.getNamespaceIdentfiersSpecificPart();
        String format = leadingZeros(amount);

        return generateURN(amount, base.getNamespaceIdentfiers(),
                            i -> nsIdentfiersSpecPart + "-" + String.format(format, i));
    }

    public MCRURN[] generateURN(int amount, MCRURN base, String setId) {
        if (Integer.parseInt(setId) < 0) {
            throw new IllegalArgumentException(
                    "setId must represent an integer >= 0, e.g. 1, 001 or 00004, but was " + setId);
        }

        if (base == null || setId == null) {
            return null;
        }

        String nsIdentfiersSpecPart = base.getNamespaceIdentfiersSpecificPart();
        String format = leadingZeros(amount);

        IntFunction<String> pattern = i -> nsIdentfiersSpecPart + "-" + setId + "-" + String.format(format, i);

        if (amount == 1) {
            pattern = i -> nsIdentfiersSpecPart + "-" + setId;
        }

        return generateURN(amount, base.getNamespaceIdentfiers(), pattern);
    }

    public String getNISS() {
        return UrmelURNProvider.NISS;
    }

    public MCRURN[] generateURN(int amount, String[] nsIdentifiers, IntFunction<String> pattern) {
        if (amount < 1) {
            return null;
        }

        MCRURN[] urns = new MCRURN[amount];

        for (int i = 0; i < amount; i++) {
            urns[i] = new MCRURN(nsIdentifiers, pattern.apply(i + 1));
        }

        return urns;
    }

    private long numDigits(long n) {
        if (n < 10)
            return 1;
        return 1 + numDigits(n / 10);
    }

    private String leadingZeros(int i) {
        return "%0" + numDigits(i) + "d";
    }

    /** Central method to nsSpecPart uuids */
    static UUID getUUID() {
        return UUID.randomUUID();
    }
}
