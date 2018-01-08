package fsu.jportal.frontend.cli;

import fsu.jportal.backend.DerivateTools;
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
    public static void setMainDoc(String derivID, String path){
        DerivateTools.setAsMain(derivID, path);
    }
}