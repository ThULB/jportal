package fsu.jportal.xml;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.jdom2.input.SAXBuilder;

/*
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE MyCoReWebPage>

<MyCoReWebPage>
  
  <section i18n="component.swf.page.commit.derivate.title" xml:lang="all">
    <editor id="editor-derivate">
      <source uri="{sourceUri}"/>
      <target type="servlet" name="UpdateDerivateServlet" method="post" format="xml"/>
      <cancel url="{cancelUrl}"/>
      
      <components root="editor-derivate" var="/mycorederivate" xmlns:xsd="http://www.w3.org/2001/XMLSchema"
        xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <include uri="webapp:editor/editor-derivate.xml"/>
      </components>
    </editor>
  </section>
  
</MyCoReWebPage>
*/

public class MCRWebpage {

    public final static String XML_ROOT = "MyCoReWebPage";

    protected List<Section> sectionList = new ArrayList<>();

    public MCRWebpage addSection(Section section) {
        this.sectionList.add(section);
        return this;
    }

    public Element toXML() {
        Element webpage = new Element(XML_ROOT);
        for (Section section : sectionList) {
            webpage.addContent(section.toXML());
        }
        return webpage;
    }

    public static class Section {
        public final static String XML_SECTION = "section";

        protected String i18n;

        protected String lang = "all";

        protected List<Element> contentList = new ArrayList<>();

        protected Text text;

        public String getI18n() {
            return i18n;
        }

        public String getLang() {
            return lang;
        }

        public Text getText() {
            return text;
        }

        public Section setI18n(String i18n) {
            this.i18n = i18n;
            return this;
        }

        public Section setLang(String lang) {
            this.lang = lang;
            return this;
        }

        public Section setText(String text) {
            this.text = new Text(text);
            return this;
        }

        public Section addContent(String content)  throws IOException, JDOMException {
            SAXBuilder builder = new SAXBuilder();
            Reader reader = new StringReader(content);
            try {
                Document doc = builder.build(reader);
                return addContent(doc.getRootElement().detach());
            } catch (Exception exc) {
                // try to surround with div element (for text elements and multi elements)
                content = new StringBuilder("<div>").append(content).append("</div>").toString();
                reader = new StringReader(content);
                Document doc = builder.build(reader);
                return addContent(doc.getRootElement().detach());
            }
        }

        public Section addContent(Element content) {
            this.contentList.add(content);
            return this;
        }

        public Element toXML() {
            Element section = new Element(XML_SECTION);
            if (i18n != null) {
                section.setAttribute("i18n", getI18n());
            }
            if (lang != null) {
                section.setAttribute("lang", getLang(), Namespace.XML_NAMESPACE);
            }
            if (text != null) {
                section.setContent(getText());
            }
            for (Element content : contentList) {
                section.addContent(content);
            }
            return section;
        }
    }
}
