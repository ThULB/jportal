/*
 * 
 * $Revision: 13085 $ $Date: 2008-02-06 18:27:24 +0100 (Mi, 06 Feb 2008) $
 *
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * This program is free software; you can use it, redistribute it
 * and / or modify it under the terms of the GNU General Public License
 * (GPL) as published by the Free Software Foundation; either version 2
 * of the License or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, in a file called gpl.txt or license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 */

package org.mycore.frontend.wcms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;

/**
 * Select action process for Web-Content-Management-System (WCMS).
 */
public class MCRWCMSChooseServlet extends MCRWCMSServlet {

	private static final long serialVersionUID = 1L;

	private Namespace ns = Namespace.XML_NAMESPACE; // xml Namespace for the

    // language attribute lang
    private String currentLang = null; //

    private String defaultLang = null; //

    private String contentError = null;

    private String href = null; // representing the href(dir) attribute of the

    // selected element in the navigation.xml
    // e.g. type="extern" --> href="http://www.db-thueringen.de" ||
    // type="intern" --> href="/content/main/information/description.xml" ||
    // dir != null --> href=dir(e.g. href="/main")
    private String action = null; // selected action:{add, edit, delete}

    private String mode = null; // if (action.equals(edit) mode = mode from

    // selected element:{intern, extern}
    // else mode=moda
    private String moda = null; // mode dependent from selected action:{intern,

    // extern}
    private String label = null; // representing the label(name) of the
                                    // selected

    // link
    private String label_currentLang = null; // representing the translated

    // label(name) of the selected link
    private String target = null; // target of the selected element: {_blank,

    // _self}
    private String template = null; // choosen Template

    private String style = null; // style of the selected element: {bold, normal}

    // have a valid dir attribute instead of a href
    // attribute
    private String addAtPosition = null; // position, where a link can be
                                            // added:

    // {predecessor, successor, child}
    private String replaceMenu = null; // Representing a Parameter, allowing an

    // navigation element to replace the
    // previous navigation structure only
    // with its subelements. Can be "true" or
    // "false".
    private String constrainPopUp = null; // menu must be open

    char c; // representing the errorcodes {1,2,5,6,7,8,9,0}

    char d; // representing the errorcodes {1,2,5,6,7,8,9,0}

    char fs = File.separatorChar;

    boolean validXHTML = true;

    List contentList;

    List defaultLangContentOutput;

    List currentLangContentOutput;

    /**
     * Main program called by doGet and doPost.
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	
        MCRSession mcrSession = MCRSessionMgr.getCurrentSession();
        String userID = (String) mcrSession.get("userID");
        String userClass = (String) mcrSession.get("userClass");
        List rootNodes = (List) mcrSession.get("rootNodes");
        addAtPosition = request.getParameter("addAtPosition");

        if (request.getParameter("action") != null) {
            action = request.getParameter("action");
        } else {
            action = "false";
        }

        File[] contentTemplates = new File(CONFIG.getString("MCR.templatePath") + "content/".replace('/', File.separatorChar)).listFiles();
        File[] masterTemplates = new File(CONFIG.getString("MCR.templatePath") + "master/".replace('/', File.separatorChar)).listFiles();
        template = request.getParameter("template");

        File conTemp = new File(CONFIG.getString("MCR.templatePath") + "content/".replace('/', File.separatorChar));
        Element templates = new Element("templates");
        defaultLangContentOutput = null;
        currentLangContentOutput = null;
        contentError = null;

        /* get languages */
        if (mcrSession != null) {
            defaultLang = MCRConfiguration.instance().getString("MCR.Metadata.DefaultLang", "en").toLowerCase();
            currentLang = mcrSession.getCurrentLanguage().toLowerCase();
        }

        /*
         * System.err.println("defaultLang"+defaultLang+"...");
         * System.err.println("currentLang"+currentLang+"...");
         */

        /**
         * If action parameter contains a character '_' (e.g.: add_intern,
         * add_extern) then split action in action (e.g.: add) and mode (e.g.:
         * intern)
         */
        if ((action != null) && (action.indexOf('_') != -1)) {
            moda = action.substring(action.indexOf('_') + 1);
            action = action.substring(0, action.indexOf('_'));
        }

