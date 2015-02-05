package fsu.jportal.resources;

import org.apache.log4j.Logger;
import org.mycore.access.MCRAccessManager;
import org.mycore.frontend.jersey.filter.access.MCRResourceAccessChecker;

import com.sun.jersey.spi.container.ContainerRequest;

public class DerivateBrowserPermission implements MCRResourceAccessChecker {

    private static Logger LOGGER = Logger.getLogger(DerivateBrowserPermission.class);

    @Override
    public boolean isPermitted(ContainerRequest request) {
        String method = request.getMethod();
        String path = request.getPath();
        
        if (method.equals("GET")){
            //load derivate-browser
            if (path.equals("derivatebrowser/start") && !MCRAccessManager.getAccessImpl().checkPermission("administrate-jportal")) {
                LOGGER.info("Permission denied on Derivate Browser");
                return false;
            }            
        }
        
        if (method.equals("DELETE")){
            //delete files in derivate
            if (path.equals("derivatebrowser/multiple") && !MCRAccessManager.getAccessImpl().checkPermission("delete-derivate")){
                LOGGER.info("Permission denied to delete derivate files");
                return false;
            }
            else{
                //delete derivate
                if (path.contains("derivate") && !path.contains("multiple") && !MCRAccessManager.getAccessImpl().checkPermission("delete-derivate")) {
                    LOGGER.info("Permission denied to delete derivate");
                    return false;
                }
                //delete journal
                if (path.contains("jpjournal") && !MCRAccessManager.getAccessImpl().checkPermission("delete-jpjournal")) {
                    LOGGER.info("Permission denied to delete journal");
                    return false;
                }
                //delete article
                if (path.contains("jparticle") && !MCRAccessManager.getAccessImpl().checkPermission("delete-jparticle")) {
                    LOGGER.info("Permission denied to delete article");
                    return false;
                }
                //delete volume
                if (path.contains("jpvolume") && !MCRAccessManager.getAccessImpl().checkPermission("delete-jpvolume")) {
                    LOGGER.info("Permission denied to delete volume");
                    return false;
                }
            }
        }
        
        if (method.equals("POST")){
            //rename derivate file
            if (path.equals("derivatebrowser/rename") && !MCRAccessManager.getAccessImpl().checkPermission("update-derivate")) {
                LOGGER.info("Permission denied to rename derivate files");
                return false;
            }
            //move derivate files
            if (path.equals("derivatebrowser/moveDeriFiles") && !MCRAccessManager.getAccessImpl().checkPermission("update-derivate")) {
                LOGGER.info("Permission denied to move derivate files");
                return false;
            }
            //check if files already exist
            if (path.equals("derivatebrowser/exists") && !MCRAccessManager.getAccessImpl().checkPermission("update-derivate")) {
                LOGGER.info("Permission denied to check if files already exist");
                return false;
            }
            //upload files to derivate / create derivate
            if (path.equals("derivatebrowser/upload") && !MCRAccessManager.getAccessImpl().checkPermission("create-derivate")) {
                LOGGER.info("Permission denied to upload files");
                return false;
            }
            //add URN to derivate
            if (path.equals("derivatebrowser/addURN") && !MCRAccessManager.getAccessImpl().checkPermission("update-derivate")) {
                LOGGER.info("Permission denied to add URN to derivate");
                return false;
            }
            //create folder in derivate
            if (path.contains("derivate") && !path.contains("rename") && !path.contains("moveDeriFiles") && !path.contains("exists") && !path.contains("upload") && !path.contains("addURN") && !MCRAccessManager.getAccessImpl().checkPermission("create-derivate")) {
                LOGGER.info("Permission denied to create derivate folder");
                return false;
            }
        }
        
        if (method.equals("PUT")){
            //move documents
            if (path.equals("derivatebrowser/moveDocs") && !MCRAccessManager.getAccessImpl().checkPermission("move-objects")) {
                LOGGER.info("Permission denied to move documents");
                return false;
            }
            //change mainfile
            if (path.contains("derivate") && !path.contains("moveDocs") && !MCRAccessManager.getAccessImpl().checkPermission("update-derivate")) {
                LOGGER.info("Permission denied change mainfile");
                return false;
            }
        }
        
        return true;
    }
}
