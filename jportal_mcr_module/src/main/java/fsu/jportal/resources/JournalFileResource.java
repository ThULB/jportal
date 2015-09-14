package fsu.jportal.resources;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Content;
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.content.MCRPathContent;
import org.mycore.common.content.util.MCRServletContentHelper;

@Path("journalFile/{id}")
public class JournalFileResource {
    static Logger LOGGER = LogManager.getLogger(JournalFileResource.class);

    @PathParam("id")
    String journalID;

    @Context
    ServletContext context;

    @Context
    HttpServletRequest request;

    @Context
    HttpServletResponse response;

    private String journalFileFolderPath = MCRConfiguration.instance().getString("JournalFileFolder");

    @POST
    @Path("{filename}")
    @Consumes(MediaType.APPLICATION_XHTML_XML)
    public Response postAddFile(@PathParam("filename") String filename, String fileContent) throws JDOMException,
        IOException {
    		if(!fileContent.equals("")) {
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
	        try {
	            java.nio.file.Path introXMl = getJournalFileFolderPath().resolve("intro.xml");
	            BufferedWriter introWriter = Files.newBufferedWriter(introXMl, StandardCharsets.UTF_8);
	            xmlOutputter.output(document, introWriter);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
    		} else {
    			deleteFile();
    		}
    		return Response.created(URI.create("../")).build();
    }
    
    private void deleteFile() throws IOException{
    	java.nio.file.Path journalFolder = null;
    	java.nio.file.Path introXMl = null;
    	try {
    		introXMl = getJournalFileFolderPath().resolve("intro.xml");
    		journalFolder = getJournalFileFolderPath();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	Files.deleteIfExists(introXMl);
    	Files.deleteIfExists(journalFolder);
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

    class FileStreamingOutput implements StreamingOutput {
        private java.nio.file.Path path;

        public FileStreamingOutput(java.nio.file.Path path) {
            this.path = path;
        }

        @Override
        public void write(OutputStream os) throws IOException, WebApplicationException {
            Files.copy(path, os);
        }

    }

    @GET
    @Path("web/{path:.*}")
    public Response getResources(@PathParam("path") String path) {
        try {
            java.nio.file.Path file = getJournalFileFolderPath().resolve("web").resolve(path);
            MCRServletContentHelper.serveContent(new MCRPathContent(file), request, response, context);
            return Response.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    public java.nio.file.Path getJournalFileFolderPath() throws Exception {
        java.nio.file.Path journalFileFolder = Paths.get(URI.create("file://" + journalFileFolderPath));
        journalFileFolder = journalFileFolder.resolve(journalID);

        if (!Files.exists(journalFileFolder)) {
            try {
                Files.createDirectories(journalFileFolder);
            } catch (IOException e) {
                LOGGER.error("Unable to create directory " + journalFileFolder.toAbsolutePath().toString(), e);
            }
        }

        if (!Files.isDirectory(journalFileFolder)) {
            throw new Exception(journalFileFolder.toString() + " is not a Directory.");
        }

        return journalFileFolder;
    }
}
