/**
 * 
 */
package org.fsu.jp.urn;

import java.util.UUID;

/**
 * @author shermann
 */
public class UrmelURNProvider {

    private static final String URMEL = "urmel";

    /** Generates a single URN */
    public static URN generateUrmelURN() {
        return new URN(URN.getDefaultNamespaceIdentifiers(), UrmelURNProvider.URMEL + "-"
                + UrmelURNProvider.getUUID().toString());
    }

    /**
     * Generates multiple urns
     * 
     * @param int the amount of urn to generate, must be &gt;= 1
     */
    public static URN[] generateUrmelURN(int amount) {
        if (amount < 1)
            return null;
        URN[] urn = new URN[amount];

        for (int i = 1; i <= amount; i++) {
            urn[i - 1] = new URN(URN.getDefaultNamespaceIdentifiers(), UrmelURNProvider.URMEL + "-"
                    + UrmelURNProvider.getUUID().toString() + "-" + i);
        }
        return urn;
    }

    /**
     * Generates multiple urns. The generated urns have the following structure
     * <code>&lt;base-urn&gt;-1</code> up to
     * <code>&lt;base-urn&gt;-amount</code>
     * 
     * @param int the amount of urn to generate, must be &gt;= 1
     * @param URN
     *            the base urn
     */
    public static URN[] generateUrmelURN(int amount, URN base) {
        if (base == null || amount < 1)
            return null;
        URN[] urn = new URN[amount];

        for (int i = 1; i <= amount; i++) {
            urn[i - 1] = new URN(base.getNamespaceIdentfiers(), base
                    .getNamespaceIdentfiersSpecificPart()
                    + "-" + UrmelURNProvider.addLeadingZeroes(amount, i));
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
     * @return an Array of {@link URN} or <code>null</code> if the base urn is
     *         null or amount &lt;1 or the setID &lt;0
     */
    public static URN[] generateUrmelURN(int amount, URN base, String setId) {
        if (base == null || amount < 1 || setId == null)
            return null;
        URN[] urn = new URN[amount];

        for (int i = 1; i <= amount; i++) {
            urn[i - 1] = new URN(base.getNamespaceIdentfiers(), base
                    .getNamespaceIdentfiersSpecificPart()
                    + "-" + setId + "-" + UrmelURNProvider.addLeadingZeroes(amount, i));
        }
        return urn;
    }

    /**
     * Method adds leading zeroes to the value parameter
     * 
     * @param digits
     *            the amount of digits
     * @param value
     *            the value to which the zeroes to add
     * */
    private static String addLeadingZeroes(int digits, int value) {
        StringBuilder builder = new StringBuilder();
        String maxS = String.valueOf(digits);
        String valueS = String.valueOf(value);
        int valueSLen = valueS.length();
        int maxSLen = maxS.length();

        /* in this case we must add zeroes */
        if (valueSLen < maxSLen) {
            int zeroesToAdd = maxSLen - valueSLen;
            for (int i = 0; i < zeroesToAdd; i++) {
                builder.append(0);
            }
            return builder.append(valueS).toString();
        }
        /* no need to add zeroes at all */
        return valueS;
    }

    /** Central method to generate uuids */
    private static UUID getUUID() {
        return UUID.randomUUID();
    }
}