package fsu.jportal.resources;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jdom.Document;
import org.jdom.output.XMLOutputter;
import org.mycore.common.MCRConfiguration;

@Path("globalMessage")
public class GlobalMessageResource {

    @POST
    @Path("save")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response save(GlobalMessage globalMessage)
            throws IOException {
//        GlobalMessage gMsg = new GlobalMessage(visibility, head, message);
        String webappDir = MCRConfiguration.instance().getString("MCR.WebApplication.basedir");
//        saveToFile(globalMessage.toXML(), new File(webappDir, "config/jp-globalmessage.xml"));
        return Response.ok().build();
    }

    public void saveToFile(Document document, File file) throws IOException {
        XMLOutputter out = new XMLOutputter();
        java.io.FileWriter writer = new java.io.FileWriter(file);
        out.output(document, writer);
        writer.flush();
        writer.close();
    }

    @XmlRootElement(name="globalmessage")
    private static class GlobalMessage {

        @XmlElement
        public String visibility;
        @XmlElement
        public String head;
        @XmlElement
        public String message;

        public GlobalMessage(String visibility, String head, String msg) {
            this.visibility = visibility;
            this.head = head;
            this.message = msg;
        }

    }
}
