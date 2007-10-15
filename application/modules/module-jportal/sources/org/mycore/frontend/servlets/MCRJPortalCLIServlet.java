/*
 * $RCSfile$
 * $Revision$ $Date$
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

package org.mycore.frontend.servlets;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.metadata.MCRXMLTableManager;
import org.mycore.services.imaging.MCRImgCacheCommands;

public class MCRJPortalCLIServlet extends MCRServlet {
    private static final long serialVersionUID = 1L;

    private static Logger LOGGER = Logger.getLogger(MCRJPortalCLIServlet.class);;

    private final static int derStartNumber = 19476;

    public void init() throws ServletException {
        super.init();
    }

    public void doGetPost(MCRServletJob job) throws IOException {
        MCRSession session = MCRSessionMgr.getCurrentSession();
        String user = session.getCurrentUserID();
        if (user.equals("root")) {
            LOGGER.info("#########################################################");
            LOGGER.info("'Create Image cache' has involced by MCRJPortalCLIServlet");
            LOGGER.info("#########################################################");
            Document answerXML = new Document(getAnswerXML(true));
            getLayoutService().sendXML(job.getRequest(), job.getResponse(), answerXML);
            executeCommand();
        } else {
            Document answerXML = new Document(getAnswerXML(false));
            getLayoutService().sendXML(job.getRequest(), job.getResponse(), answerXML);
        }
    }

    private void executeCommand() {

        MCRXMLTableManager xmlTableManager = MCRXMLTableManager.instance();
        List derivateList = xmlTableManager.retrieveAllIDs("derivate");

        for (Iterator it = derivateList.iterator(); it.hasNext();) {
            String derivateID = (String) it.next();
            int derNumber = Integer.parseInt(derivateID.substring(17, 25));
            if (derNumber < derStartNumber) {
                LOGGER.info("NOTHING done - Derivate " + derivateID + " is already image cached\n");
            } else {
                try {
                    LOGGER.info("Caching Derivate " + derivateID);
                    MCRImgCacheCommands.cacheDeriv(derivateID);
                    LOGGER.info("\n\n Creating image cache for derivate " + derivateID + " completed successfull!\n");
                } catch (MCRException ex) {
                    LOGGER.error(ex.getMessage());
                    LOGGER.error("");
                    LOGGER.info("\n\n Creating image cache for derivate " + derivateID + " failed, will be skipped!\n");
                } catch (Exception e) {
                    LOGGER.error(e);
                    LOGGER.info("\n\n Creating image cache for derivate " + derivateID + " failed, will be skipped!\n");
                }
            }
        }
        LOGGER.info("\n\n Creating image cache for all derivates completed successfull!\n");
    }

    private final Element getAnswerXML(boolean allowed4Action) {
        Element xml = new Element("cliRoot");
        String tag = "requestExecuted";
        xml.addContent(new Element(tag));
        if (allowed4Action) {
            xml.getChild(tag).setText("yes, watch your web log to see what happens currently !");
        } else {
            xml.getChild(tag).setText("no, permission does not exist !");
        }
        return xml;
    }
}