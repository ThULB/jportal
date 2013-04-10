package org.mycore.frontend.cli;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.transformer.MCRXSLTransformer;
import org.mycore.common.xsl.MCRParameterCollector;
import org.mycore.common.xsl.MCRXSLTransformerFactory;
import org.mycore.datamodel.common.MCRLinkTableManager;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.parsers.bool.MCRAndCondition;
import org.mycore.services.fieldquery.MCRFieldValue;
import org.mycore.services.fieldquery.MCRHit;
import org.mycore.services.fieldquery.MCRQuery;
import org.mycore.services.fieldquery.MCRQueryCondition;
import org.mycore.services.fieldquery.MCRQueryManager;
import org.mycore.services.fieldquery.MCRResults;

@MCRCommandGroup(name = "JP doubletOf Commands")
public class MCRJPortalRedundancyCommands{

    private static final Logger LOGGER = Logger.getLogger(MCRJPortalRedundancyCommands.class);

    @MCRCommand(helpKey = "Deletes and relinks all doublets for a specific type. Doublets signed with doubletOf", syntax = "fix title of {0} for link {1}")
    public static void removeDoublets(String objId, String linkID){
        MCRXSLTransformer transformer = new MCRXSLTransformer("/xsl/fixTitleOfLink.xsl");
        MCRXMLMetadataManager mcrxmlMetadataManager = MCRXMLMetadataManager.instance();
        MCRObjectID mcrObjId = MCRObjectID.getInstance(objId);
        
        try {
            MCRContent source = mcrxmlMetadataManager.retrieveContent(mcrObjId);
            MCRParameterCollector parameter = new MCRParameterCollector();
            parameter.setParameter("linkId", linkID);
            MCRContent fixedObj = transformer.transform(source, parameter);
            mcrxmlMetadataManager.update(mcrObjId, fixedObj, new Date());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @MCRCommand(helpKey = "Deletes and relinks all doublets for a specific type. Doublets signed with doubletOf", syntax = "jp clean up {0}")
    public static List<String> cleanUp(String type) {
        // get all objects of specific type where doubletOf is not empty
        MCRResults results = getDoubletObjsOfType(type);
        
        ArrayList<String> commandList = new ArrayList<String>();
        Iterator<MCRHit> it = results.iterator();
        while (it.hasNext()) {
            MCRHit hit = it.next();
            String doublet = hit.getID();
            String doubletOf = getDoubletOf(hit);
            if (!doublet.equals(doubletOf)) {
                StringBuffer replaceCommand = new StringBuffer("internal replace links and remove ");
                replaceCommand.append(doublet).append(" ").append(doubletOf);
                commandList.add(replaceCommand.toString());
            }
        }

//        commandList.add(new StringBuffer("clean up redundancy in database for type ").append(type).toString());
        return commandList;
    }

    public static MCRResults getDoubletObjsOfType(String type) {
        MCRQueryCondition typeCond = new MCRQueryCondition("objectType", "=", type);
        MCRQueryCondition doubletOfCond = new MCRQueryCondition("doubletOf", "like", "*");
        MCRAndCondition andCond = new MCRAndCondition(typeCond, doubletOfCond);
        MCRResults results = MCRQueryManager.search(new MCRQuery(andCond));
        return results;
    }

    @MCRCommand(helpKey = "internal command for replacing links and removing the doublet", syntax = "internal replace links and remove {0} {1}")
    public static List<String> replaceAndRemove(String doublet, String doubletOf) throws Exception {
        ArrayList<String> commandList = new ArrayList<String>();
        if (!MCRMetadataManager.exists(MCRObjectID.getInstance(doubletOf))) {
            String errorMsg = "'" + doublet + "' is defined as a doublet of the nonexistent object '" + doubletOf + "'!"
                    + " The doublet is not removed!";
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
            commandList.add("fix title of " + source + " for link " + doubletOf);
            
        }
        // add delete command
        commandList.add(new StringBuffer("delete object ").append(doublet).toString());
        return commandList;
    }

    private static String getDoubletOf(MCRHit hit) {
        for (MCRFieldValue fieldValue : hit.getMetaData()) {
            if (fieldValue.getFieldName().equals("doubletOf"))
                return fieldValue.getValue();
        }
        return null;
    }

}