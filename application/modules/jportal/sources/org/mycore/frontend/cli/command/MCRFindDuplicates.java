package org.mycore.frontend.cli.command;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matthias Eichner
 */
public class MCRFindDuplicates {

    /**
     * Default method which finds all duplicates for a specified type.
     * @throws Exception
     */
    public static List<String> findDuplicates(String type) throws Exception {
        List<String> commands = new ArrayList<String>();
        // generate checkForDuplicates.xml
        commands.add("internal create checkForDuplicates.xml for type: " + type);
        commands.add("internal create redundancy.xml for type: " + type);
        return commands;
    }

}