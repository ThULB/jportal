package fsu.jportal.resolver;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.output.DOMOutputter;
import org.jdom2.transform.JDOMSource;

import fsu.jportal.xml.XMLTools;

public class EditorPreProc implements URIResolver{

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        String fileName = getFileName(href);
        ParamsXML params = getParams(href);
        return new JDOMSource(exec(fileName, params));
    }
 
    public ParamsXML getParams(String href) {
        Matcher matcher = getMatcher(href, "[\\?|&]([\\w]+)=([\\w\\:/\\. ]+)");
        ParamsXML params = new ParamsXML();
        while(matcher.find()){
            params.put(matcher.group(1), matcher.group(2));
        }
        
        return params;        
    }

    private Matcher getMatcher(String str, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher;
    }

    private String getFileName(String href) {
        Matcher matcher = getMatcher(href, "\\:([\\w\\W]+)\\?");
        if(matcher.find()){
            return matcher.group(1);
        }
        
        return null;
    }

    public Document exec(String fileName, ParamsXML paramsXML) {
        String xmlSource = "/editor/xml/" + fileName + ".xml";
        String xslSource = "/editor/xsl/" + fileName + ".xsl";
        
        Map<String, Object> params = new HashMap<String, Object>();
        try {
            params.put("paramsXML", paramsXML.getDocument());
            return new XMLTools().transform(xmlSource, xslSource, params);
        } catch (TransformerFactoryConfigurationError | TransformerException | JDOMException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static class ParamsXML{
        private Element paramsElem;
        private StringBuilder paramStr;

        public ParamsXML() {
            paramsElem = new Element("params");
            paramStr = new StringBuilder();
        }
        
        public void put(String name, String value){
            Element paramElem = new Element("param");
            paramElem.setAttribute("name", name);
            paramElem.setAttribute("value", value);
            paramsElem.addContent(paramElem);
            
            if(paramStr.length() > 0){
                paramStr.append("&");
            }
            paramStr.append(name + "=" + value);
        }
        
        public org.w3c.dom.Document getDocument() throws JDOMException{
            DOMOutputter domOutputter = new DOMOutputter();
            return domOutputter.output(new Document(paramsElem));
        }
        
        public StringBuilder getParamStr() {
            if(paramStr.length() > 0){
                paramStr.insert(0, "?");
            }
            return paramStr;
        }
    }

}