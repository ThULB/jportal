package de.fsu.org.instrumentation;

import java.lang.instrument.Instrumentation;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class JavaAgent {
    static final Logger LOGGER = LogManager.getLogger(JavaAgent.class);
    private static String[] argsArrays;
    
    static {
//        TODO
//        BasicConfigurator.configure();
//        LOGGER.setLevel(Level.INFO);
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
        inst.addTransformer(new ServletTransformer());
    }

}
