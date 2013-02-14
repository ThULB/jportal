package fsu.jportal.resolver;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;

import fsu.jportal.xml.XMLContentTools;

public class ParentsResolver implements URIResolver {

	@Override
	public Source resolve(String href, String base) throws TransformerException {
		String childID = href.replaceAll("parents:", "");
		Element parents = new XMLContentTools().getParents(childID);
		return new JDOMSource(parents);
	}

}
