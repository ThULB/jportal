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

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaInstitutionName;
import org.mycore.datamodel.metadata.MCRMetaInterface;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetaLink;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

import fsu.jportal.constants.ParticipantRoleTypes;
import fsu.jportal.mods.MODSLogoEntity;
import fsu.jportal.xml.mapper.MODSLogoEntityXMLMapper;

@Path("modslogos")
public class MODSLogoResource {
    static Logger LOGGER = Logger.getLogger(MODSLogoResource.class);
    @GET
    @Path("{hiddenJournalId}")
    @Produces(MediaType.APPLICATION_XML)
    public String getLogos(@PathParam("hiddenJournalId") String hiddenJournalId) throws IOException {
        LOGGER.info("MODS logo for " + hiddenJournalId);
        List<MODSLogoEntity> entities = new ArrayList<MODSLogoEntity>();
        MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(hiddenJournalId));
        MCRMetaElement participants = mcrObject.getMetadata().getMetadataElement("participants");
        if(participants != null) {
            for (MCRMetaInterface retrievedElem : participants) {
                if (retrievedElem instanceof MCRMetaLinkID) {
                    MCRMetaLinkID participant = (MCRMetaLinkID) retrievedElem;
                    String role = participant.getType();
                    if (ParticipantRoleTypes.equals(role)) {
                        String participantID = participant.getXLinkHref();
                        MCRObject participantMcrObj = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(participantID));
                        entities.add(createLogoEntity(role, participantMcrObj));
                    }
                }
            }
        }
        return xmlToString(entities);
    }

    protected MODSLogoEntity createLogoEntity(String role, MCRObject participantMcrObj) {
        MODSLogoEntity modsLogoEntity = new MODSLogoEntity();
        modsLogoEntity.setName(getFullname(participantMcrObj));
        modsLogoEntity.setRole(role);
        modsLogoEntity.setSiteURL(getSite(participantMcrObj));
        getLogoURL(participantMcrObj, modsLogoEntity);
        return modsLogoEntity;
    }

    protected String xmlToString(List<MODSLogoEntity> entities) throws IOException {
        Document xml = new MODSLogoEntityXMLMapper(entities).getXML();
        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        Writer stringWriter = new StringWriter();
        xmlOutputter.output(xml, stringWriter);
        return stringWriter.toString();
    }

    protected void getLogoURL(MCRObject participantMcrObj, MODSLogoEntity modsLogoEntity) {
        MCRMetaElement logo = participantMcrObj.getMetadata().getMetadataElement("logo");
        if(logo == null) {
            return;
        }
        for (MCRMetaInterface logoElem : logo) {
            if(logoElem instanceof MCRMetaLangText){
                MCRMetaLangText url = (MCRMetaLangText) logoElem;
                modsLogoEntity.setLogoUrl(url.getType(), url.getText());
            }
        }
    }

    protected String getFullname(MCRObject participantMcrObj) {
        MCRMetaElement names = participantMcrObj.getMetadata().getMetadataElement("names");
        if(names != null) {
            for (MCRMetaInterface nameElem : names) {
                if (nameElem instanceof MCRMetaInstitutionName) {
                    MCRMetaInstitutionName name = (MCRMetaInstitutionName) nameElem;
                    return name.getFullName();
                }
            }
        }
        return "No Name";
    }

    protected String getSite(MCRObject participantMcrObj) {
        MCRMetaElement urls = participantMcrObj.getMetadata().getMetadataElement("urls");
        if(urls != null) {
            for (MCRMetaInterface url : urls) {
                if (url instanceof MCRMetaLink) {
                    return ((MCRMetaLink) url).getXLinkHref();
                }
            }
        }
        return null;
    }

}
