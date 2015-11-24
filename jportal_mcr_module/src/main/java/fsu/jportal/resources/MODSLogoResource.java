package fsu.jportal.resources;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaInstitutionName;
import org.mycore.datamodel.metadata.MCRMetaInterface;
import org.mycore.datamodel.metadata.MCRMetaLink;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.servlets.MCRServlet;

import fsu.jportal.mods.MODSLogoEntity;
import fsu.jportal.xml.mapper.MODSLogoEntityXMLMapper;

@Path("modslogos")
public class MODSLogoResource {
    static Logger LOGGER = LogManager.getLogger(MODSLogoResource.class);

    private static enum ParticipantRoleTypes {
        operator, sponsor, partner;
        public static boolean equals(String type) {
            for (ParticipantRoleTypes participantType : ParticipantRoleTypes.values()) {
                if (type.equals(participantType.toString())) {
                    return true;
                }
            }
            return false;
        }
    }

    @GET
    @Path("{hiddenJournalId}")
    @Produces(MediaType.APPLICATION_XML)
    public String getLogos(@PathParam("hiddenJournalId") String hiddenJournalId) throws IOException {
        Document doc = get(hiddenJournalId);
        return xmlToString(doc);
    }

    public static Document get(String journalID) {
        LOGGER.info("MODS logo for " + journalID);
        List<MODSLogoEntity> entities = new ArrayList<MODSLogoEntity>();
        MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(journalID));
        MCRMetaElement participants = mcrObject.getMetadata().getMetadataElement("participants");
        if (participants != null) {
            for (MCRMetaInterface retrievedElem : participants) {
                if (retrievedElem instanceof MCRMetaLinkID) {
                    MCRMetaLinkID participant = (MCRMetaLinkID) retrievedElem;
                    String role = participant.getType();
                    if (role != null && ParticipantRoleTypes.equals(role)) {
                        String participantID = participant.getXLinkHref();
                        MCRObject participantMcrObj = MCRMetadataManager
                            .retrieveMCRObject(MCRObjectID.getInstance(participantID));
                        entities.add(createLogoEntity(role, participantMcrObj));
                    }
                }
            }
        }
        return new MODSLogoEntityXMLMapper(entities).getXML();
    }

    protected static MODSLogoEntity createLogoEntity(String role, MCRObject participantMcrObj) {
        MODSLogoEntity modsLogoEntity = new MODSLogoEntity();
        modsLogoEntity.setName(getFullname(participantMcrObj));
        modsLogoEntity.setRole(role);
        modsLogoEntity.setSiteURL(getSite(participantMcrObj));
        getLogoURL(participantMcrObj, modsLogoEntity);
        return modsLogoEntity;
    }

    protected String xmlToString(Document xml) throws IOException {
        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        Writer stringWriter = new StringWriter();
        xmlOutputter.output(xml, stringWriter);
        return stringWriter.toString();
    }

    protected static void getLogoURL(MCRObject participantMcrObj, MODSLogoEntity modsLogoEntity) {
        List<MCRMetaLinkID> derivates = participantMcrObj.getStructure().getDerivates();
        if (derivates == null || derivates.isEmpty()) {
            return;
        }
        // we assume that there is only one derivate
        MCRObjectID derivateId = derivates.get(0).getXLinkHrefID();
        MCRDerivate derivate = MCRMetadataManager.retrieveMCRDerivate(derivateId);
        String mainDoc = derivate.getDerivate().getInternals().getMainDoc();
        // TODO: we should check if its an image (iview api?)
        StringBuilder url = new StringBuilder(MCRServlet.getServletBaseURL()).append("MCRFileNodeServlet/")
            .append(derivateId.toString()).append("/").append(mainDoc);
        modsLogoEntity.setLogo(url.toString());
    }

    protected static String getFullname(MCRObject participantMcrObj) {
        MCRMetaElement names = participantMcrObj.getMetadata().getMetadataElement("names");
        if (names != null) {
            for (MCRMetaInterface nameElem : names) {
                if (nameElem instanceof MCRMetaInstitutionName) {
                    MCRMetaInstitutionName name = (MCRMetaInstitutionName) nameElem;
                    return name.getFullName();
                }
            }
        }
        return "No Name";
    }

    protected static String getSite(MCRObject participantMcrObj) {
        MCRMetaElement urls = participantMcrObj.getMetadata().getMetadataElement("urls");
        if (urls != null) {
            for (MCRMetaInterface url : urls) {
                if (url instanceof MCRMetaLink) {
                    return ((MCRMetaLink) url).getXLinkHref();
                }
            }
        }
        return null;
    }

}
