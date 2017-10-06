package org.mycore.datamodel.metadata;

import com.google.gson.JsonObject;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.mycore.common.MCRException;
import org.mycore.common.xml.MCRXMLHelper;

/**
 * Simpler version of {@link MCRMetaXML} with support of a single xml element instead of multiple content.
 *
 * @author Matthias Eichner
 */
public class MCRMetaElementXML extends MCRMetaDefault {

    protected Element element;

    @Override
    public void setFromDOM(Element element) throws MCRException {
        super.setFromDOM(element);
        this.element = element;
    }

    @Override
    public Element createXML() throws MCRException {
        Element elm = super.createXML();
        for (Attribute attr : this.element.getAttributes()) {
            elm.setAttribute(attr.clone());
        }
        for (Content content : this.element.getContent()) {
            elm.addContent(content.clone());
        }
        return elm;
    }

    /**
     * Creates the JSON representation. Extends the {@link MCRMetaDefault#createJSON()} method
     * with the following data.
     *
     * <pre>
     *   {
     *     content: {
     *         ... properties of the parsed content ...
     *     }
     *   }
     * </pre>
     *
     * @see MCRXMLHelper#jsonSerialize(Element)
     */
    @Override
    public JsonObject createJSON() {
        JsonObject json = super.createJSON();
        json.add("content", MCRXMLHelper.jsonSerialize(this.element));
        return json;
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

}
