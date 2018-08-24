package fsu.jportal.resources;

import java.util.Optional;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mets.model.Mets;

import fsu.jportal.backend.JPDerivateComponent;
import fsu.jportal.backend.JPObjectType;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.mets.DfgViewerMetsGenerator;
import fsu.jportal.util.JPComponentUtil;

@Path("mets/dfg")
public class DfgViewerMetsResource {

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("{id}")
    public Response get(@PathParam("id") String id) {
        if (!MCRObjectID.isValid(id)) {
            throw new BadRequestException(id + " is not a valid mycore identifier.");
        }
        MCRObjectID mcrObjectID = MCRObjectID.getInstance(id);
        if (!(JPComponentUtil.is(mcrObjectID, JPObjectType.jparticle)
            || JPComponentUtil.is(mcrObjectID, JPObjectType.jpvolume)
            || JPComponentUtil.is(mcrObjectID, JPObjectType.jpjournal))) {
            throw new BadRequestException(
                "Invalid identifier " + id + ". Type has to be jparticle, jpvolume or jpjournal.");
        }
        Optional<JPDerivateComponent> derivateOptional = JPComponentUtil.getPeriodical(mcrObjectID)
            .flatMap(JPPeriodicalComponent::getFirstDerivate);
        if (!derivateOptional.isPresent()) {
            throw new BadRequestException(
                "Object " + id + " does not contain any derivates.");
        }
        DfgViewerMetsGenerator metsGenerator = new DfgViewerMetsGenerator();
        metsGenerator.setup(derivateOptional.get().getId().toString());
        Mets mets = metsGenerator.generate();
        Document xml = mets.asDocument();
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        return Response.ok(out.outputString(xml), MediaType.APPLICATION_XML).build();
    }

}
