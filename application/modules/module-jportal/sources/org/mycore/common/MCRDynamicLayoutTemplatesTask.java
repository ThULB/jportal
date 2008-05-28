package org.mycore.common;

import java.io.File;
import java.io.FilenameFilter;
import java.io.FileOutputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.*;

/**
 * Ant task that is looking for new templates and automatically generating a new
 * chooseTemplate.xsl
 * 
 * @author Stephan Schmidt (mcrschmi)
 */

public class MCRDynamicLayoutTemplatesTask extends Task {

    private String choosepath;

    /**
     * Execute the requested operation.
     * 
     * throws BuildException if an error occurs
     */
    public void execute() throws BuildException {

/*        File templatedir = new File(dir);
        // directories = templatedir.list(new TemplateFilenameFilter());
        for (int i = 0; i < directories.length; i++) {
            System.out.println(i + ":" + directories[i]);
        }
        createxsl(directories);
  
        Element rootOut = new Element("stylesheet", "xsl", "http://www.w3.org/1999/XSL/Transform").setAttribute("version", "1.0").setAttribute(
                        "layoutDetector", "xalan://org.mycore.frontend.MCRJPortalLayoutTemplateDetector", org.jdom.Namespace.XML_NAMESPACE).setAttribute(
                        "xalan", "http://xml.apache.org/xalan", org.jdom.Namespace.XML_NAMESPACE);
        ;
        Document jdom = new Document(rootOut);

        Element template = new Element("template", "xsl", "http://www.w3.org/1999/XSL/Transform").setAttribute("name", "chooseTemplate");
        Element choose = new Element("choose", "xsl", "http://www.w3.org/1999/XSL/Transform");

        for (int i = 0; i < directory.length; i++) {
            temptest = "$template = '" + directory[i] + "'";
            Element when = new Element("when", "xsl", "http://www.w3.org/1999/XSL/Transform").setAttribute("test", temptest);
            Element call = new Element("call-template", "xsl", "http://www.w3.org/1999/XSL/Transform").setAttribute("name", directory[i]);
            when.addContent(call);
            choose.addContent(when);
        }
        template.addContent(choose);
        rootOut.addContent(template);

        // try to save the xsl stream
        try {
            XMLOutputter xmlOut = new XMLOutputter();
            FileOutputStream fos = new FileOutputStream(new File(choosepath));
            xmlOut.output(jdom, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
*/
    }

    // getter and setter for the templatepath and the choosepath

/*    public String getTemplatepath() {
        return templatepath;
    }

    public void setTemplatepath(String templatepath) {
        this.templatepath = templatepath;
    }

    public String getChoosepath() {
        return choosepath;
    }

    public void setChoosepath(String choosepath) {
        this.choosepath = choosepath;
    }*/
}