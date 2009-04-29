package org.mycore.services.fieldquery;

import java.util.ArrayList;

import org.mycore.common.MCRTestCase;

public class MCRResultsTest extends MCRTestCase {

    public void testAndMerge() throws Exception {
        ArrayList<MCRResults> results = createSampleResults();
        MCRResults res1 = results.get(0);
        MCRResults res2 = results.get(1);
        MCRResults res3 = results.get(2);

        res1.and(res2);
        assertEquals(2, res1.getNumHits()); // 1 3
        res1.and(res3);
        assertEquals(1, res1.getNumHits()); // 1
        res2.and(res3);
        assertEquals(2, res2.getNumHits()); // 1 7
    }

    public void testOrMerge() throws Exception {
        ArrayList<MCRResults> results = createSampleResults();
        MCRResults res1 = results.get(0);
        MCRResults res2 = results.get(1);
        MCRResults res3 = results.get(2);

        res1.or(res2);
        assertEquals(5, res1.getNumHits()); // 0 1 2 3 7
        res1.or(res3);
        assertEquals(8, res1.getNumHits()); // all
        res2.or(res3);
        assertEquals(6, res2.getNumHits()); // 1 3 4 5 6 7
    }

    protected ArrayList<MCRResults> createSampleResults() {
        ArrayList<MCRResults> results = new ArrayList<MCRResults>();
        MCRResults res1 = new MCRResults();
        MCRResults res2 = new MCRResults();
        MCRResults res3 = new MCRResults();
        results.add(res1);
        results.add(res2);
        results.add(res3);

        String id0 = "0";
        String id1 = "1";
        String id2 = "2";
        String id3 = "3";
        String id4 = "4";
        String id5 = "5";
        String id6 = "6";
        String id7 = "7";

        // res1 has the first four hits
        res1.addHit(new MCRHit(id0));
        res1.addHit(new MCRHit(id1));
        res1.addHit(new MCRHit(id2));
        res1.addHit(new MCRHit(id3));
        // res2 has the hits 1, 3 and 5
        res2.addHit(new MCRHit(id1));
        res2.addHit(new MCRHit(id3));
        res2.addHit(new MCRHit(id7));
        // res3 has the hits 1, 4, 6 and 7
        res3.addHit(new MCRHit(id1));
        res3.addHit(new MCRHit(id4));
        res3.addHit(new MCRHit(id5));
        res3.addHit(new MCRHit(id6));
        res3.addHit(new MCRHit(id7));
        return results;
    }
}
