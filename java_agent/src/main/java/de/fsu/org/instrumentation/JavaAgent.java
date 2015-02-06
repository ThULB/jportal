package de.fsu.org.instrumentation;

import java.lang.instrument.Instrumentation;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class JavaAgent {
    static final Logger LOGGER = Logger.getLogger(JavaAgent.class);
    private static String[] argsArrays;
    
    static {
        BasicConfigurator.configure();
        LOGGER.setLevel(Level.INFO);
    }
    
    public static String[] getArgsArrays() {
        return argsArrays;
    }
    
    public static void premain(String args, Instrumentation inst) {
        LOGGER.info("Java Agent/Hot Deployment active");
        argsArrays = args.split(",");
        inst.addTransformer(new MCRXSLTransformerTransformer());
        inst.addTransformer(new ClassLoaderTransformer());
        inst.addTransformer(new ServletContextTransformer());
    }

}
