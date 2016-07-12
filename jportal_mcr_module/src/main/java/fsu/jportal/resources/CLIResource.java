package fsu.jportal.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.frontend.cli.MCRCommandManager;
import org.mycore.frontend.jersey.MCRJerseyUtil;

/**
 * Simple CLI resource until the apache version on the
 * production system is updated.
 * <p>Invoke commands with ?command=</p>
 * Spaces have to be encoded with a plus sign.
 * 
 * @author Matthias Eichner
 */
@Path("cli")
public class CLIResource {

    private static final Logger LOGGER = LogManager.getLogger();

    @GET
    public Response run(@QueryParam("command") String command) {
        MCRJerseyUtil.checkPermission("use-webcli");
        if (command == null || command.trim().isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity("missing or invalid command parameter").build();
        }
        MCRCommandManager mgr = new MCRCommandManager();
        try {
            invoke(mgr, command);
        } catch (Exception exc) {
            LOGGER.error("while executing command " + command, exc);
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                           .entity("Internal server error " + exc.getMessage())
                           .build();
        }
        return Response.ok().entity("command successful").build();
    }

    public void invoke(MCRCommandManager mgr, String command) throws Exception {
        List<String> subCommands = mgr.invokeCommand(command);
        for (String subCommand : subCommands) {
            invoke(mgr, subCommand);
        }
    }

}
