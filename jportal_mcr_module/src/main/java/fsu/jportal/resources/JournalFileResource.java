package fsu.jportal.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.jdom.Content;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.mycore.common.MCRConfiguration;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Path("journaFile")
public class JournalFileResource {
    static Logger logger = Logger.getLogger(JournalFileResource.class);
    
    
    @POST
    @Path("{id}/{filename}")
    @Consumes(MediaType.APPLICATION_XHTML_XML)
    public Response postAddFile(@PathParam("id") String journalID, @PathParam("filename") String filename, String fileContent) throws JDOMException, IOException {
        String journalFileFolderPath = MCRConfiguration.instance().getString("JournalFileFolder");
        File journalFileDir = new File(journalFileFolderPath + File.separator + journalID);
        Element elem = new Element("foo");
        
        if(!journalFileDir.exists()){
            journalFileDir.mkdirs();
        }
        
        String rootName = "MyCoReWebPage";
        Element root = new Element(rootName);
        Element section = new Element("section");
        section.setAttribute("lang", "de", Namespace.XML_NAMESPACE);
        section.setAttribute("title", journalID);


        root.addContent(section);
        Document document = new Document(root);
        DocType doctype = new DocType(rootName);
        document.setDocType(doctype);
        
        addStringContent(section, fileContent);
        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());

        xmlOutputter.output(document, new FileOutputStream(journalFileFolderPath + File.separator + journalID + File.separator + "intro.xml"));
        
        return Response.created(URI.create("../")).build();
    }
    
    private void addStringContent(Element element, String s) throws JDOMException, IOException {
        s = "<tmp>" + s + "</tmp>";
        SAXBuilder saxBuilder = new SAXBuilder();
        Reader stringReader = new StringReader(s);
        Document stringAsDoc = saxBuilder.build(stringReader);
        List content = stringAsDoc.getRootElement().getContent();
        Iterator iterator = content.iterator();
        while (iterator.hasNext()) {
            Content object = (Content) iterator.next();
            iterator.remove();
            element.addContent(object);
        }
    }
}
