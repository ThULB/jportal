package fsu.jportal.xml;

import org.jdom2.Element;
import org.mycore.common.MCRConstants;

import fsu.jportal.util.ParentsList;

public class ParentsListXML implements ParentsList<Element> {
    Element parentsElement = new Element("parents");
    
    @Override
    public void addParent(String title, String id, String inherited){
        Element parentElement = new Element("parent");
        parentElement.setAttribute("inherited", inherited);
        parentElement.setAttribute("title", title, MCRConstants.XLINK_NAMESPACE);
        parentElement.setAttribute("href", id, MCRConstants.XLINK_NAMESPACE);
        getParents().addContent(0, parentElement);
    }
    
    @Override
    public Element getParents(){
        return parentsElement;
    }
}