        if (action.equals("false")) {
            contentError = "Error: Please select an action to be done.";
        }

        /**
         * Split href parameter (e.g.: 1/content/top/index.xml) in character c
         * and href for identifying the selected element in the navigation.xml
         * and checking the selected action Transfered characters are: --- valid
         * values
         * ------------------------------------------------------------------------
         * '1' - if the selected element has a valid href attribute. '2' - if
         * the selected element was a main element (e.g.: "Men? oberhalb", "Men?
         * links" and "Zusatzmen? links unten") and has a valid dir attribute
         * instead of a valid href attribute. --- invalid values
         * ----------------------------------------------------------------------
         * '5' - if the selected element is a main navigation element like "Men?
         * links" and addAtPosition is "predecessor" or "successor". '6' - if
         * the selected element has an external target and action was add. This
         * is to avoid the creation of a child under a parent with external
         * target because it can not reached via navigation tree. '7' - if the
         * selected element has child(s) and action was delete. Thus, an element
         * with active childs can not be deleted. Deleting elements with active
         * childs is only possible when deleting all childs first. '8' - if the
         * selected element has childs but is not nested within a rootNode. This
         * occurs only on restricted users trying to edit such an element. '9' -
         * if a place holder was selected. '0' - if nothing was selected.
         */
        if (request.getParameter("href") != null) {
            href = request.getParameter("href").substring(1);
            c = d = request.getParameter("href").charAt(0);
        } else {
            c = d = '0';
        }

        if (c == '2') {
            mcrSession.put("dir", href);
        } else {
            mcrSession.put("dir", "false");
        }

        /**
         * Build jdom object dependence on the selected action and selected
         * element.
         */
        Element rootOut = new Element("cms");
        Document jdom = new Document(rootOut);

        /**
         * Try to build a jdom Object from the content of the navigation.xml.
         */
        try {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(CONFIG.getString("MCR.navigationFile").replace('/', File.separatorChar));
            Element root = doc.getRootElement();
            validate(root);
        } catch (JDOMException jdome) {
            PrintWriter errorOut = response.getWriter();
            errorOut.println(jdome.getMessage());
            errorOut.close();
        }

