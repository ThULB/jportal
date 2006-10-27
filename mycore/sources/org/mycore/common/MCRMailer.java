/*
 * $RCSfile: MCRMailer.java,v $
 * $Revision: 1.16 $ $Date: 2006/04/11 12:34:39 $
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

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.URLDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * This class provides methods to send emails from within a MyCoRe application.
 * 
 * @author Marc Schluepmann
 * @author Frank L�tzenkirchen
 * @author Werner Gre�hoff
 * 
 * @version $Revision: 1.16 $ $Date: 2006/04/11 12:34:39 $
 */
public class MCRMailer {
    /** Logger */
    static Logger logger = Logger.getLogger(MCRMailer.class);

    protected static Session mailSession;
    
    protected static String encoding;

    /** Initializes the class */
    static {
        MCRConfiguration config = MCRConfiguration.instance();
        encoding = config.getString( "MCR.mail.encoding", "ISO-8859-1" );
        
        Properties mailProperties = new Properties();

        try {
            mailProperties.setProperty("mail.smtp.host", config.getString("MCR.mail.server"));
            mailProperties.setProperty("mail.transport.protocol", config.getString("MCR.mail.protocol", "smtp"));
            mailSession = Session.getDefaultInstance(mailProperties, null);
            mailSession.setDebug(config.getBoolean("MCR.mail.debug", false));
        } catch (MCRConfigurationException mcrx) {
            String msg = "Missing email configuration data.";
            logger.fatal(msg, mcrx);
        }
    }

    /**
     * This method sends a simple plaintext email with the given parameters.
     * 
     * @param sender
     *            the sender of the email
     * @param recipient
     *            the recipient of the email
     * @param subject
     *            the subject of the email
     * @param body
     *            the textbody of the email
     */
    public static void send(String sender, String recipient, String subject, String body) {
        logger.debug("Called plaintext send method with single recipient.");

        ArrayList recipients = new ArrayList();
        recipients.add(recipient);
        send(sender, null, recipients, null, subject, body, null);
    }

    /**
     * This method sends a simple plaintext email to more than one recipient. If
     * flag BCC is true, the sender will also get the email as BCC recipient.
     * 
     * @param sender
     *            the sender of the email
     * @param recipients
     *            the recipients of the email as a List of Strings
     * @param subject
     *            the subject of the email
     * @param body
     *            the textbody of the email
     * @param bcc
     *            if true, sender will also get a copy as cc recipient
     */
    public static void send(String sender, List recipients, String subject, String body, boolean bcc) {
        logger.debug("Called plaintext send method with multiple recipients.");

        List bccList = null;

        if (bcc) {
            bccList = new ArrayList();
            bccList.add(sender);
        }

        send(sender, null, recipients, bccList, subject, body, null);
    }

    /**
     * This method sends a multipart email with the given parameters.
     * 
     * @param sender
     *            the sender of the email
     * @param recipient
     *            the recipient of the email
     * @param subject
     *            the subject of the email
     * @param parts
     *            a List of URL strings which should be added as parts
     * @param body
     *            the textbody of the email
     */
    public static void send(String sender, String recipient, String subject, String body, List parts) {
        logger.debug("Called multipart send method with single recipient.");

        ArrayList recipients = new ArrayList();
        recipients.add(recipient);
        send(sender, null, recipients, null, subject, body, parts);
    }

    /**
     * This method sends a multipart email to more than one recipient. If flag
     * BCC is true, the sender will also get the email as BCC recipient.
     * 
     * @param sender
     *            the sender of the email
     * @param recipients
     *            the recipients of the email as a List of Strings
     * @param subject
     *            the subject of the email
     * @param body
     *            the textbody of the email
     * @param parts
     *            a List of URL strings which should be added as parts
     * @param bcc
     *            if true, sender will also get a copy as bcc recipient
     */
    public static void send(String sender, List recipients, String subject, String body, List parts, boolean bcc) {
        logger.debug("Called multipart send method with multiple recipients.");

        List bccList = null;

        if (bcc) {
            bccList = new ArrayList();
            bccList.add(sender);
        }

        send(sender, null, recipients, bccList, subject, body, parts);
    }

