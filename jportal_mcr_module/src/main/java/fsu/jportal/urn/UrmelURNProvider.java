package fsu.jportal.urn;

import java.util.UUID;

import org.mycore.common.config.MCRConfiguration;
import org.mycore.urn.services.MCRIURNProvider;
import org.mycore.urn.services.MCRURN;

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
                UrmelURNProvider.NISS + "-" + UrmelURNProvider.getUUID().toString());
    }

    public MCRURN[] generateURN(int amount) {
        NSSpecificPartPattern generator = new NSSpecificPartPattern() {
            @Override
            public String generate(int i) {
                return UrmelURNProvider.NISS + "-" + UrmelURNProvider.getUUID().toString() + "-" + i;
            }
        };

        return generateURN(amount, MCRURN.getDefaultNamespaceIdentifiers(), generator);
    }

    @Override
    public MCRURN[] generateURN(int amount, MCRURN base) {
        if (base == null) {
            return null;
        }

        PatternGenerator generator = new PatternGenerator(amount, base.getNamespaceIdentfiersSpecificPart());

        return generateURN(amount, base.getNamespaceIdentfiers(), generator);
    }

    public MCRURN[] generateURN(int amount, MCRURN base, String setId) {
        int parsedInt = Integer.parseInt(setId);

        if (parsedInt < 0) {
            throw new IllegalArgumentException(
                    "setId must represent an integer >= 0, e.g. 1, 001 or 00004, but was " + setId);
        }

        if (base == null || setId == null) {
            return null;
        }

        if (amount == 1) {
            return new MCRURN[] { generateURN(base, setId) };
        }

        PatternGenerator generator = new PatternGenerator(amount, base.getNamespaceIdentfiersSpecificPart());
        generator.setId(setId);

        return generateURN(amount, base.getNamespaceIdentfiers(), generator);
    }

    public String getNISS() {
        return UrmelURNProvider.NISS;
    }

    public MCRURN generateURN(MCRURN base, String setId) {
        return new MCRURN(base.getNamespaceIdentfiers(), base.getNamespaceIdentfiersSpecificPart() + "-" + setId);
    }

    public MCRURN[] generateURN(int amount, String[] nsIdentifiers, NSSpecificPartPattern pattern) {
        if (amount < 1) {
            return null;
        }

        MCRURN[] urns = new MCRURN[amount];

        for (int i = 0; i < amount; i++) {
            urns[i] = new MCRURN(nsIdentifiers, pattern.generate(i + 1));
        }

        return urns;
    }

    private class PatternGenerator implements NSSpecificPartPattern {
        private final String format;

        private final String nsIdentifiersSpecParts;

        private String id = "";

        public PatternGenerator(int leadingZeros, String nsIdentifiersSpecParts) {
            this.format = "%0" + leadingZeros + "d";
            this.nsIdentifiersSpecParts = nsIdentifiersSpecParts;
        }

        public void setId(String id) {
            this.id = id + "-";
        }

        @Override
        public String generate(int i) {
            return nsIdentifiersSpecParts + "-" + id + String.format(format, i);
        }
    }

    interface NSSpecificPartPattern {
        public String generate(int i);
    }

    /** Central method to generate uuids */
    static UUID getUUID() {
        return UUID.randomUUID();
    }
}
