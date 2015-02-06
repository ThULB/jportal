package de.fsu.org.test;

import static org.junit.Assert.*;

import org.junit.Test;

public class Foo {

    @Test
    public void test() {
        String clazz1 = "java/lang/ClassLoader";
        String clazz2 = "java/lang/ClassLoader";
        
        System.out.println(clazz1.equals(clazz2));
    }

}
