package fsu.jportal.frontend.cli;

import fsu.jportal.backend.DerivateTools;
import fsu.jportal.util.MetsUtil;

import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;

/**
 * Created by chi on 08.01.18.
 *
 * @author Huu Chi Vu
 */
@MCRCommandGroup(name = "JP Derivate CMDs")
public class DerivateCMD {

    @MCRCommand(syntax = "set maindoc for derivate {0} to file {1}")
    public static void setMainDoc(String derivID, String path) {
        DerivateTools.setAsMain(derivID, path);
    }

    @MCRCommand(help = "generates a new mets.xml and replaces the old one",
        syntax = "generate and replace mets.xml for derivate {0}")
    public static void generateAndReplaceMetsXML(String derivateId) throws Exception {
        MetsUtil.generateAndReplace(MCRObjectID.getInstance(derivateId));
    }

}
