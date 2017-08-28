package fsu.jportal.resolver;

import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.access.MCRAccessManager;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

/**
 * Determines whether the current user has the permission to perform a certain action.
 * 
 * @author Matthias Eichner
 */
@URIResolverSchema(schema = "acl")
public class ACLResolver implements URIResolver {

    /**
     * <p>
     * acl:{id}:{permission}
     * </p>
     * @return &lt;access permission="true | false" /&gt;
     */
    @Override
    public Source resolve(String href, String base) throws TransformerException {
        String[] uriParts = href.split(":");
        if (uriParts.length < 3) {
            throw new IllegalArgumentException("Invalid format of uri given to resolve: " + href);
        }
        String id = uriParts[1];
        String permission = uriParts[2];
        boolean hasPermission = !"".equals(id) ? MCRAccessManager.checkPermission(id, permission) : MCRAccessManager
            .checkPermission(permission);
        return new JDOMSource(new Element("access").setAttribute("permission", String.valueOf(hasPermission)));
    }

}
