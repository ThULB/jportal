package fsu.jportal.resources;

import java.io.File;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;
import org.mycore.common.MCRConfiguration;

import com.google.gson.Gson;

@Path("globalMessage")
public class GlobalMessageResource {

    static Logger LOGGER = Logger.getLogger(GlobalMessageResource.class);

    private static JAXBContext JAXB_CONTEXT;
    private static File GLOBAL_MESSAGE_FILE;

    static {
        try {
            JAXB_CONTEXT = JAXBContext.newInstance(GlobalMessage.class);
        } catch(Exception exc) {
            LOGGER.error("Unable to create jaxb context", exc);
        }
        try {
            String webappDir = MCRConfiguration.instance().getString("MCR.WebApplication.basedir");
            GLOBAL_MESSAGE_FILE = new File(webappDir, "config/jp-globalmessage.xml");
        } catch(Exception exc) {
            LOGGER.error("Unable to get globalmessage file", exc);
        }
    }

    @POST
    @Path("save")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response save(String json) throws JAXBException {
        Gson gson = new Gson();
        GlobalMessage msg = gson.fromJson(json, GlobalMessage.class);
        Marshaller m = JAXB_CONTEXT.createMarshaller();
        synchronized(m) {
            m.marshal(msg, GLOBAL_MESSAGE_FILE);
        }
        return Response.ok().build();
    }

    @XmlRootElement(name = "globalmessage")
    private static class GlobalMessage {
        @XmlElement
        public String visibility;
        @XmlElement
        public String head;
        @XmlElement
        public String message;
    }
}
