package org.mycore.frontend.cli;

import java.util.HashMap;

public class MCRJournalStatsUtilities {

    public static String decreaseValues(String values, String range) {

        int valuecounter = 0;
        int n = Integer.parseInt(range);
        int divider = 2;
        int absolute = 0;
        int module = 0;
        float tempvalue = 0;
        HashMap<Integer, Float> valueMap = new HashMap<Integer, Float>();
        boolean dividerFound = false;
        boolean modulevalues = false;
        String decreasedValues = "";
        String tempValues = values;
        float firstValue = 0;
        // counts values, if them more than n then the number is decreased
        // and put all values into a HashMap
        while (tempValues.contains(",")) {
            if (valuecounter == 0) {
                firstValue = Float.parseFloat(tempValues.substring(0,
                        tempValues.indexOf(",")));
                tempValues = tempValues.substring(tempValues.indexOf(",") + 1);
            } else {
                valueMap.put(valuecounter - 1, Float.parseFloat(tempValues
                        .substring(0, tempValues.indexOf(","))));
                tempValues = tempValues.substring(tempValues.indexOf(",") + 1);
            }
            valuecounter++;
        }
        valuecounter--;

        float lastValue = Float.parseFloat(tempValues);

        if ((valuecounter + 2) > n) {
            while (!dividerFound) {
                absolute = valuecounter / divider;
                module = valuecounter % divider;
                if ((absolute + module) <= (n + 1)) {
                    dividerFound = true;
                } else {
                    divider++;
                }
            }

            decreasedValues = Float.toString(firstValue);

            for (int i = 0; i < absolute; i++) {
                tempvalue = 0;
                for (int k = 0; k < divider; k++) {
                    tempvalue = tempvalue + valueMap.get(((i * divider) + k));
                }
                tempvalue = tempvalue / divider;

                decreasedValues = decreasedValues.concat(",");
                decreasedValues = decreasedValues.concat(Float
                        .toString(tempvalue));
            }
            tempvalue = 0;
            for (int k = 0; k < module; k++) {
                tempvalue = tempvalue + valueMap.get((valuecounter - 1 - k));
                modulevalues = true;
            }
            if (modulevalues) {
                tempvalue = tempvalue / module;
                decreasedValues = decreasedValues.concat(",");
                decreasedValues = decreasedValues.concat(Float
                        .toString(tempvalue));
            }
            decreasedValues = decreasedValues.concat(",");
            decreasedValues = decreasedValues.concat(Float.toString(lastValue));
        } else {
            decreasedValues = values;
        }

        return decreasedValues;
    }

    public static String decreaseLabels(String xLabels, String yLabels,
            String range) {
        String decresedLabels = "";

        int valuecounter = 0;
        int n = Integer.parseInt(range);

        HashMap<Integer, String> valueMap = new HashMap<Integer, String>();
        String tempValues = xLabels;
        String firstValue = "";
        String lastValue = "";
        String xLabels1 = "";
        String xLabels2 = "";
        float use = 0;

        // counts values, if them more than n then the number is decreased
        // and put all values into a HashMap
        while (tempValues.contains("|")) {
            if (valuecounter == 0) {
                firstValue = tempValues.substring(0, tempValues.indexOf("|"));
                tempValues = tempValues.substring(tempValues.indexOf("|") + 1);
            } else {
                valueMap.put(valuecounter - 1, tempValues.substring(0,
                        tempValues.indexOf("|")));
                tempValues = tempValues.substring(tempValues.indexOf("|") + 1);
            }
            valuecounter++;
        }
        valuecounter--;
        lastValue = tempValues;

        if ((valuecounter + 2) > n) {

            use = (float) valuecounter / (float) (n - 2);

            int x = new Float(use).intValue();
            int left = x / 2;

            xLabels1 = xLabels1.concat("|").concat(firstValue);
            xLabels2 = xLabels2.concat("|").concat("");

            int evencounter = 0;
            for (int i = 1; i <= valuecounter; i++) {

                if (i % x == 0) {

                    if (evencounter % 2 == 0) {
                        xLabels1 = xLabels1.concat("|").concat("");
                        xLabels2 = xLabels2.concat("|").concat(
                                valueMap.get(i - 1 - left));
                    } else {
                        xLabels1 = xLabels1.concat("|").concat(
                                valueMap.get(i - 1 - left));
                        xLabels2 = xLabels2.concat("|").concat("");
                    }
                    evencounter++;
                    float temp = use * (float) (evencounter + 1);
                    x = new Float(temp).intValue();
                }
            }
            if (n % 2 == 0) {
                xLabels1 = xLabels1.concat("|").concat("");
                xLabels2 = xLabels2.concat("|").concat(lastValue);
            } else {
                xLabels1 = xLabels1.concat("|").concat(lastValue);
                xLabels2 = xLabels2.concat("|").concat("");
            }
        } else {
            xLabels1 = xLabels1.concat("|").concat(firstValue);
            xLabels2 = xLabels2.concat("|").concat("");
            for (int i = 0; i < valuecounter; i++) {
                if (i % 2 == 0) {
                    xLabels1 = xLabels1.concat("|").concat("");
                    xLabels2 = xLabels2.concat("|").concat(valueMap.get(i));
                } else {
                    xLabels1 = xLabels1.concat("|").concat(valueMap.get(i));
                    xLabels2 = xLabels2.concat("|").concat("");
                }
            }
            if (valuecounter % 2 == 0) {
                xLabels1 = xLabels1.concat("|").concat("");
                xLabels2 = xLabels2.concat("|").concat(lastValue);
            } else {
                xLabels1 = xLabels1.concat("|").concat(lastValue);
                xLabels2 = xLabels2.concat("|").concat("");
            }
        }

        decresedLabels = decresedLabels.concat("&chxt=x,y,x");
        decresedLabels = decresedLabels.concat("&chxl=");
        decresedLabels = decresedLabels.concat("0:" + xLabels1 + "|");
        decresedLabels = decresedLabels.concat("1:" + yLabels + "|");
        decresedLabels = decresedLabels.concat("2:" + xLabels2);

        return decresedLabels;
    }

}
