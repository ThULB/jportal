package org.mycore.frontend.cli;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import murlen.util.fscript.FSException;
import murlen.util.fscript.FSReflectionExtension;
import murlen.util.fscript.FScript;

import org.apache.log4j.Logger;

public class MCRFscriptExtension extends MCRAbstractCommands{
    private static Logger LOGGER = Logger.getLogger(MCRFscriptExtension.class.getName());
    
    public MCRFscriptExtension() {
        super();
        
        MCRCommand com = null;

        com = new MCRCommand("load fscript from file {0}", "org.mycore.frontend.cli.MCRFscriptExtension.fscriptFromFile String", "read a fscript from file");
        command.add(com);
    }
    
    public static void fscriptFromFile(String file){
        FScript fscript = new FScript();
        FSReflectionExtension reflection = new FSReflectionExtension();
        fscript.registerExtension(reflection);
        try {
            fscript.load(new FileReader(file));
            fscript.run();
            LOGGER.info("Execute script " + file + " successfully.");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
