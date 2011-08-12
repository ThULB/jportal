package fsu.jportal.resources.filter;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

public class NullFilter extends ClientFilter {

    @Override
    public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
        System.out.println("Null Filter");
        
        return getNext().handle(cr);
    }

}
