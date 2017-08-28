package org.mycore.datamodel.metadata;

import com.google.gson.JsonObject;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.mycore.common.MCRException;
import org.mycore.common.xml.MCRXMLHelper;

public class MCRMetaElementXML extends MCRMetaDefault {

    protected Element element;

    @Override
    public void setFromDOM(Element element) throws MCRException {
        super.setFromDOM(element);
        this.element = element;
    }

    @Override
    public Element createXML() throws MCRException {
        if (!isValid()) {
            debug();
            throw new MCRException("The content of MCRMetaDefault is not valid.");
        }
        Element clone = this.element != null ? this.element.clone().setName(subtag) : new Element(subtag);
        if (getLang() != null && getLang().length() > 0)
            clone.setAttribute("lang", getLang(), Namespace.XML_NAMESPACE);
        if (getType() != null && getType().length() > 0) {
            clone.setAttribute("type", getType());
        }
        clone.setAttribute("inherited", Integer.toString(getInherited()));
        return clone;
    }

    @Override
    public JsonObject createJSON() {
        JsonObject obj = MCRXMLHelper.jsonSerialize(this.element);
        if (getLang() != null) {
            obj.addProperty("lang", getLang());
        }
        if (getType() != null) {
            obj.addProperty("type", getType());
        }
        obj.addProperty("inherited", getInherited());
        return obj;
    }

    @Override
    public MCRMetaInterface clone() {
        MCRMetaElementXML clone = new MCRMetaElementXML();
        clone.setFromDOM(this.element.clone());
        return clone;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        final MCRMetaElementXML other = (MCRMetaElementXML) obj;
        return MCRXMLHelper.deepEqual(this.element, other.element);
    }

    @Override
    public boolean isValid() {
        return super.isValid();
    }

}
