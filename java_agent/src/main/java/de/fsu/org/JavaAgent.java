package de.fsu.org;

import java.lang.instrument.Instrumentation;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.fsu.org.instrumentation.ClassLoaderTransformer;
import de.fsu.org.instrumentation.MCRXSLTransformerTransformer;

public class JavaAgent {
    static final Logger LOGGER = Logger.getLogger(JavaAgent.class.getName());
    static {
        BasicConfigurator.configure();
        LOGGER.setLevel(Level.INFO);
    }

    public static void premain(String args, Instrumentation inst) {
        LOGGER.info("Test Super Java Agent");
        inst.addTransformer(new MCRXSLTransformerTransformer());
        inst.addTransformer(new ClassLoaderTransformer());
    }

}
