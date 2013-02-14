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
import org.jdom2.Content;
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.common.MCRConfiguration;

@Path("journalFile")
public class JournalFileResource {
    static Logger logger = Logger.getLogger(JournalFileResource.class);

    @POST
    @Path("{id}/{filename}")
    @Consumes(MediaType.APPLICATION_XHTML_XML)
    public Response postAddFile(@PathParam("id") String journalID, @PathParam("filename") String filename, String fileContent) throws JDOMException, IOException {
        String journalFileFolderPath = MCRConfiguration.instance().getString("JournalFileFolder");
        File journalFileDir = new File(journalFileFolderPath + File.separator + journalID);
        
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
        List<Content> content = stringAsDoc.getRootElement().getContent();
        Iterator<Content> iterator = content.iterator();
        while (iterator.hasNext()) {
            Content object = iterator.next();
            iterator.remove();
            element.addContent(object);
        }
    }

}
