package org.mycore.frontend.cli;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class MCRUsersGroupsTools extends MCRAbstractCommands {
    private static Logger LOGGER = Logger.getLogger(MCRUsersGroupsTools.class.getName());

    public MCRUsersGroupsTools() {
        super();

        MCRCommand com = null;

        com = new MCRCommand("add {0} to {1}", "org.mycore.frontend.cli.MCRUsersGroupsTools.addUsers String String",
                "add [userIDs] to [groupIDs]\n usersIDs: user1,user2,...\n groupIDs: group1, group2,...");
        command.add(com);
    }

    public static List<String> addUsers(String userIDs, String groupIDS) {
        String[] userList = userIDs.split(",");
        String[] groupList = groupIDS.split(",");
        List<String> cmdList = new ArrayList<String>();
        String cmd = "";

        for (int j = 0; j < groupList.length; j++) {
            for (int i = 0; i < userList.length; i++) {
                cmd = "add user " + userList[i] + " as member to group " + groupList[j];
                cmdList.add(cmd);
            }
        }

        return cmdList;
    }
}
