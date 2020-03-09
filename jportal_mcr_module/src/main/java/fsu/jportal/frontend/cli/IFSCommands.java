package fsu.jportal.frontend.cli;

import java.lang.reflect.Field;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;

/**
 * Created by chi on 06.03.20
 *
 * @author Huu Chi Vu
 */
@MCRCommandGroup(name = "JP IFS Commands")
public class IFSCommands {
    private static final Logger LOGGER = LogManager.getLogger();

    @MCRCommand(syntax = "jp delete ifs node {0}", help = "deletes ifs node {0} recursivly, even parent not exist.")
    public static void deleteIFSNode(String nodeID) {
        MCRFilesystemNode node = MCRFilesystemNode.getNode(nodeID);
        if (node == null) {
            LOGGER.warn("IFS Node {} does not exist.", nodeID);
            return;
        }
        MCRDirectory parent = node.getParent();
        if (node.hasParent() && parent == null) {
            LOGGER.info("Deleting IFS Node with missing parent {}: {} parentID {} name {}",
                    nodeID, node.getOwnerID(), node.getParentID(), node.getName());
            try {
                Field parentID = node.getClass().getSuperclass().getDeclaredField("parentID");
                parentID.setAccessible(true);
                parentID.set(node, null);
                LOGGER.info("Set parentID for IFS Node to null.");
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            LOGGER.info("Deleting IFS Node {}: {}{}", nodeID, node.getOwnerID(), node.getAbsolutePath());
        }
        node.delete();
    }
}
