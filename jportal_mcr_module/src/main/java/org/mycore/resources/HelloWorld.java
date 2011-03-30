package org.mycore.resources;

import java.io.IOException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import fsu.thulb.jaxb.JaxbTools;
import fsu.thulb.jp.datamodel.common.MCRObject;

@Path("/helloworld")
public class HelloWorld {
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(@Context HttpHeaders headers) throws JAXBException, ProtocolException, IOException{
        Set<Entry<String, List<String>>> entrySet = headers.getRequestHeaders().entrySet();
        String hello = "Hello World!\n";
        StringBuffer strBuff = new StringBuffer(hello);
        for (Entry<String, List<String>> entry : entrySet) {
            strBuff.append(entry.getKey() + ": " + entry.getValue() + "\n");
        }
        
        MCRObject storageContentList = JaxbTools.unmarschall(new URL("http://localhost:8080/VD17-webapp/jersey/storage/participant/mcrid/jportal_person_00000002"), MCRObject.class);
        JaxbTools.marschall(storageContentList, System.out);
        
        return strBuff.toString();
    }
}
