package org.mycore.services.fieldquery;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class InheritanceTest extends TestCase {
    class Parent{
        private Logger LOGGER = Logger.getLogger(this.getClass());
        protected Logger getLogger(){
            BasicConfigurator.configure();
//            if (LOGGER == null) {
//                LOGGER = Logger.getLogger(this.getClass());
//            }
            
            return LOGGER;
        }
    }
    
    class Child extends Parent{
        public void info(){
            getLogger().info("This is the Child");
        }
    }
    
    public void testname() throws Exception {
        Child child = new Child();
        child.info();
    }
}
