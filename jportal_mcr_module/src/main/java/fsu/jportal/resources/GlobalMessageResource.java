package fsu.jportal.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

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
import org.mycore.common.config.MCRConfiguration;

import com.google.gson.Gson;

@Path("globalMessage")
public class GlobalMessageResource {

    static Logger LOGGER = Logger.getLogger(GlobalMessageResource.class);

    private static JAXBContext JAXB_CONTEXT;
    private static java.nio.file.Path GLOBAL_MESSAGE_FILE;

    static {
        try {
            JAXB_CONTEXT = JAXBContext.newInstance(GlobalMessage.class);
        } catch(Exception exc) {
            LOGGER.error("Unable to create jaxb context", exc);
        }
        try {
            String mcrDataDir = MCRConfiguration.instance().getString("MCR.datadir");
            java.nio.file.Path confDir = Paths.get(mcrDataDir, "config");
            String confFileName = "jp-globalmessage.xml";
            GLOBAL_MESSAGE_FILE = confDir.resolve(confFileName);
            
            if(!Files.exists(confDir)){
            	Files.createDirectories(confDir);
            	
            	InputStream origMsgFile = GlobalMessageResource.class.getResourceAsStream("/META-INF/resources/config/" + confFileName);
            	Files.copy(origMsgFile, GLOBAL_MESSAGE_FILE);
            }
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
        	OutputStream msgOutputStream;
					try {
						msgOutputStream = Files.newOutputStream(GLOBAL_MESSAGE_FILE);
						m.marshal(msg, msgOutputStream);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
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
