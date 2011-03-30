package org.mycore.frontend.cli;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.mycore.datamodel.common.MCRLinkTableManager;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.parsers.bool.MCRAndCondition;
import org.mycore.services.fieldquery.MCRFieldDef;
import org.mycore.services.fieldquery.MCRFieldValue;
import org.mycore.services.fieldquery.MCRHit;
import org.mycore.services.fieldquery.MCRQuery;
import org.mycore.services.fieldquery.MCRQueryCondition;
import org.mycore.services.fieldquery.MCRQueryManager;
import org.mycore.services.fieldquery.MCRResults;

public class MCRJPortalRedundancyCommands extends MCRAbstractCommands {

    private static final Logger LOGGER = Logger.getLogger(MCRJPortalRedundancyCommands.class);

    public MCRJPortalRedundancyCommands() {
        super();

        MCRCommand cleanUp = new MCRCommand("jp clean up {0}", "org.mycore.frontend.cli.MCRJPortalRedundancyCommands.cleanUp String", 
                "Deletes and relinks all doublets for a specific type.");
        command.add(cleanUp);

        MCRCommand merge = new MCRCommand("merge file {1} of type {0} with redundancy map", "org.mycore.frontend.cli.command.MCRMergeOldRedundancyMap.merge String String", "");
        command.add(merge);

        MCRCommand replRemove = new MCRCommand("internal replace links and remove {0} {1}", "org.mycore.frontend.cli.MCRJPortalRedundancyCommands.replaceAndRemove String String",
                "internal command for replacing links and removing the doublet");
        command.add(replRemove);
    }

    public static List<String> cleanUp(String type) {
        ArrayList<String> commandList = new ArrayList<String>();
        // get all objects of specific type where doubletOf is not empty
        MCRQueryCondition typeCond = new MCRQueryCondition(MCRFieldDef.getDef("objectType"), "=", type);
        MCRQueryCondition doubletOfCond = new MCRQueryCondition(MCRFieldDef.getDef("doubletOf"), "like", "*");
        MCRAndCondition andCond = new MCRAndCondition(typeCond, doubletOfCond);
        MCRResults results = MCRQueryManager.search(new MCRQuery(andCond));

        Iterator<MCRHit> it = results.iterator();
        while(it.hasNext()) {
            MCRHit hit = it.next();
            String doublet = hit.getID();
            String doubletOf = getDoubletOf(hit);
            StringBuffer replaceCommand = new StringBuffer("internal replace links and remove ");
            replaceCommand.append(doublet).append(" ").append(doubletOf);
            commandList.add(replaceCommand.toString());
        }

        commandList.add(new StringBuffer("clean up redundancy in database for type ").append(type).toString());
        return commandList;
    }

    public static List<String> replaceAndRemove(String doublet, String doubletOf) throws Exception {
        ArrayList<String> commandList = new ArrayList<String>();
        if(!MCRMetadataManager.exists(MCRObjectID.getInstance(doubletOf))) {
            String errorMsg ="'" + doublet + "' is defined as a doublet of the nonexistent object '" + doubletOf + "'!" +
                " The doublet is not removed!";
            // print to console
            LOGGER.error(errorMsg);
            // write to file
            BufferedWriter out = new BufferedWriter(new FileWriter("invalidDoublets.txt", true)); 
            out.write(errorMsg + "\n"); 
            out.close();
            return commandList;
        }
        Collection<String> list = MCRLinkTableManager.instance().getSourceOf(doublet, "reference");
        for (String source : list) {
            // add replace command
            StringBuffer command = new StringBuffer("internal replace links ");
            command.append(source).append(" ");
            command.append(doublet).append(" ");
            command.append(doubletOf);
            commandList.add(command.toString());
        }
        // add delete command
        commandList.add(new StringBuffer("delete object ").append(doublet).toString());
        return commandList;
    }
    
    private static String getDoubletOf(MCRHit hit) {
        for(MCRFieldValue fieldValue : hit.getMetaData()) {
            if(fieldValue.getField().getName().equals("doubletOf"))
                return fieldValue.getValue();
        }
        return null;
    }

}