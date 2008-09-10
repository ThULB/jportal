package org.mycore.frontend.cli;

import java.util.HashMap;

import org.mycore.datamodel.metadata.MCRMetaLink;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

public class MCRJournalStats extends MCRAbstractCommands {

    private MCRObjectID ID;
    private String name;
    private String JournalType;
    private String ObjectFocus;

    private HashMap<Integer, MCRObjectID> incompleteObjects = new HashMap<Integer, MCRObjectID>();
    private HashMap<Integer, MCRObjectID> MissingChildObj = new HashMap<Integer, MCRObjectID>();
    private HashMap<Integer, String> derivateTypes = new HashMap<Integer, String>();
    private HashMap<String, long[]> derivateStats = new HashMap<String, long[]>();

    private int GoodObjectCounter = 0;
    private int BadObjectCounter = 0;
    private int OverallCounter = 0;
    private int MissingChildrenCounter = 0;

    private static int AllObjectsCounter = 0;

    MCRJournalStats(MCRObjectID ID, String JournalType) {
        MCRObject Journal = new MCRObject();
        Journal.receiveFromDatastore(ID);
        this.ID = ID;
        this.name = Journal.getMetadata().createXML().getChild("maintitles")
                .getChild("maintitle").getText();
        this.ObjectFocus = Journal.getMetadata().createXML().getChild(
                "dataModelCoverages").getChild("dataModelCoverage")
                .getAttributeValue("categid");
        this.JournalType = JournalType;
    }

    public void incompleteObj(MCRObjectID ID) {
        incompleteObjects.put(BadObjectCounter, ID);

        this.BadObjectCounter++;
        this.OverallCounter++;
        AllObjectsCounter++;
    }

    public void completeObj(MCRObjectID ID) {
        this.GoodObjectCounter++;
        this.OverallCounter++;
        AllObjectsCounter++;
    }

    public void MissingChildObj(MCRObjectID ID) {
        MissingChildObj.put(MissingChildrenCounter, ID);
        this.OverallCounter++;

        this.MissingChildrenCounter++;
    }

    public int getGoodCounter() {
        return this.GoodObjectCounter;
    }

    public int getBadCounter() {
        return this.BadObjectCounter;
    }

    public int getAllCounter() {
        return this.OverallCounter;
    }

    public int getMissingChildrenCounter() {
        return this.MissingChildrenCounter;
    }

    public static int getAllObjectsCounter() {
        return AllObjectsCounter;
    }

    public MCRObjectID getJournalID() {
        return this.ID;
    }

    public String getJournalName() {
        return this.name;
    }

    public String getType() {
        return this.JournalType;
    }

    public String getObjectFocus() {
        return this.ObjectFocus;
    }

    public HashMap<Integer, MCRObjectID> getIncompleteObjects() {
        return this.incompleteObjects;
    }

    public HashMap<Integer, MCRObjectID> getMissingChildren() {
        return this.MissingChildObj;
    }

    public HashMap<Integer, String> getDerivateTypes() {
        return this.derivateTypes;
    }

    public HashMap<String, long[]> getDerivateStats() {
        return this.derivateStats;
    }

    public void setDerivates(String type, long size) {
        if (getDerivateTypes().containsValue(type)) {
            long[] values = derivateStats.get(type);
            values[0]++;
            values[1] = values[1] + size;
        } else {
            long[] values = { 0, 0 };
            values[1] = size;
            derivateStats.put(type, values);
            derivateTypes.put(derivateTypes.size(), type);
        }
    }
}
