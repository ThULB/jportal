package org.mycore.common.xml;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;

public class MCRJPortalURIGetAllClassIDs implements MCRURIResolver.MCRResolver {

    private static final Logger LOGGER = Logger.getLogger(MCRJPortalURIGetAllClassIDs.class);

    private String URI = "jportal_getALLClassIDs:";

    private Element returnXML = null;

    private List<MCRCategoryID> categoryRootIDs = null;
    
    private MCRCategoryDAO categDAO;

    public MCRJPortalURIGetAllClassIDs() {
        categDAO = MCRCategoryDAOFactory.getInstance();
    }
    
    public MCRJPortalURIGetAllClassIDs(MCRCategoryDAO dao) {
        categDAO = dao;
    }

    /**
     * 
     * Syntax: jportal_getAllClassIDs
     * 
     * @return 
     * <p><blockquote><pre>
     * &lt;dummyRoot> 
     *  &lt;list type="dropdown"> 
     *      &lt;item value="{classification id}"> 
     *          &lt;label xml:lang="de">{classification description}&lt;/label> 
     *      &lt;/item>
     *   &lt;/list>
     * &lt;/dummyRoot>
     * </pre></blockquote></p>
     */
    public Element resolveElement(String uri) {
        LOGGER.debug("start resolving " + uri);

        if (!wellURI(uri))
            throw new IllegalArgumentException("Invalid format of uri given to resolve " + URI + "=" + uri);

        List<MCRCategoryID> currentRootIds = categDAO.getRootCategoryIDs();
        // if the current categorys differs from the previous generated,
        // then create an updated xml tree
        if(returnXML == null ||
           categoryRootIDs == null ||
           currentRootIds.size() != categoryRootIDs.size() ||
           !currentRootIds.containsAll(categoryRootIDs)) {
            categoryRootIDs = currentRootIds;
            returnXML = createXML(categDAO, categoryRootIDs);
        }
        return returnXML;
    }

    private Element createXML(MCRCategoryDAO categDAO, List<MCRCategoryID> ids) {
        Element returnXML = new Element("dummyRoot");
        Iterator<MCRCategoryID> ci = ids.iterator();
        while (ci.hasNext()) {
            MCRCategoryID cid = (MCRCategoryID) ci.next();
            String classID = cid.getRootID();
            String descr = "";
            MCRCategory rootCat = categDAO.getRootCategory(cid, 0);
            if ((null != rootCat.getLabels()))
                descr = rootCat.getCurrentLabel().getText();
            Element item = new Element("item").setAttribute("value", classID);
            Element label = new Element("label").setAttribute("lang", "de", Namespace.XML_NAMESPACE);
            label.setText(descr + " (" + classID + ")");
            item.addContent(label);
            returnXML.addContent(item);
        }
        return returnXML;
    }

    private boolean wellURI(String uri) {
        if (uri.equals(URI))
            return true;
        else
            return false;
    }

}
