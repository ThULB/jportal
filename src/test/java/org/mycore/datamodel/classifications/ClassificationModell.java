package org.mycore.datamodel.classifications;

import java.util.LinkedList;
import java.util.List;

import org.jdom.Element;
import org.mycore.datamodel.classifications2.MCRCategLinkService;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRLabel;

public class ClassificationModell {

    private MCRCategoryDAO categoryDAO;
    private MCRCategLinkService linkService;

    public ClassificationModell(MCRCategoryDAO categoryDAO, MCRCategLinkService linkService) {
        this.categoryDAO = categoryDAO;
        this.linkService = linkService;
    }

    public Element getClassifications() {
        LinkedList<Element> elementList = createCategoryElementList(categoryDAO.getRootCategories());
        
        Element root = new Element("categories");
        root.addContent(elementList);
        
        return root;
    }

    private LinkedList<Element> createCategoryElementList(List<MCRCategory> categories) {
        LinkedList<Element> elementList = new LinkedList<Element>();
        for (MCRCategory mcrCategory : categories) {
            Element categTag = createCategoryElementWithCurrentLabel(mcrCategory);
            
            elementList.add(categTag);
        }
        return elementList;
    }

    /**
     * Creates the xml tag from a MCRCategory
     * @param mcrCategory
     * @return
     * Jdom Element of the following form
     * <p><blockquote><pre>
     * &lt;category rootID="rootID0" id="">
     *  &lt;label lang="de" text="rootLabel0" description="rootLabel Descr0" />
     * &lt;/category>
     * </pre></blockquote></p>
     */
    private Element createCategoryElementWithCurrentLabel(MCRCategory mcrCategory) {
        
        MCRLabel currentLabel = mcrCategory.getCurrentLabel();
        Element labelTag = createLabelElement(currentLabel);
        
        Element categTag = new Element("category");
        categTag.setAttribute("rootID", mcrCategory.getId().getRootID());
        categTag.setAttribute("id", mcrCategory.getId().getID());
        
        categTag.addContent(labelTag);
        return categTag;
    }

    /**
     * Creates the xml tag from a MCRLabel
     * 
     * @param label
     * @return
     * Jdom Element of the following form
     * <p><blockquote><pre>
     * &lt;label lang="de" text="rootLabel0" description="rootLabel Descr0" />
     * </pre></blockquote></p>
     */
    private Element createLabelElement(MCRLabel label) {
        Element labelTag = new Element("label");
        labelTag.setAttribute("lang", label.getLang());
        labelTag.setAttribute("text", label.getText());
        labelTag.setAttribute("description", label.getDescription());
        return labelTag;
    }

    
}
