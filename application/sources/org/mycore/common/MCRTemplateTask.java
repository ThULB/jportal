package org.mycore.common;

import java.io.File;
import java.io.FilenameFilter;
import java.io.FileOutputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.*;

/**
 * Ant task that is looking for new templates and 
 * automatically generating a new chooseTemplate.xsl 
 * @author Stephan Schmidt (mcrschmi)
 */

//filters the wrong directories
class TemplateFilenameFilter implements FilenameFilter{
	
	public boolean accept(File f, String s)
		{
		 return (s.indexOf("template") != -1);
		}
}

public class MCRTemplateTask extends Task {

//	 some fields settable and gettable via methods	
private String templatepath;
private String choosepath;
private String directories[];


/**
 * Execute the requested operation.
 * 
 * throws BuildException
 *             if an error occurs
 */
	public void execute() throws BuildException {
		path(templatepath);   
	    }

/**
 * 
 * Reading all the templatedirectories 
 * 
 * 
 */
	public void path(String dir){
		File templatedir = new File(dir);
		directories = templatedir.list(new TemplateFilenameFilter());
		for (int i=0; i<directories.length; i++) 
    	{	
			System.out.println(i+":"+directories[i]);
    	}
        createxsl(directories);
	}
	
/**
 * 
 * is creating the chooseTemplate.xsl
 * 
 */	
	
	
	public void createxsl(String[] directory){
		String temppath = "";
		String temptest = "";
		
        Element rootOut = new Element("stylesheet", "xsl" , "http://www.w3.org/1999/XSL/Transform").setAttribute("version", "1.0");
        Document jdom = new Document(rootOut);
        
        
        for (int i=0; i<directory.length; i++) 
        	{	
        	 temppath=directory[i]+".xsl";
        	 rootOut.addContent(new Element("include", "xsl" , "http://www.w3.org/1999/XSL/Transform").setAttribute("href", temppath));
        	}
        
        Element template = new Element("template", "xsl" , "http://www.w3.org/1999/XSL/Transform").setAttribute("name", "chooseTemplate");
        Element choose = new Element("choose", "xsl" , "http://www.w3.org/1999/XSL/Transform");
        
        for (int i=0; i<directory.length; i++) 
    		{
        	 temptest= "$template = '"+ directory[i] + "'";
        	 Element when = new Element("when", "xsl" , "http://www.w3.org/1999/XSL/Transform").setAttribute("test", temptest);
        	 Element call = new Element("call-template", "xsl" , "http://www.w3.org/1999/XSL/Transform").setAttribute("name", directory[i]);
        	 when.addContent(call);
        	 choose.addContent(when);
    		}
        template.addContent(choose);
        rootOut.addContent(template);
        
        //try to save the xsl stream
        try{
        XMLOutputter xmlOut = new XMLOutputter();
		FileOutputStream fos = new FileOutputStream(new File(choosepath));
		xmlOut.output(jdom, fos);
		fos.flush();
		fos.close();
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }
 
		      
	}

	
//getter and setter for the templatepath and the choosepath	
	
	public String getTemplatepath() {
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
    }
}