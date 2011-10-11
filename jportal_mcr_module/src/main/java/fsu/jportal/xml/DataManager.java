package fsu.jportal.xml;

import org.mycore.datamodel.metadata.MCRObject;

public interface DataManager {
    public MCRObject getObj(String id);
    public void update(MCRObject obj);
}
