package fsu.xml;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

public class ClathPathResourceResolver implements LSResourceResolver {

    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
        LSInput input = new ClassPathInput(getClass().getResourceAsStream("/" + systemId));
        return input;
    }

}