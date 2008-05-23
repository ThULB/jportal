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

package org.mycore.services.plugins;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Comment;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.mycore.common.MCRException;
import org.mycore.datamodel.ifs.MCRFileContentType;
import org.mycore.datamodel.ifs.MCRFileContentTypeFactory;
import org.w3c.tidy.Tidy;

/**
 * Converts XML, XTHML and HTML to plain text for indexing
 * 
 * @author Frank L�tzenkirchen
 * @author Harald Richter
 */
public class XmlHtmlPlugin implements TextFilterPlugin {
    /** The logger */
    private static final Logger LOGGER = Logger.getLogger(XmlHtmlPlugin.class);

    private static final int MAJOR = 1;

    private static final int MINOR = 0;

    private static HashSet contentTypes;

    private static String info = null;

    public XmlHtmlPlugin() {
        super();

        if (contentTypes == null) {
            contentTypes = new HashSet();

            if (MCRFileContentTypeFactory.isTypeAvailable("xml")) {
                contentTypes.add(MCRFileContentTypeFactory.getType("xml"));
            }

            if (MCRFileContentTypeFactory.isTypeAvailable("html")) {
                contentTypes.add(MCRFileContentTypeFactory.getType("html"));
            }
        }

        if (info == null) {
            info = new StringBuffer("This filter converts XML, XTHML and HTML to plain text").toString();
        }
    }

    /**
     * @see org.mycore.services.plugins.TextFilterPlugin#getName()
     */
    public String getName() {
        return "hfwri's and fluetze's amazing xml and html Filter";
    }

    /**
     * @see org.mycore.services.plugins.TextFilterPlugin#getInfo()
     */
    public String getInfo() {
        return info;
    }

    /**
     * @see org.mycore.services.plugins.XmlHtmlPlugin#getSupportedContentTypes()
     */
    public HashSet getSupportedContentTypes() {
        return contentTypes;
    }

    /**
     * @see org.mycore.services.plugins.TextFilterPlugin#transform(org.mycore.datamodel.ifs.MCRFileContentType,org.mycore.datamodel.ifs.MCRContentInputStream,
     *      java.io.OutputStream)
     */
    public Reader transform(MCRFileContentType ct, InputStream input) throws FilterPluginTransformException {
        if (getSupportedContentTypes().contains(ct)) {
            String tx = getFullText(ct, input);

            return new StringReader(tx);
        }
        throw new FilterPluginTransformException("ContentType " + ct + " is not supported by " + getName() + "!");
    }

    /**
     * @see org.mycore.services.plugins.TextFilterPlugin#getMajorNumber()
     */
    public int getMajorNumber() {
        return MAJOR;
    }

    /**
     * @see org.mycore.services.plugins.TextFilterPlugin#getMinorNumber()
     */
    public int getMinorNumber() {
        return MINOR;
    }

    private static String getFullText(MCRFileContentType ct, InputStream input) {
        try {
            if (ct.getID().equals("xml")) {
                org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();

                return getText(builder.build(input)); // file.getContentAsJDOM()
            } else if (ct.getID().equals("html")) {
                org.jdom.Document xml = tidy(input);
                return (xml == null ? "" : getText(xml));
            } else {
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();

            return null;
        }
    }

    /** Converts HTML string to XML to be able to extract text nodes * */
    public static String getFullText(String html) {
        org.jdom.Document xml = tidy(new ByteArrayInputStream(html.getBytes()));
        if (xml == null)
            return null;
        else
            return getText(xml);
    }

    /** Converts HTML files to XML to be able to extract text nodes * */
    private static org.jdom.Document tidy(InputStream input) {
        Tidy tidy = new Tidy();
        tidy.setForceOutput(true);
        tidy.setFixComments(true);
        tidy.setHideEndTags(false);
        tidy.setQuiet(!LOGGER.isDebugEnabled());
        tidy.setShowWarnings(LOGGER.isDebugEnabled());
        tidy.setXmlOut(true);
        tidy.setXmlTags(false);
        tidy.setPrintBodyOnly(true);
        tidy.setNumEntities(true);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write("<html><body>".getBytes());
            tidy.parseDOM(input, baos);
            baos.write("</body></html>".getBytes());
            baos.close();
            byte[] bytes = baos.toByteArray();
            LOGGER.debug("------ after JTidy: ------");
            LOGGER.debug(new String(bytes, tidy.getOutputEncoding()));
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            SAXBuilder builder = new SAXBuilder();
            builder.setExpandEntities(false);
            builder.setValidation(false);
            org.jdom.Document jdoc = builder.build(bais);
            return jdoc;
        } catch (Exception ex) {
            LOGGER.info("Exception while tidying HTML to XML: " + ex.getClass().getName() + ": " + ex.getMessage());
            LOGGER.debug(MCRException.getStackTraceAsString(ex));
            return null;
        }
    }

    /** Extracts text of text nodes and comment nodes from xml files * */
    private static String getText(org.jdom.Document xml) {
        StringBuffer buffer = new StringBuffer();
        xml2txt(buffer, xml.getContent());
        LOGGER.debug("------ after xml2txt ------" );
        LOGGER.debug(buffer.toString());
        return buffer.toString();
    }

    /** Extracts text of text nodes and comment nodes from xml files * */
    private static void xml2txt(StringBuffer buffer, List content) {
        for (int i = 0; (content != null) && (i < content.size()); i++) {
            Object obj = content.get(i);

            if (obj instanceof Element) {
                Element elem = (Element) obj;
                xml2txt(buffer, elem.getContent());
            } else if (obj instanceof Text) {
                Text text = (Text) obj;
                buffer.append(text.getTextTrim()).append("\n\n");
            } else if (obj instanceof Comment) {
                Comment comm = (Comment) obj;
                buffer.append(comm.getText()).append("\n\n");
            }
        }
    }
}
