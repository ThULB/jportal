package fsu.jportal.resources;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Text;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.datamodel.ifs.MCRFile;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.jersey.MCRJerseyUtil;

@Path("mets/import")
public class MetsImporterResource {

    @GET
    @Path("{derivateId}")
    public Response importFromDerivate(@PathParam("derivateId") String derivateId) throws Exception {
        MCRObjectID mcrDerivateId = MCRJerseyUtil.getID(derivateId);
        MCRDerivate derivate = MCRMetadataManager.retrieveMCRDerivate(mcrDerivateId);
        MCRFilesystemNode rootNode = MCRFilesystemNode.getRootNode(mcrDerivateId.toString());
        if (rootNode == null) {
            return Response.status(Status.NOT_FOUND).entity("root node of derivate not found").build();
        }
        MCRFilesystemNode metsNode = rootNode.getRootDirectory().getChildByPath("/mets.xml");
        if (metsNode != null) {
            importMets((MCRFile) metsNode, derivate);
            return Response.ok().build();
        }
        return Response.status(Status.NOT_FOUND).entity("derivate has no mets.xml").build();
    }

    protected void importMets(MCRFile file, MCRDerivate derivate) throws JDOMException, IOException {
        Document xml = file.getContentAsJDOM();
        Element mets = xml.getRootElement();
        String creatorSoftware = getCreatorSoftware(mets);
        if (creatorSoftware.equals("DEA METS Engine")) {
            new InnsbruckMetsImporter(xml, derivate).importMets();
        } else if (creatorSoftware.startsWith("AAS_ImageCutting_")) {

        } else {
            throw new WebApplicationException(Response.status(Status.NOT_FOUND)
                .entity("Unknown mets.xml format. Cannot import.").build());
        }
    }

    protected String getCreatorSoftware(Element mets) {
        XPathExpression<Text> creatorSoftwareExp = XPathFactory.instance().compile(
            "mets:metsHdr/mets:agent[@ROLE='CREATOR' and @OTHERTYPE='SOFTWARE']/mets:name/text()", Filters.text(),
            null, MetsImporterBase.getNameSpaceList());
        Text creatorSoftware = creatorSoftwareExp.evaluateFirst(mets);
        return creatorSoftware != null ? creatorSoftware.getText() : "";
    }
}
