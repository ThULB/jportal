/*
 * 
 * $Revision: 14750 $ $Date: 2009-02-17 16:36:38 +0100 (Di, 17. Feb 2009) $
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

package org.mycore.common;

import static org.mycore.common.MCRConstants.DATE_FORMAT;
import static org.mycore.common.MCRConstants.DEFAULT_ENCODING;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class represent a general set of external methods to support the
 * programming API.
 * 
 * @author Jens Kupferschmidt
 * @author Frank L\u00fctzenkirchen
 * @author Thomas Scheffler (yagee)
 * 
 * @version $Revision: 14750 $ $Date: 2009-02-17 16:36:38 +0100 (Di, 17. Feb 2009) $
 */
public class MCRUtils {
    // The file slash
    private static String SLASH = System.getProperty("file.separator");;

    public final static char COMMAND_OR = 'O';

    public final static char COMMAND_AND = 'A';

    public final static char COMMAND_XOR = 'X';

    // public constant data
    private static final Logger LOGGER = Logger.getLogger(MCRUtils.class);

    // Language lists
    private static ArrayList<String> langlist = new ArrayList<String>();
    private static ArrayList<String> countrylist = new ArrayList<String>();
    
    /**
     * Load two static arrays for fast search of ISO-639/ISO-3166 strings.
     */
    static {
        StringBuffer sb;
        // add id as workaround
        langlist.add("id");
        countrylist.add("ID");
        // add codes from locale
        for ( Locale l : Locale.getAvailableLocales()) {
            sb = new StringBuffer(l.getLanguage());
            langlist.add(sb.toString());
            sb.append('-').append(l.getCountry());
            countrylist.add(sb.toString());
        }
    }
    
    /**
     * This method check the language string base on RFC 1766 to the supported
     * languages in mycore.
     * 
     * @param lang
     *            the language string
     * @return true if the language was supported, otherwise false
     */
    public static final boolean isSupportedLang(String lang) {
        if ((lang == null) || ((lang = lang.trim()).length() == 0)) {
            return false;
        }
        if (lang.startsWith("x-")) { return true; }
        if (langlist.contains(lang)) { return true; }
        if (countrylist.contains(lang)) { return true; }
        return false;
    }

    /**
     * The methode convert the input date string to the ISO output string. If
     * the input can't convert, the output is null.
     * 
     * @param indate
     *            the date input
     * @return the ISO output or null
     */
    public static final String covertDateToISO(String indate) {
        if ((indate == null) || ((indate = indate.trim()).length() == 0)) {
            return null;
        }

        GregorianCalendar calendar = new GregorianCalendar();
        boolean test = false;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setLenient(false);

        try {
            calendar.setTime(formatter.parse(indate));
            test = true;
        } catch (ParseException e) {
        }

        if (!test) {
            for (int i = 0; i < DATE_FORMAT.length; i++) {
                DateFormat df = DATE_FORMAT[i];
                df.setLenient(false);

                try {
                    calendar.setTime(df.parse(indate));
                    test = true;
                } catch (ParseException e) {
                }

                if (test) {
                    break;
                }
            }
        }

        if (!test) {
            return null;
        }

        formatter.setCalendar(calendar);

        return formatter.format(calendar.getTime());
    }

    /**
     * The methode convert the input date string to the GregorianCalendar. If
     * the input can't convert, the output is null.
     * 
     * @param indate
     *            the date input
     * @return the GregorianCalendar or null
     */
    public static final GregorianCalendar covertDateToGregorianCalendar(String indate) {
        if ((indate == null) || ((indate = indate.trim()).length() == 0)) {
            return null;
        }

        boolean era = true;
        int start = 0;

        if (indate.substring(0, 2).equals("AD")) {
            era = true;
            start = 2;
        }

        if (indate.substring(0, 2).equals("BC")) {
            era = false;
            start = 2;
        }

        GregorianCalendar calendar = new GregorianCalendar();
        boolean test = false;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        try {
            calendar.setTime(formatter.parse(indate.substring(start, indate.length())));

            if (!era) {
                calendar.set(Calendar.ERA, GregorianCalendar.BC);
            }

            test = true;
        } catch (ParseException e) {
        }

        if (!test) {
            for (int i = 0; i < DATE_FORMAT.length; i++) {
                DateFormat df = DATE_FORMAT[i];

                try {
                    calendar.setTime(df.parse(indate.substring(start, indate.length())));

                    if (!era) {
                        calendar.set(Calendar.ERA, GregorianCalendar.BC);
                    }

                    test = true;
                } catch (ParseException e) {
                }

                if (test) {
                    break;
                }
            }
        }

        if (!test) {
            return null;
        }

        return calendar;
    }