        /**
         * Try to build a jdom Object from the content (this has to be valid
         * XHTML content) of the file to be edited. The path of this file is
         * build from the webapp dir and the href parameter of the selected
         * element found in the navigation.xml.
         */
        if ((action.equals("edit") && (c == '1')) || action.equals("add") || (action.equals("translate") && (c == '1'))) {
            defaultLangContentOutput = new Vector();
            currentLangContentOutput = new Vector();

            try {
                SAXBuilder sax = new SAXBuilder();
                Document ed;

                if (!action.equals("add") && mode.equals("intern")) {
                    ed = sax.build(getServletContext().getRealPath("") + fs + href);
                } else {
                    // if (mode.equals("intern")) {
                	String edFiller = (CONFIG.getString("MCR.templatePath") + "content/").replace('/', File.separatorChar) + template; 
                    ed = sax.build(edFiller);
                    System.out.println(edFiller);
                    // }
                    // else return;
                }

                Element begin = ed.getRootElement();
                List contentElements = begin.getChildren("section");
                Iterator contentElementsIterator = contentElements.iterator();

                while (contentElementsIterator.hasNext()) {
                    Element content = (Element) contentElementsIterator.next();

                    if (content.getAttributeValue("lang", ns) != null) {
                        contentList = content.getContent();
                        validXHTML = true;

                        if (content.getAttributeValue("lang", ns).equals(defaultLang)) {
                            for (int i = (contentList.size() - 1); i >= 0; i--) {
                                Object o = contentList.get(i);

                                // String u = null;
                                if (o instanceof Element) {
                                    ((Element) o).detach();
                                }

                                if (o instanceof Text) {
                                    ((Text) o).detach();
                                }

                                if (o instanceof Comment) {
                                    ((Comment) o).detach();
                                }

                                defaultLangContentOutput.add(o);
                            }

                            // System.out.println("deflistbefore:
                            // "+defaultLangContentOutput.toString());
                            reverse(defaultLangContentOutput);

                            // System.out.println("deflistafter:
                            // "+defaultLangContentOutput.toString());
                            // break;
                        }

                        if (content.getAttributeValue("lang", ns).equals(currentLang) && action.equals("translate")) {
                            for (int i = (contentList.size() - 1); i >= 0; i--) {
                                Object o = contentList.get(i);

                                // String u = null;
                                if (o instanceof Element) {
                                    ((Element) o).detach();
                                }

                                if (o instanceof Text) {
                                    ((Text) o).detach();
                                }

                                if (o instanceof Comment) {
                                    ((Comment) o).detach();
                                }

                                currentLangContentOutput.add(o);
                            }

                            // System.out.println("curlistbefore:
                            // "+currentLangContentOutput.toString());
                            reverse(currentLangContentOutput);

                            // System.out.println("curlistafter:
                            // "+currentLangContentOutput.toString());
                            // break;
                        }
                    }

                    contentList.clear();

                    // else contentError = "Error: \"lang\" attribute not found
                    // in
                    // "+getServletContext().getRealPath("")+href.replace('/',
                    // File.separatorChar);
                }
            } catch (JDOMException je) {
                try {
                    if (!mode.equals("extern")) {
                        // "Error: " + getServletContext().getRealPath("") + href.replace('/', File.separatorChar) + " is no valid XHTML file.";

                        File hrefFile = new File(getServletContext().getRealPath("") + href);
                        BufferedReader br = new BufferedReader(new FileReader(hrefFile));

                        validXHTML = false;

                        String str;
                        String stri = "";

                        while (true) {
                            if ((str = br.readLine()) != null) {
                                stri = stri.concat(str + '\n');
                            } else {
                                break;
                            }
                        }

                        br.close();

                        int begin = stri.indexOf("<section");
                        String wow = stri.substring(begin, stri.indexOf("</section>"));
                        String strin = stri.substring(begin + wow.indexOf('>') + 2, stri.indexOf("</section>"));
                        defaultLangContentOutput.add(strin);
                    }
                } catch (FileNotFoundException fne) {
                    // System.out.println(fne);
                }
            }
        }

        /*
         * catch (JDOMException je) { if (output != null) { output.clear();
         * output.add("File konnte nicht geparst werden."); } }
         */
        if (action.equals("edit") && (c == '2')) {
            c = '8';
        }

        if (action.equals("delete") && (c == '2')) {
            c = '7';
        }

        /**
         * If character c consist an invalid value (5, 6, 7, 8, 9, 0) then print
         * out the corresponding error and restart the WCMSChooseServlet (both
         * things will be done by the belonging stylesheet). Output: <cms>
         * <session>choose </session> <error>5|6|7|8|9|0 </error> <rootNode
         * href="no"|"yes">rootNode </rootNode>{0,*} <templates><content>
         * <template>$template </template>{0,*} </content> </templates> </cms>
         */
        if (((c != '1') && (c != '2')) || (contentError != null)) {
            rootOut.addContent(new Element("session").setText("choose"));

            if ((c != '1') && (c != '2')) {
                rootOut.addContent(new Element("error").setText(String.valueOf(c)));
            } else {
                rootOut.addContent(new Element("contentError").setAttribute("msg", contentError).setText("yes"));
            }

            Iterator rootNodesIterator = rootNodes.iterator();

            while (rootNodesIterator.hasNext()) {
                Element rootNode = (Element) rootNodesIterator.next();
                rootOut.addContent(new Element("rootNode").setAttribute("href", rootNode.getAttributeValue("href")).setText(rootNode.getTextTrim()));
            }

            Element contentTemp = new Element("content");

            for (int i = 0; i < contentTemplates.length; i++) {
                if (!contentTemplates[i].isDirectory()) {
                    contentTemp.addContent(new Element("template").setText(contentTemplates[i].getName()));
                }
            }

            templates.addContent(contentTemp);
            rootOut.addContent(templates);
        }
        /**
         * Else output: <cms><session>action </session> <userName>uid
         * </userName> <userClass>admin|editor|autor </userClass> <action
         * mode="intern"|"extern">add|edit|delete </action>
         * <addAtPosition>predecessor|successor|child </addAtPosition> <-- only
         * at action add <href>href </href> <label>label </label>
         * <target>_blank|_self </target> <style>normal|bold </style>
         * <content>output </content> <-- only at action edit and add
         * <images>$imagePath </images>{0,*} <documents>$documentPath
         * </documents>{0,*} <templates><master><template>$template
         * </template>{0,*} </master> <content><template>conTemp </template>
         * </content> </templates> </cms>
         */
        else {
            rootOut.addContent(new Element("session").setText("action"));
        }

