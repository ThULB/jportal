package fsu.jportal.resolver;

import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.transform.JDOMSource;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;

import fsu.jportal.annotation.URIResolverSchema;

/**
 * <p><blockquote><pre>
 * &lt;dummyRoot> 
 *    &lt;option value="{classification id}"> 
 *          &lt;label xml:lang="de">{classification description}&lt;/label> 
 *    &lt;/option>
 * &lt;/dummyRoot>
 * </pre></blockquote></p>
 */
@URIResolverSchema(schema = "xClassifications")
public class ClassificationsResolver implements URIResolver {

    private Element returnXML = null;

    private List<MCRCategoryID> categoryRootIDs = null;

    @Override
    public synchronized Source resolve(String href, String base) throws TransformerException {
        List<MCRCategoryID> currentRootIds = MCRCategoryDAOFactory.getInstance().getRootCategoryIDs();
        // if the current categorys differs from the previous generated,
        // then create an updated xml tree
        if (returnXML == null || categoryRootIDs == null || currentRootIds.size() != categoryRootIDs.size()
            || !currentRootIds.containsAll(categoryRootIDs)) {
            categoryRootIDs = currentRootIds;
            returnXML = createXML(MCRCategoryDAOFactory.getInstance(), categoryRootIDs);
        }
        return new JDOMSource(returnXML);
    }

    private Element createXML(MCRCategoryDAO categDAO, List<MCRCategoryID> ids) {
        Element returnXML = new Element("dummyRoot");
        Iterator<MCRCategoryID> ci = ids.iterator();
        while (ci.hasNext()) {
            MCRCategoryID cid = (MCRCategoryID) ci.next();
            String classID = cid.getRootID();
            MCRCategory rootCat = categDAO.getRootCategory(cid, 0);
            Element item = new Element("option").setAttribute("value", classID);
            Element label = new Element("label").setAttribute("lang", "de", Namespace.XML_NAMESPACE);
            label.setText((rootCat.getLabels() != null ? rootCat.getCurrentLabel().getText() : "") + " (" + classID
                + ")");
            item.addContent(label);
            returnXML.addContent(item);
        }
        return returnXML;
    }

}
