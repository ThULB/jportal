package fsu.jportal.resources;

import com.google.gson.Gson;
import fsu.jportal.frontend.GlobalMessageFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import java.io.IOException;
import java.io.OutputStream;

@Path("globalMessage")
public class GlobalMessageResource {

    static Logger LOGGER = LogManager.getLogger(GlobalMessageResource.class);

    static JAXBContext JAXB_CONTEXT;

    static {
        try {
            JAXB_CONTEXT = JAXBContext.newInstance(GlobalMessage.class);
        } catch (Exception exc) {
            LOGGER.error("Unable to create jaxb context", exc);
        }
    }

    @POST
    @Path("save")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response save(String json) throws JAXBException {
        Gson gson = new Gson();
        GlobalMessage msg = gson.fromJson(json, GlobalMessage.class);
        Marshaller m = JAXB_CONTEXT.createMarshaller();
        synchronized (m) {
            OutputStream msgOutputStream;
            try {
                msgOutputStream = GlobalMessageFile.getOutputStream();
                m.marshal(msg, msgOutputStream);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return Response.ok().build();
    }

    @XmlRootElement(name = "globalmessage")
    static class GlobalMessage {
        @XmlElement
        public String visibility;

        @XmlElement
        public String head;

        @XmlElement
        public String message;
    }
}
