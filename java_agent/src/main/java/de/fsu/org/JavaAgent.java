package de.fsu.org;

import java.lang.instrument.Instrumentation;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class JavaAgent {
    static final Logger LOGGER = Logger.getLogger(JavaAgent.class.getName());
    static {
        BasicConfigurator.configure();
        LOGGER.setLevel(Level.INFO);
    }

    public static void premain(String args, Instrumentation inst) {
        LOGGER.info("Test Super Java Agent");
        SimpleClassTransformer simpleClassTransformer = new SimpleClassTransformer();
        inst.addTransformer(simpleClassTransformer);
    }

}
