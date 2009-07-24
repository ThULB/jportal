/*
 * $RCSfile$
 * $Revision: 473 $ $Date: 2008-05-28 15:33:33 +0200 (Wed, 28 May 2008) $
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

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.frontend.cli.MCRDerivateCommands;

public class MCRJPortalCLIServlet extends MCRServlet {
    private static final long serialVersionUID = 1L;

    private static Logger LOGGER = Logger.getLogger(MCRJPortalCLIServlet.class);;

    // private final static int derStartNumber = 18260;

    public void init() throws ServletException {
        super.init();
    }

    public void doGetPost(MCRServletJob job) throws IOException {
        MCRSession session = MCRSessionMgr.getCurrentSession();
        String user = session.getCurrentUserID();
        if (user.equals("root")) {
            LOGGER.info("#########################################################");
            LOGGER.info("'Started.");
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

        //MCRObjectCommands.repairMetadataSearch("person");
        //MCRObjectCommands.repairMetadataSearch("jpinst");
        //MCRObjectCommands.repairMetadataSearch("jpjournal");
        //MCRObjectCommands.repairMetadataSearch("jpvolume");
        //MCRObjectCommands.repairMetadataSearch("jparticle");
        
        String derFolder = "/mcr/jp/migration/generic/tools/migration/eisenberger_nb/results/derivates/";
        //jportal_derivate_00066214.xml
        
        for (int i = 66214; i < 77394; i++) {
            String fileLoc = derFolder + "jportal_derivate_000" + Integer.toString(i) + ".xml";
            MCRDerivateCommands.loadFromFile(fileLoc);
            LOGGER.debug("############################# load derivate from "+fileLoc);
        }
    }

    private final Element getAnswerXML(boolean allowed4Action) {
        String tn = Thread.currentThread().getName();
        Element xml = new Element("cliRoot");
        String tag = "requestExecuted";
        xml.addContent(new Element(tag));

        if (allowed4Action) {
            xml.getChild(tag).setText("yes, watch your web log to see what happens currently (Thread: [" + tn + "]) !");
        } else {
            xml.getChild(tag).setText("no, permission does not exist !");
        }
        return xml;
    }
}