        rootOut.addContent(new Element("userID").setText(userID));
        rootOut.addContent(new Element("userClass").setText(userClass));

        if (action.equals("add") && (mode != null)) {
            mode = moda;
            rootOut.addContent(new Element("action").setAttribute("mode", mode).setText(action));
            rootOut.addContent(new Element("addAtPosition").setText(addAtPosition));
            rootOut.addContent(new Element("replaceMenu").setText(replaceMenu));
        }

        if (action.equals("edit") && (mode != null)) {
            rootOut.addContent(new Element("action").setAttribute("mode", mode).setText(action));

            if (mode.equals("intern")) {
                rootOut.addContent(new Element("replaceMenu").setText(replaceMenu));
            }

            rootOut.addContent(new Element("constrainPopUp").setText(constrainPopUp));
        }

        if (action.equals("delete") && (mode != null)) {
            rootOut.addContent(new Element("action").setAttribute("mode", mode).setText(action));
        }

        if (action.equals("translate") && (mode != null)) {
            rootOut.addContent(new Element("action").setAttribute("mode", mode).setText(action));
            rootOut.addContent(new Element("label_currentLang").setText(label_currentLang));
        }

        rootOut.addContent(new Element("href").setText(href));
        rootOut.addContent(new Element("label").setText(label));
        rootOut.addContent(new Element("target").setText(target));
        rootOut.addContent(new Element("style").setText(style));

