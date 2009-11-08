package org.mycore.frontend.cli;

public class MCRJPortalRedundancyCommands extends MCRAbstractCommands {

    public MCRJPortalRedundancyCommands() {
        super();

        MCRCommand merge = new MCRCommand("merge file {1} of type {0} with redundancy map", "org.mycore.frontend.cli.command.MCRMergeOldRedundancyMap.merge String String", "");
        command.add(merge);

//        MCRCommand generatePersons = new MCRCommand("generate persons between {0} and {1}", "org.mycore.frontend.cli.command.MCRGenerateObjects.generatePersons int int", "generates persons between start and stop");
//        command.add(generatePersons);
//        
//        MCRCommand generateJpInst = new MCRCommand("generate institutions between {0} and {1}", "org.mycore.frontend.cli.command.MCRGenerateObjects.generateInstitutions int int", "generates institutions between start and stop");
//        command.add(generateJpInst);
    }
}