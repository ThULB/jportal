package fsu.jportal.xml;

import java.io.FileInputStream;

import org.jdom.Document;
import org.mycore.common.xml.MCRXMLParserFactory;
import org.mycore.datamodel.ifs2.MCRContent;
import org.xml.sax.SAXParseException;

public class XMLTools {

    public static Document readXMLFromIS(FileInputStream in) throws SAXParseException {
        return MCRXMLParserFactory.getNonValidatingParser().parseXML(MCRContent.readFrom(in));
    }

}