    /**
     * This methode replace any characters to XML entity references.
     * <p>
     * <ul>
     * <li>&lt; to &amp;lt;
     * <li>&gt; to &amp;gt;
     * <li>&amp; to &amp;amp;
     * <li>&quot; to &amp;quot;
     * <li>&apos; to &amp;apos;
     * </ul>
     * 
     * @param in
     *            a string
     * @return the converted string.
     */
    public static final String stringToXML(String in) {
        if (in == null) {
            return "";
        }

        StringBuffer sb = new StringBuffer(2048);

        for (int i = 0; i < in.length(); i++) {
            if (in.charAt(i) == '<') {
                sb.append("&lt;");

                continue;
            }

            if (in.charAt(i) == '>') {
                sb.append("&gt;");

                continue;
            }

            if (in.charAt(i) == '&') {
                sb.append("&amp;");

                continue;
            }

            if (in.charAt(i) == '\"') {
                sb.append("&quot;");

                continue;
            }

            if (in.charAt(i) == '\'') {
                sb.append("&apos;");

                continue;
            }

            sb.append(in.charAt(i));
        }

        return sb.toString();
    }

    /**
     * This method convert a JDOM tree to a byte array.
     * 
     * @param jdom
     *            the JDOM tree
     * @return a byte array of the JDOM tree
     */
    public static final byte[] getByteArray(org.jdom.Document jdom) throws MCRPersistenceException {
        MCRConfiguration conf = MCRConfiguration.instance();
        String mcr_encoding = conf.getString("MCR.Metadata.DefaultEncoding", DEFAULT_ENCODING);
        ByteArrayOutputStream outb = new ByteArrayOutputStream();

        try {
            XMLOutputter outp = new XMLOutputter(Format.getRawFormat().setEncoding(mcr_encoding));
            outp.output(jdom, outb);
        } catch (Exception e) {
            throw new MCRPersistenceException("Can't produce byte array.");
        }

        return outb.toByteArray();
    }

    /**
     * Converts an Array of Objects to an Array of Strings using the toString()
     * method.
     * 
     * @param objects
     *            Array of Objects to be converted
     * @return Array of Strings representing Objects
     */
    public static final String[] getStringArray(Object[] objects) {
        String[] returns = new String[objects.length];

        for (int i = 0; i < objects.length; i++)
            returns[i] = objects[i].toString();

        return returns;
    }

    /**
     * Converts an Array of Objects to an Array of Strings using the toString()
     * method.
     * 
     * @param objects
     *            Array of Objects to be converted
     * @param maxitems
     *            The maximum of items to convert
     * @return Array of Strings representing Objects
     */
    public static final String[] getStringArray(Object[] objects, int maxitems) {
        String[] returns = new String[maxitems];

        for (int i = 0; i < maxitems; i++)
            returns[i] = objects[i].toString();

        return returns;
    }

    /**
     * Copies all content read from the given input stream to the given output
     * stream. Note that this method will NOT close the streams when finished
     * copying.
     * 
     * @param source
     *            the InputStream to read the bytes from
     * @param target
     *            out the OutputStream to write the bytes to, may be null
     * @return true if Inputstream copied successfully to OutputStream
     */
    public static boolean copyStream(InputStream source, OutputStream target) {
        if (source == null) {
            throw new MCRException("InputStream source is null.");
        }
        
        try {
            // R E A D / W R I T E by chunks
            int chunkSize = 63 * 1024;

            // code will work even when chunkSize = 0 or chunks = 0;
            // Even for small files, we allocate a big buffer, since we
            // don't know the size ahead of time.
            byte[] ba = new byte[chunkSize];

            // keep reading till hit eof
            while (true) {
                int bytesRead = readBlocking(source, ba, 0, chunkSize);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(MCRUtils.class.getName() + ".copyStream(): " + bytesRead + " bytes read");
                }

                if (bytesRead > 0) {
                    if (target != null) {
                        target.write(ba, 0 /* offset in ba */, bytesRead /*
                                                                             * bytes
                                                                             * to
                                                                             * write
                                                                             */);
                    }
                } else {
                    break; // hit eof
                }
            } // end while

            // C L O S E, done by caller if wanted.
        } catch (IOException e) {
            LOGGER.debug("IOException caught while copying streams:");
            LOGGER.debug(e.getClass().getName() + ": " + e.getMessage());
            LOGGER.debug(e);
            return false;
        }