    /**
     * Send email from a given XML document. See the sample mail below:
     * 
     * <email><from>bingo@bongo.com</from>
     *   <to>jim.knopf@lummerland.de</to>
     *   <bcc>frau.waas@lummerland.de</bcc>
     *   <subject>Gr��e aus der Stadt der Drachen</subject>
     *   <body>Es ist recht bew�lkt. Alles Gute, Jim.</body>
     *   <part>http://upload.wikimedia.org/wikipedia/de/f/f7/JimKnopf.jpg</part>
     * </email>
     *
     * @param email the email as JDOM element.
     */
    public static void send(Element email) {
        String from = email.getChildTextTrim("from");

        List rptList = email.getChildren("replyTo");
        List replyTo = new ArrayList();

        for (int i = 0; i < rptList.size(); i++)
            replyTo.add(((Element) rptList.get(i)).getTextTrim());

        List toList = email.getChildren("to");
        List to = new ArrayList();

        for (int i = 0; i < toList.size(); i++)
            to.add(((Element) toList.get(i)).getTextTrim());

        List bccList = email.getChildren("bcc");
        List bcc = new ArrayList();

        for (int i = 0; i < bccList.size(); i++)
            bcc.add(((Element) bccList.get(i)).getTextTrim());

        String subject = email.getChildTextTrim("subject");
        String body = email.getChildTextTrim("body");

        List partsList = email.getChildren("part");
        List parts = new ArrayList();

        for (int i = 0; i < partsList.size(); i++)
            parts.add(((Element) partsList.get(i)).getTextTrim());

        send(from, replyTo, to, bcc, subject, body, parts);
    }

    /**
     * Sends email.
     * 
     * @param from
     *            the sender of the email
     * @param replyTo
     *            the reply-to addresses as a List of Strings, may be null
     * @param to
     *            the recipients of the email as a List of Strings
     * @param bcc
     *            the bcc recipients of the email as a List of Strings, may be
     *            null
     * @param subject
     *            the subject of the email
     * @param body
     *            the text of the email
     * @param parts
     *            a List of URL strings which should be added as parts, may be
     *            null
     */
    public static void send(String from, List replyTo, List to, List bcc, String subject, String body, List parts) {
        try {
            MimeMessage msg = new MimeMessage(mailSession);
            msg.setFrom(buildAddress(from));

            if (replyTo != null) {
                InternetAddress[] adrs = new InternetAddress[replyTo.size()];

                for (int i = 0; i < replyTo.size(); i++)
                    adrs[i] = buildAddress((String) (replyTo.get(i)));

                msg.setReplyTo(adrs);
            }

            for (int i = 0; i < to.size(); i++)
                msg.addRecipient(Message.RecipientType.TO, buildAddress((String) to.get(i)));

            if (bcc != null) {
                for (int i = 0; i < bcc.size(); i++)
                    msg.addRecipient(Message.RecipientType.BCC, buildAddress((String) bcc.get(i)));
            }

            msg.setSentDate(new Date());
            msg.setSubject( subject, encoding );

            if ((parts == null) || (parts.size() == 0)) {
                msg.setText( body, encoding );
            } else {
                // Create the message part
                MimeBodyPart messagePart = new MimeBodyPart();

                // Fill the message
                messagePart.setText( body, encoding );

                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(messagePart);

                for (int i = 0; i < parts.size(); i++) {
                    messagePart = new MimeBodyPart();

                    DataSource source = new URLDataSource(new URL((String) parts.get(i)));
                    messagePart.setDataHandler(new DataHandler(source));
                    multipart.addBodyPart(messagePart);
                }

                // Put parts in message
                msg.setContent(multipart);
            }

            Transport.send(msg);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new MCRException("Email could not be sent.", ex);
        }
    }

    /**
     * Builds email address from a string. The string may be a single email
     * address or a combination of a personal name and address, like 
     * "John Doe" <john@doe.com>
     */
    private static InternetAddress buildAddress(String s) throws Exception {
        if (!s.endsWith(">")) {
            return new InternetAddress(s.trim());
        }

        String name = s.substring(0, s.lastIndexOf("<")).trim();
        String addr = s.substring(s.lastIndexOf("<") + 1, s.length() - 1).trim();
        
        // Name must be quoted if it contains umlauts or special characters
        if( ! name.startsWith( "\"" ) ) name = "\"" + name;
        if( ! name.endsWith( "\"" ) ) name = name + "\"";

        return new InternetAddress(addr, name);
    }
}
