package fsu.jportal.backend.event;

import org.junit.Test;
import org.mycore.common.MCRTestCase;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRObjectID;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SolrOrderTest extends MCRTestCase {

    @Test
    public void calcIndexList() {
        SolrHandler handler = new SolrHandler();
        assertEquals(0, handler.calcIndexList(buildList(new int[] { 1, 2, 3 }), buildList(new int[] { 1, 2, 3 })).size());
        assertEquals(0, handler.calcIndexList(buildList(new int[] { 1, 3 }), buildList(new int[] { 1, 2, 3 })).size());
        assertEquals(2, handler.calcIndexList(buildList(new int[] { 3, 1 }), buildList(new int[] { 1, 2, 3 })).size());
        assertEquals(2, handler.calcIndexList(buildList(new int[] { 3, 2 ,1 }), buildList(new int[] { 1, 2, 3 })).size());
        assertEquals(2, handler.calcIndexList(buildList(new int[] { 1, 2, 3, 4, 5 }), buildList(new int[] { 1, 2, 3 })).size());
        assertEquals(3, handler.calcIndexList(buildList(new int[] { 1, 2, 3 }), buildList(new int[] { 4, 5, 6 })).size());
        assertEquals(0, handler.calcIndexList(buildList(new int[] { 1, 2, 5 }), buildList(new int[] { 1, 2, 3, 4, 5 })).size());
        assertEquals(1, handler.calcIndexList(buildList(new int[] { 1, 6 }), buildList(new int[] { 1, 2, 3, 4, 5 })).size());
        assertEquals(5, handler.calcIndexList(buildList(new int[] { 4, 1, 5, 2, 3, 7 }), buildList(new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 })).size());
    }

    private List<MCRMetaLinkID> buildList(int[] values) {
        List<MCRMetaLinkID> list = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            int id = values[i];
            MCRMetaLinkID link = new MCRMetaLinkID();
            MCRObjectID mcrId = MCRObjectID.getInstance("jportal_jparticle_" + id);
            link.setReference(mcrId, null, null);
            list.add(link);
        }
        return list;
    }

}
