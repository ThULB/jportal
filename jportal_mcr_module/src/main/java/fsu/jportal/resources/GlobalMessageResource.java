package fsu.jportal.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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

import com.google.gson.Gson;

@Path("globalMessage")
public class GlobalMessageResource {

    static Logger LOGGER = Logger.getLogger(GlobalMessageResource.class);

    static JAXBContext JAXB_CONTEXT;
    
    @POST
    @Path("save")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response save(String json) throws JAXBException {
        Gson gson = new Gson();
        GlobalMessage msg = gson.fromJson(json, GlobalMessage.class);
        Marshaller m = JAXB_CONTEXT.createMarshaller();
        synchronized(m) {
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
