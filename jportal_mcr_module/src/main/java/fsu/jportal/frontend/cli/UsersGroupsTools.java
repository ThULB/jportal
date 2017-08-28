package fsu.jportal.frontend.cli;

import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;

import java.util.ArrayList;
import java.util.List;

@MCRCommandGroup(name = "User Group Tools")
public class UsersGroupsTools {

    @MCRCommand(syntax = "add {0} to {1}", help = "add [userIDs] to [groupIDs]\n usersIDs: user1,user2,...\n groupIDs: group1, group2,...")
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
