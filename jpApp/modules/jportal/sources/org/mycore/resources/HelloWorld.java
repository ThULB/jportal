package org.mycore.resources;

import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

@Path("/helloworld")
public class HelloWorld {
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(@Context HttpHeaders headers){
        Set<Entry<String, List<String>>> entrySet = headers.getRequestHeaders().entrySet();
        String hello = "Hello World!\n";
        StringBuffer strBuff = new StringBuffer(hello);
        for (Entry<String, List<String>> entry : entrySet) {
            strBuff.append(entry.getKey() + ": " + entry.getValue() + "\n");
        }
        
        return strBuff.toString();
    }
}