        // all was ok
        return true;
    } // end copy

    /**
     * Copies all content read from the given input stream to the given output
     * stream. Note that this method will NOT close the streams when finished
     * copying.
     * 
     * @param source
     *            the InputStream to read the bytes from
     * @param target
     *            out the OutputStream to write the bytes to, may be null
     * @return true if Inputstream copied successfully to OutputStream
     */
    public static boolean copyReader(Reader source, Writer target) {
        if (source == null) {
            throw new MCRException("Reader source is null.");
        }
        
        try {
            // R E A D / W R I T E by chunks
            int chunkSize = 63 * 1024;

            // code will work even when chunkSize = 0 or chunks = 0;
            // Even for small files, we allocate a big buffer, since we
            // don't know the size ahead of time.
            char[] ca = new char[chunkSize];

            // keep reading till hit eof
            while (true) {
                int charsRead = readBlocking(source, ca, 0, chunkSize);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(MCRUtils.class.getName() + ".copyReader(): " + charsRead + " characters read");
                }

                if (charsRead > 0) {
                    if (target != null) {
                        target.write(ca, 0 /* offset in ba */, charsRead /*
                                                                             * bytes
                                                                             * to
                                                                             * write
                                                                             */);
                    }
                } else {
                    break; // hit eof
                }
            } // end while

            // C L O S E, done by caller if wanted.
        } catch (IOException e) {
            return false;
        }

        // all was ok
        return true;
    } // end copy

    /**
     * merges to HashSets of MyCoreIDs after specific rules
     * 
     * @see #COMMAND_OR
     * @see #COMMAND_AND
     * @see #COMMAND_XOR
     * @param set1
     *            1st HashSet to be merged
     * @param set2
     *            2nd HashSet to be merged
     * @param operation
     *            available COMMAND_XYZ
     * @return merged HashSet
     */
    public static final <T> HashSet<T> mergeHashSets(HashSet<? extends T> set1, HashSet<? extends T> set2, char operation) {
        HashSet<T> merged = new HashSet<T>();
        T id;

        switch (operation) {
        case COMMAND_OR:
            merged.addAll(set1);
            merged.addAll(set2);

            break;

        case COMMAND_AND:

            for (Iterator<? extends T> it = set1.iterator(); it.hasNext();) {
                id = it.next();

                if (set2.contains(id)) {
                    merged.add(id);
                }
            }

            break;

        case COMMAND_XOR:

            for (Iterator<? extends T> it = set1.iterator(); it.hasNext();) {
                id = it.next();

                if (!set2.contains(id)) {
                    merged.add(id);
                }
            }

            for (Iterator<? extends T> it = set2.iterator(); it.hasNext();) {
                id = it.next();

                if (!set1.contains(id) && !merged.contains(id)) {
                    merged.add(id);
                }
            }

            break;

        default:
            throw new IllegalArgumentException("operation not permited: " + operation);
        }

        return merged;
    }

    /**
     * The method cut an ArrayList for a maximum of items.
     * 
     * @param arrayin The incoming ArrayList
     * @param maxitem The maximum number of items
     * @return the cutted ArrayList
     */
    public static final <T> ArrayList<T> cutArrayList(ArrayList<? extends T> arrayin, int maxitems) {
        if (arrayin == null) {
            throw new MCRException("Input ArrayList is null.");
        }

        if (maxitems < 1) {
            LOGGER.warn("The maximum items are lower then 1.");
        }

        ArrayList<T> arrayout = new ArrayList<T>();
        int i = 0;

        for (Iterator<? extends T> it = arrayin.iterator(); it.hasNext() && (i < maxitems); i++) {
            arrayout.add(it.next());
        }
        return arrayout;
    }

    /**
     * Reads exactly <code>len</code> bytes from the input stream into the
     * byte array. This method reads repeatedly from the underlying stream until
     * all the bytes are read. InputStream.read is often documented to block
     * like this, but in actuality it does not always do so, and returns early
     * with just a few bytes. readBlockiyng blocks until all the bytes are read,
     * the end of the stream is detected, or an exception is thrown. You will
     * always get as many bytes as you asked for unless you get an eof or other
     * exception. Unlike readFully, you find out how many bytes you did get.
     * 
     * @param b
     *            the buffer into which the data is read.
     * @param off
     *            the start offset of the data.
     * @param len
     *            the number of bytes to read.
     * @return number of bytes actually read.
     * @exception IOException
     *                if an I/O error occurs.
     * 
     */
    public static final int readBlocking(InputStream in, byte[] b, int off, int len) throws IOException {
        int totalBytesRead = 0;

        while (totalBytesRead < len) {
            int bytesRead = in.read(b, off + totalBytesRead, len - totalBytesRead);

            if (bytesRead < 0) {
                break;
            }

            totalBytesRead += bytesRead;
        }

        return totalBytesRead;
    } // end readBlocking

    /**
     * Reads exactly <code>len</code> bytes from the input stream into the
     * byte array. This method reads repeatedly from the underlying stream until
     * all the bytes are read. Reader.read is often documented to block like
     * this, but in actuality it does not always do so, and returns early with
     * just a few bytes. readBlockiyng blocks until all the bytes are read, the
     * end of the stream is detected, or an exception is thrown. You will always
     * get as many bytes as you asked for unless you get an eof or other
     * exception. Unlike readFully, you find out how many bytes you did get.
     * 
     * @param c
     *            the buffer into which the data is read.
     * @param off
     *            the start offset of the data.
     * @param len
     *            the number of bytes to read.
     * @return number of bytes actually read.
     * @exception IOException
     *                if an I/O error occurs.
     * 
     */
    public static final int readBlocking(Reader in, char[] c, int off, int len) throws IOException {
        int totalCharsRead = 0;

        while (totalCharsRead < len) {
            int charsRead = in.read(c, off + totalCharsRead, len - totalCharsRead);

            if (charsRead < 0) {
                break;
            }

            totalCharsRead += charsRead;
        }

        return totalCharsRead;
    } // end readBlocking

    /**
     * <p>
     * Returns String in with newStr substituted for find String.
     * 
     * @param in
     *            String to edit
     * @param find
     *            string to match
     * @param newStr
     *            string to substitude for find
     */
    public static String replaceString(String in, String find, String newStr) {
        char[] working = in.toCharArray();
        StringBuffer sb = new StringBuffer();

        int startindex = in.indexOf(find);

        if (startindex < 0) {
            return in;
        }

        int currindex = 0;

        while (startindex > -1) {
            for (int i = currindex; i < startindex; i++) {
                sb.append(working[i]);
            } // for

            currindex = startindex;
            sb.append(newStr);
            currindex += find.length();
            startindex = in.indexOf(find, currindex);
        } // while

        for (int i = currindex; i < working.length; i++) {
            sb.append(working[i]);
        } // for

        return sb.toString();
    }

    /**
     * The method wrap the org.jdom.Element in a org.jdom.Document and write it
     * to a file.
     * 
     * @param elm
     *            the JDOM Document
     * @param xml
     *            the File instance
     */
    public static final void writeElementToFile(Element elm, File xml) {
        writeJDOMToFile((new Document()).addContent(elm), xml);
    }

    /**
     * The method write a given JDOM Document to a file.
     * 
     * @param jdom
     *            the JDOM Document
     * @param xml
     *            the File instance
     */
    public static final void writeJDOMToFile(Document jdom, File xml) {
        try {
            XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(xml));
            xout.output(jdom, out);
            out.close();
        } catch (IOException ioe) {
            if (LOGGER.isDebugEnabled()) {
                ioe.printStackTrace();
            } else {
                LOGGER.error("Can't write org.jdom.Document to file "+xml.getName()+".");
            }
        }
    }

    /**
     * The method wrap the org.jdom.Element in a org.jdom.Document and write it
     * to Sysout.
     * 
     * @param elm
     *            the JDOM Document
     */
    public static final void writeElementToSysout(Element elm) {
        writeJDOMToSysout((new Document()).addContent(elm));
    }

    /**
     * The method write a given JDOM Document to the system output.
     * 
     * @param jdom
     *            the JDOM Document
     */
    public static final void writeJDOMToSysout(Document jdom) {
        try {
            XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
            BufferedOutputStream out = new BufferedOutputStream(System.out);
            xout.output(jdom, out);
            out.flush();
        } catch (IOException ioe) {
            if (LOGGER.isDebugEnabled()) {
                ioe.printStackTrace();
            } else {
                LOGGER.error("Can't write org.jdom.Document to Sysout.");
            }
        }
    }

    /**
     * The method return a list of all file names under the given directory and
     * subdirectories of itself.
     * 
     * @param basedir
     *            the File instance of the basic directory
     * @return an ArrayList with file names as pathes
     */
    public static ArrayList<String> getAllFileNames(File basedir) {
        ArrayList<String> out = new ArrayList<String>();
        File[] stage = basedir.listFiles();

        for (int i = 0; i < stage.length; i++) {
            if (stage[i].isFile()) {
                out.add(stage[i].getName());
            }

            if (stage[i].isDirectory()) {
                out.addAll(getAllFileNames(stage[i], stage[i].getName() + SLASH));
            }
        }

        return out;
    }

    /**
     * The method return a list of all file names under the given directory and
     * subdirectories of itself.
     * 
     * @param basedir
     *            the File instance of the basic directory
     * @param path
     *            the part of directory path
     * @return an ArrayList with file names as pathes
     */
    public static ArrayList<String> getAllFileNames(File basedir, String path) {
        ArrayList<String> out = new ArrayList<String>();
        File[] stage = basedir.listFiles();

        for (int i = 0; i < stage.length; i++) {
            if (stage[i].isFile()) {
                out.add(path + stage[i].getName());
            }

            if (stage[i].isDirectory()) {
                out.addAll(getAllFileNames(stage[i], path + stage[i].getName() + SLASH));
            }
        }

        return out;
    }

    /**
     * The method return a list of all directory names under the given directory
     * and subdirectories of itself.
     * 
     * @param basedir
     *            the File instance of the basic directory
     * @return an ArrayList with directory names as pathes
     */
    public static ArrayList<String> getAllDirectoryNames(File basedir) {
        ArrayList<String> out = new ArrayList<String>();
        File[] stage = basedir.listFiles();

        for (int i = 0; i < stage.length; i++) {
            if (stage[i].isDirectory()) {
                out.add(stage[i].getName());
                out.addAll(getAllDirectoryNames(stage[i], stage[i].getName() + SLASH));
            }
        }

        return out;
    }

    /**
     * The method return a list of all directory names under the given directory
     * and subdirectories of itself.
     * 
     * @param basedir
     *            the File instance of the basic directory
     * @param path
     *            the part of directory path
     * @return an ArrayList with directory names as pathes
     */
    public static ArrayList<String> getAllDirectoryNames(File basedir, String path) {
        ArrayList<String> out = new ArrayList<String>();
        File[] stage = basedir.listFiles();

        for (int i = 0; i < stage.length; i++) {
            if (stage[i].isDirectory()) {
                out.add(path + stage[i].getName());
                out.addAll(getAllDirectoryNames(stage[i], path + stage[i].getName() + SLASH));
            }
        }

        return out;
    }

    public static String arrayToString(Object[] objArray, String seperator) {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < objArray.length; i++) {
            buf.append(objArray[i]).append(seperator);
        }

        if (objArray.length > 0) {
            buf.setLength(buf.length() - seperator.length());
        }

        return buf.toString();
    }

    public static String parseDocumentType(InputStream in) {
        SAXParser parser = null;

        try {
            parser = SAXParserFactory.newInstance().newSAXParser();
        } catch (Exception ex) {
            String msg = "Could not build a SAX Parser for processing XML input";
            throw new MCRConfigurationException(msg, ex);
        }

        final Properties detected = new Properties();
        final String forcedInterrupt = "mcr.forced.interrupt";

        DefaultHandler handler = new DefaultHandler() {
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                LOGGER.debug("MCRLayoutService detected root element = " + qName);
                detected.setProperty("docType", qName);
                throw new MCRException(forcedInterrupt);
            }

            // We would need SAX 2.0 to be able to do this, for later use:
            public void startDTD(String name, String publicId, String systemId) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(new StringBuffer(1024).append("MCRUtils detected DOCTYPE declaration = ").append(name).append(" publicId = ").append(publicId).append(" systemId = ").append(systemId).toString());
                }
                detected.setProperty("docType", name);
                throw new MCRException(forcedInterrupt);
            }
        };

        try {
            parser.parse(new InputSource(in), handler);
        } catch (Exception ex) {
            if (!forcedInterrupt.equals(ex.getMessage())) {
                String msg = "Error while detecting XML document type from input source";
                throw new MCRException(msg, ex);
            }
        }

        return detected.getProperty("docType");
    }

}
