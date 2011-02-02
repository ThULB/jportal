package org.mycore.frontend.cli;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.Filter;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.parsers.bool.MCROrCondition;
import org.mycore.services.fieldquery.MCRFieldDef;
import org.mycore.services.fieldquery.MCRHit;
import org.mycore.services.fieldquery.MCRQuery;
import org.mycore.services.fieldquery.MCRQueryCondition;
import org.mycore.services.fieldquery.MCRQueryManager;
import org.mycore.services.fieldquery.MCRResults;

/**
 * This command runs through journals, volumes and articles
 * and deletes all xml:lang attributes. The old version of jportal
 * contains languages like latin and hebrew which are not support by
 * java. @see MCRUtils.isSupportedLang
 *
 * @author Matthias Eichner
 */
public class DeleteLangCommand {

    public static List<String> delete() throws Exception {
       // build search condition
        MCROrCondition orCond = new MCROrCondition();
        MCRQueryCondition jpArticleCond = new MCRQueryCondition(MCRFieldDef.getDef("objectType"), "=", "jparticle");
        MCRQueryCondition jpVolumeCond = new MCRQueryCondition(MCRFieldDef.getDef("objectType"), "=", "jpvolume");
        MCRQueryCondition jpJournalCond = new MCRQueryCondition(MCRFieldDef.getDef("objectType"), "=", "jpjournal");
        orCond.addChild(jpArticleCond);
        orCond.addChild(jpVolumeCond);
        orCond.addChild(jpJournalCond);

        // search
        MCRQuery query = new MCRQuery(orCond);
        MCRResults results = MCRQueryManager.search(query);
        List<String> commandList =new ArrayList<String>();

        // go through all articles and volumes
        for(MCRHit hit : results) {
            // get the mcr object
            commandList.add("internal delete xml:lang " + hit.getID());
        }
        return commandList;
    }

    public static void deleteXmlLang(String id) throws Exception {
        MCRObjectID mcrId = MCRObjectID.getInstance(id);
        Document doc = MCRXMLMetadataManager.instance().retrieveXML(mcrId);

        boolean hasXmlLang = false;
        Iterator it = doc.getDescendants(new XmlLangFilter());
        while(it.hasNext()) {
            hasXmlLang = true;
            Element eWithLang = (Element)it.next();
            eWithLang.removeAttribute("lang", Namespace.XML_NAMESPACE);
        }

        if(hasXmlLang)
            MCRXMLMetadataManager.instance().update(mcrId, doc, new Date(System.currentTimeMillis()));
    }

    private static class XmlLangFilter implements Filter {
        @Override
        public boolean matches(Object o) {
            if(!(o instanceof Element))
                return false;
            Element e = (Element)o;
            String lang = e.getAttributeValue("lang", Namespace.XML_NAMESPACE);
            if(lang == null)
                return false;
            return true;
        }
    }
}
