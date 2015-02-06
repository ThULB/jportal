package de.fsu.org.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import de.fsu.org.ext.ClassLoaderExt;

public class ClassLoaderFiddle extends ClassLoader {
    @Override
    protected URL findResource(String name) {
        System.out.println("Find Resource: " + name);
        //        if("xsl/jp-layout-main.xsl".equals(name)) {
        //            System.out.println("Find Resource: " + name);
        //            try {
        //                return new File("/Users/chi/Development/workspace/jportal_gradle/jportal_mcr_module/src/main/resources/xsl/jp-layout-main.xsl").toURI().toURL();
        //            } catch (MalformedURLException e) {
        //                // TODO Auto-generated catch block
        //                e.printStackTrace();
        //            }
        //        }
        return super.findResource(name);
    }

    public Enumeration<URL> getResources_orig(String name) throws IOException {
        return null;
    }

    public Enumeration<URL> getResources(String name) throws IOException {
        ClassLoaderExt loaderExt = new ClassLoaderExt();
        ArrayList<URL> resources = loaderExt._getResources(name);
        if (!resources.isEmpty()) {
            return Collections.enumeration(resources);
        }

        return getResources_orig(name);
    }

    public boolean check() {
        return true;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        ClassLoaderExt loaderExt = new ClassLoaderExt();
        InputStream is = loaderExt._getResourceAsStream(name);
        if (is != null) {
            return is;
        }

        return getResourceAsStream_orig(name);
    }

    private InputStream getResourceAsStream_orig(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public URL getResource(String name) {
        ClassLoaderExt loaderExt = new ClassLoaderExt();
        URL is = loaderExt._getResource(name);
        if (is != null) {
            return is;
        }

        return getResource_orig(name);
    }

    public URL getResource_orig(String name) {
        // TODO Auto-generated method stub
        return null;
    }
}
