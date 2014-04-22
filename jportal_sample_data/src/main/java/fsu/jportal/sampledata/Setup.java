package fsu.jportal.sampledata;

import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletContext;

import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.events.MCRStartupHandler.AutoExecutable;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.xml.sax.SAXParseException;

/**
 * To include the sample data on application startup you have to add this class to the 'MCR.Startup.Class'
 * property and enable it via 'JP.Sampledata.loadOnStartup=true'.
 * 
 * @author Matthias Eichner
 */
public class Setup implements AutoExecutable {

    @Override
    public String getName() {
        return "Sample Data Setup";
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void startUp(ServletContext servletContext) {
        boolean loadOnStartup = MCRConfiguration.instance().getBoolean("JP.Sampledata.loadOnStartup", false);
        if (!loadOnStartup) {
            return;
        }
        try {
            MCRSession currentSession = MCRSessionMgr.getCurrentSession();
            currentSession.beginTransaction();
            ClassLoader classLoader = getClass().getClassLoader();
            loadFromFile(classLoader.getResource("jportal_person_00000001.xml").toURI());
            loadFromFile(classLoader.getResource("jportal_jpinst_00000001.xml").toURI());
            loadFromFile(classLoader.getResource("jportal_jpjournal_00000001.xml").toURI());
            loadFromFile(classLoader.getResource("jportal_jpvolume_00000001.xml").toURI());
            loadFromFile(classLoader.getResource("jportal_jparticle_00000001.xml").toURI());
            currentSession.commitTransaction();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    public void loadFromFile(URI uri) throws SAXParseException, IOException {
        MCRObject mcrObj = new MCRObject(uri);
        mcrObj.setImportMode(true);
        MCRMetadataManager.create(mcrObj);
    }

}
