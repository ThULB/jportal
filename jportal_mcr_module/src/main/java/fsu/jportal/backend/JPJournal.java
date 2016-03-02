package fsu.jportal.backend;

import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

/**
 * Journal abstraction. Be aware this class is not fully implemented.
 * 
 * @author Matthias Eichner
 */
public class JPJournal extends JPContainer {

    public static String TYPE = "jpjournal";

    public JPJournal() {
        super();
    }

    public JPJournal(String mcrId) {
        super(mcrId);
    }

    public JPJournal(MCRObjectID mcrId) {
        super(mcrId);
    }

    public JPJournal(MCRObject mcrObject) {
        super(mcrObject);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getNameOfTemplate() {
        return getText("hidden_templates", null).orElse(null);
    }

}
