package org.mycore.frontend.cli;

public class MCRDuplicateCommands extends MCRAbstractCommands {

    public MCRDuplicateCommands() {
        super();
        
        MCRCommand generateCommand = new MCRCommand("generate redundancy map for type {0}", "org.mycore.frontend.cli.command.MCRGenerateRedundancyMap.generate String", "generates a redundancy map for a specific type.");
        command.add(generateCommand);

        MCRCommand cleanUpRed = new MCRCommand("clean up redundancy in database for type: {0}", "org.mycore.frontend.cli.command.MCRCleanUpRedundancyInDB.cleanUp String", "cleans up the redundancy in the database of a specific type.");
        command.add(cleanUpRed);

        MCRCommand merge = new MCRCommand("merge file {1} of type {0} with redundancy map", "org.mycore.frontend.cli.command.MCRMergeOldRedundancyMap.merge String String", "");
        command.add(merge);
//        MCRCommand generatePersons = new MCRCommand("generate persons between {0} and {1}", "org.mycore.frontend.cli.command.MCRGenerateObjects.generatePersons int int", "generates persons between start and stop");
//        command.add(generatePersons);
//        
//        MCRCommand generateJpInst = new MCRCommand("generate institutions between {0} and {1}", "org.mycore.frontend.cli.command.MCRGenerateObjects.generateInstitutions int int", "generates institutions between start and stop");
//        command.add(generateJpInst);

        MCRCommand prInternalProcessRedundancyObjects = new MCRCommand("internal process redundancy object {0}", "org.mycore.frontend.cli.command.MCRCleanUpRedundancyInDB.processRedundancyObject String", "");
        command.add(prInternalProcessRedundancyObjects);

        MCRCommand prInternalReplaceLinks = new MCRCommand("internal replace links {0} {1} {2}", "org.mycore.frontend.cli.command.MCRCleanUpRedundancyInDB.replaceLinks String String String", "");
        command.add(prInternalReplaceLinks);
        
        MCRCommand prInternalDeleteRedundancyEntry = new MCRCommand("internal delete redundancy object xml entry {0}", "org.mycore.frontend.cli.command.MCRCleanUpRedundancyInDB.deleteRedundancyElementEntry String", "");
        command.add(prInternalDeleteRedundancyEntry);

        MCRCommand prInternalUpdateXMLDocument = new MCRCommand("internal update xml document {0}", "org.mycore.frontend.cli.command.MCRCleanUpRedundancyInDB.updateXMLDocument String", "");
        command.add(prInternalUpdateXMLDocument);
    }
}