        if (defaultLangContentOutput != null) {
            try {
                if (validXHTML) {
                    /*
                     * String test = ""; Element elem = null; for ( int i=0; i
                     * <output.size(); i++ ) { elem = (Element)output.get(i);
                     * test += elem.getText(); } System.out.println(test);
                     */
                    StringWriter sw = new StringWriter();
                    XMLOutputter contentOut = new XMLOutputter(Format.getRawFormat().setTextMode(Format.TextMode.PRESERVE).setEncoding(OUTPUT_ENCODING));

                    // 1.Method: rootOut.addContent(new
                    // Element("content").setText(contentOut.output(defaultLangContentOutput,
                    // contentOut)));
                    contentOut.output(defaultLangContentOutput, sw);

                    // System.out.println("testoutput: "+sw.toString());
                    rootOut.addContent(new Element("content").setText(sw.toString()));
                    sw.flush();
                    sw.close();

                    if (action.equals("translate") && (currentLangContentOutput != null)) {
                        // rootOut.addContent(new
                        // Element("content_currentLang").setText(contentOut.outputString(currentLangContentOutput)));
                        contentOut.output(currentLangContentOutput, sw);

                        rootOut.addContent(new Element("content_currentLang").setText(sw.toString()));
                        sw.flush();
                        sw.close();
                    }
                } else {
                    rootOut.addContent(new Element("content").setText((String) defaultLangContentOutput.get(0)));
                    rootOut.addContent(new Element("error").setText("invalidXHTML"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        templates = getMultimediaConfig(rootOut);

        Element master = new Element("master");

        for (int i = 0; i < masterTemplates.length; i++) {
            if (masterTemplates[i].isDirectory() && (masterTemplates[i].getName().compareToIgnoreCase("cvs") != 0)) {
                master.addContent(new Element("template").setText(masterTemplates[i].getName()));
            }
        }

        Element content = new Element("content");
        content.addContent(new Element("template").setText(conTemp.toString()));
        templates.addContent(master);
        templates.addContent(content);
        rootOut.addContent(templates);

        /**
         * and add some further attributes to the session object: --- allready
         * existing --------------------------------- <session ... uid=uid
         * userClass="admin"|"editor"|"autor" rootNodes=rootNodes ... --- new
         * attributes ------------------------------------ ... href=href
         * label=label action="add"|"edit"|"delete" mode="intern"|"extern" ...
         * ... target="intern"|"extern" style="normal"|"bold" />
         */
        if (href != null) {
            mcrSession.put("href", href);
        }

        if (label != null) {
            mcrSession.put("label", label);
        }

        if (label_currentLang != null) {
            mcrSession.put("label_currentLang", label_currentLang);
        }

        if (action != null) {
            mcrSession.put("action", action);
        }

        if (mode != null) {
            mcrSession.put("mode", mode);
        }

        if (target != null) {
            mcrSession.put("target", target);
        }

        if (style != null) {
            mcrSession.put("style", style);
        }

        if (defaultLang != null) {
            mcrSession.put("defaultLang", defaultLang);
        }

        if (currentLang != null) {
            mcrSession.put("currentLang", currentLang);
        }

        if (addAtPosition != null) {
            mcrSession.put("addAtPosition", addAtPosition);
        }

        getLayoutService().doLayout(request, response, jdom);
    }



    /**
     * Finds the selected element in the navigation.xml and assigns attribute
     * values for the variables of the jdom object and setting character c
     * respectively.
     */
    public void validate(Element element) {
        List elements = element.getChildren();
        Iterator elementIterator = elements.iterator();

        while (elementIterator.hasNext()) {
            Element child = (Element) elementIterator.next();

            if (child.getAttribute("href") != null) {
                if (child.getAttributeValue("href").equals(href)) {
                    if (action.equals("delete") && !child.getChildren("item").isEmpty()) {
                        c = '7';
                    }
                    
                      else if (action.equals("add") &&
                      child.getAttributeValue("type").equals("extern") &&
                      addAtPosition.equals("child")) c = '6';
                     
                    else {
                        c = d;
                    }

                    if (child.getAttributeValue("type") != null) {
                        mode = child.getAttributeValue("type");
                    }

                    if ((getLabel(child, defaultLang) != null) && !getLabel(child, defaultLang).equals("")) {
                        label = getLabel(child, defaultLang);
                    }

                    if ((getLabel(child, currentLang) != null) && !getLabel(child, currentLang).equals("")) {
                        label_currentLang = getLabel(child, currentLang);
                    }

                    if (child.getAttributeValue("target") != null) {
                        target = child.getAttributeValue("target");
                    }

                    if (child.getAttributeValue("style") != null) {
                        style = child.getAttributeValue("style");
                    }

                    if (child.getAttributeValue("replaceMenu") != null) {
                        replaceMenu = child.getAttributeValue("replaceMenu");
                    }

                    if (child.getAttributeValue("constrainPopUp") != null) {
                        constrainPopUp = child.getAttributeValue("constrainPopUp");
                    } else {
                        replaceMenu = "false";
                    }

                    return;
                }
            }

            if (child.getAttributeValue("dir") != null) {
                if (child.getAttributeValue("dir").equals(href)) {
                    href = ((Element) child.getParent()).getAttributeValue("dir") + href;
                    mode = moda;

                    if (action.equals("delete") && !child.getChildren("item").isEmpty()) {
                        c = '7';
                    } else {
                        c = d;
                    }

                    if (action.equals("add") && !addAtPosition.equals("child")) {
                        c = '5';
                    }

                    return;
                }
            }

            validate(child);
        }
    }

    /**
     * Finds the right label
     */
    public String getLabel(Element element, String language) {
        List elements = element.getChildren("label");
        Iterator elementIterator = elements.iterator();
        String labelReturn = null;

        while (elementIterator.hasNext()) {
            Element child = (Element) elementIterator.next();

            if (child.getAttribute("lang", ns) != null) {
                if (child.getAttributeValue("lang", ns).equals(language)) {
                    if (child.getText() != null) {
                        labelReturn = child.getTextTrim();
                    }
                }
            }

            getLabel(child, language);
        }

        return labelReturn;
    }

    public void reverse(List l) {
        ListIterator fwd = l.listIterator();
        ListIterator rev = l.listIterator(l.size());

        for (int i = 0, n = l.size() / 2; i < n; i++) {
            Object tmp = fwd.next();
            fwd.set(rev.previous());
            rev.set(tmp);
        }
    }
}
