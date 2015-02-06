package de.fsu.org.test;

import static org.junit.Assert.*;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import org.junit.Test;

public class ASMFiddleTest {
    

    @Test
    public void test() {
//        fail("Not yet implemented");
        URL[] urLs = ((URLClassLoader)getClass().getClassLoader()).getURLs();
        System.out.println(Arrays.toString(urLs));
    }

}
