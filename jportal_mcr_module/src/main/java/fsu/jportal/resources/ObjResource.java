package fsu.jportal.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFile;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.iview2.services.MCRIView2Tools;
import org.mycore.iview2.services.MCRImageTiler;
import org.mycore.iview2.services.MCRTileJob;
import org.mycore.iview2.services.MCRTilingQueue;
import org.xml.sax.SAXException;

@Path("obj")
public class ObjResource {
    
    private static final Logger LOGGER = Logger.getLogger(ObjResource.class);

    @POST
    @Path("{id}/mergeDeriv")
    public Response mergeDerivates(@PathParam("id") String objID){
        MCRObjectID mcrObjectID = MCRObjectID.getInstance(objID);
        try {
            Document objXML = MCRXMLMetadataManager.instance().retrieveXML(mcrObjectID);
            if(objXML == null){
                return Response.status(Status.NOT_FOUND).build();
            }
            
            XPathExpression<Attribute> derivIDsXpath = XPathFactory.instance().compile("/mycoreobject/structure/derobjects/derobject/@xlink:href",Filters.attribute(), null, MCRConstants.XLINK_NAMESPACE);
            List<Attribute> hrefAttributes = derivIDsXpath.evaluate(objXML);
            
            if (hrefAttributes.size() > 1) {
                String destDerivID = hrefAttributes.get(0).getValue();
                MCRDirectory destDeriv = (MCRDirectory) MCRFilesystemNode.getRootNode(destDerivID);
                
                List<Attribute> toMergeDerivIDs = hrefAttributes.subList(1, hrefAttributes.size());
                for (Attribute mergeID : toMergeDerivIDs) {
                    String currentDerivID = mergeID.getValue();
                    MCRDirectory derivate = (MCRDirectory) MCRFilesystemNode.getRootNode(currentDerivID);
                    for (MCRFilesystemNode child : derivate.getChildren()) {
                        child.move(destDeriv);
                    }
                    MCRMetadataManager.deleteMCRDerivate(MCRObjectID.getInstance(currentDerivID));
                }
                createTileforDeriv(destDerivID);
            }
        } catch (IOException | JDOMException | SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        } catch (MCRPersistenceException e) {
            e.printStackTrace();
            return Response.status(Status.FORBIDDEN).build();
        }
        
        return Response.status(Status.OK).build();
    }
    
    private void createTileforDeriv(String derivateID){
        if (!MCRIView2Tools.isDerivateSupported(derivateID)) {
            LOGGER.info("Skipping tiling of derivate " + derivateID + " as it's main file is not supported by IView2.");
        }
        List<String> returns = new ArrayList<String>();
        MCRDirectory derivate = null;

        MCRFilesystemNode node = MCRFilesystemNode.getRootNode(derivateID);

        if (node == null || !(node instanceof MCRDirectory))
            throw new MCRException("Derivate " + derivateID + " does not exist or is not a directory!");
        derivate = (MCRDirectory) node;

        List<MCRFile> supportedFiles = getSupportedFiles(derivate);
        for (MCRFile image : supportedFiles) {
            tileImage(image);
        }
    }
    
    public void tileImage(MCRFile file) {
        MCRTilingQueue TILE_QUEUE = MCRTilingQueue.getInstance();
        
        if (MCRIView2Tools.isFileSupported(file)) {
            MCRTileJob job = new MCRTileJob();
            job.setDerivate(file.getOwnerID());
            job.setPath(file.getAbsolutePath());
            TILE_QUEUE.offer(job);
            LOGGER.info("Added to TilingQueue: " + file.getID() + " " + file.getAbsolutePath());
            startMasterTilingThread();
        }
    }
    
    private void startMasterTilingThread() {
        if (!MCRImageTiler.isRunning()) {
            LOGGER.info("Starting Tiling thread.");
            final Thread tiling = new Thread(MCRImageTiler.getInstance());
            tiling.start();
        }
    }
    
    private List<MCRFile> getSupportedFiles(MCRDirectory rootNode) {
        ArrayList<MCRFile> files = new ArrayList<MCRFile>();
        MCRFilesystemNode[] nodes = rootNode.getChildren();
        for (MCRFilesystemNode node : nodes) {
            if (node instanceof MCRDirectory) {
                MCRDirectory dir = (MCRDirectory) node;
                files.addAll(getSupportedFiles(dir));
            } else {
                MCRFile file = (MCRFile) node;
                if (MCRIView2Tools.isFileSupported(file)) {
                    files.add(file);
                }
            }
        }
        return files;
    }
    
    private List<String> getDerivIDs(Document objXML) {
        XPathExpression<Attribute> derivIDsXpath = XPathFactory.instance().compile("/mycoreobject/structure/derobjects/derobject/@xlink:href",Filters.attribute(), null, MCRConstants.XLINK_NAMESPACE);
        List<Attribute> hrefAttributes = derivIDsXpath.evaluate(objXML);
        
        ArrayList<String> derivIDs = new ArrayList<String>();
        for (Attribute href : hrefAttributes) {
            derivIDs.add(href.getValue());
        }
        return derivIDs;
    }
}
