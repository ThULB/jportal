package fsu.jportal.fix;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Content;
import org.jdom.Element;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaInstitutionName;
import org.mycore.datamodel.metadata.MCRMetaInterface;
import org.mycore.datamodel.metadata.MCRMetaLink;
import org.mycore.datamodel.metadata.MCRMetaXML;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.parsers.bool.MCRCondition;
import org.mycore.services.fieldquery.MCRFieldDef;
import org.mycore.services.fieldquery.MCRHit;
import org.mycore.services.fieldquery.MCRQuery;
import org.mycore.services.fieldquery.MCRQueryCondition;
import org.mycore.services.fieldquery.MCRQueryManager;
import org.mycore.services.fieldquery.MCRResults;

public class PersonFixServlet extends MCRServlet {

    static Logger LOGGER = Logger.getLogger(PersonFixServlet.class);

    @Override
    protected void doGetPost(MCRServletJob job) throws Exception {
        String mode = job.getRequest().getParameter("mode");
        if(mode.equals("count")) {
            int hits = find().getNumHits();
            getLayoutService().doLayout(job.getRequest(), job.getResponse(), new MCRJDOMContent(new Element("count").setText(String.valueOf(hits))));
        } else if(mode.equals("repair")) {
            MCRResults results = find();
            List<String> errorList = repair(results);
            Element repair = new Element("repair");
            repair.addContent(new Element("count").setText(String.valueOf(results.getNumHits())));
            Element errors = new Element("error");
            repair.addContent(errors);
            for(String mcrId : errorList) {
                errors.addContent(new Element("mcrobject").setText(mcrId));
            }
            getLayoutService().doLayout(job.getRequest(), job.getResponse(), new MCRJDOMContent(repair));
        }
    }

    public MCRResults find() {
        MCRCondition<Object> cond = new MCRQueryCondition(MCRFieldDef.getDef("corruptParticipants"), ">", "0");
        MCRQuery query = new MCRQuery(cond);
        return MCRQueryManager.search(query);
    }

    public List<String> repair(MCRResults results) {
        List<String> errorList = new ArrayList<String>();
        for(int i = 0; i < results.getNumHits(); i++) {
            MCRHit hit = results.getHit(i);
            String mcrId = hit.getID();
            try {
                repair(MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(mcrId)));
            } catch(Exception exc) {
                LOGGER.error("Unable to update " + mcrId, exc);
                errorList.add(mcrId);
            }
        }
        return errorList;
    }

    public void repair(MCRObject mcrObj) throws MCRActiveLinkException {
        MCRMetaElement participants = mcrObj.getMetadata().getMetadataElement("participants");
        Iterator<MCRMetaInterface> it = participants.iterator();
        while(it.hasNext()) {
            MCRMetaInterface metaInterface = it.next();
            if(metaInterface instanceof MCRMetaLink) {
                MCRMetaLink participant = (MCRMetaLink)metaInterface;
                String title = getTitle(participant.getXLinkHref());
                if(title != null) {
                    participant.setXLinkTitle(title);
                } else {
                    LOGGER.warn("Unable to set title of " + participant.getXLinkHref() + " in article " + mcrObj.getId().toString());
                }
            }
        }
        MCRMetadataManager.update(mcrObj);
    }

    public String getTitle(String id) {
        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(id));
        if(mcrObj.getId().getTypeId().equals("person")) {
            MCRMetaElement defHeading = mcrObj.getMetadata().getMetadataElement("def.heading");
            MCRMetaInterface metaInterface = defHeading.getElementByName("heading");
            String lastName = null;
            String firstName = null;
            if(metaInterface instanceof MCRMetaXML) {
                MCRMetaXML heading = (MCRMetaXML)metaInterface;
                for(Content content : heading.getContent()) {
                    if(content instanceof Element) {
                        Element e = (Element)content;
                        if(e.getName().equals("lastName")) {
                            lastName = e.getText();
                        } else if(e.getName().equals("firstName")) {
                            firstName = e.getText();
                        }
                    }
                }
            }
            if(firstName != null) {
                return lastName + ", " + firstName;
            }
            return lastName;
        } else if(mcrObj.getId().getTypeId().equals("jpinst")) {
            MCRMetaElement names = mcrObj.getMetadata().getMetadataElement("names");
            MCRMetaInterface metaInterface = names.getElementByName("name");
            if(metaInterface instanceof MCRMetaInstitutionName) {
                return ((MCRMetaInstitutionName)metaInterface).getFullName();
            }
        }
        return null;
    }

}
