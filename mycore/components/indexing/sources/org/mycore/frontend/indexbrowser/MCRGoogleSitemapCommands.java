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

package org.mycore.frontend.indexbrowser;

import java.io.File;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.mycore.common.MCRUtils;
import org.mycore.frontend.cli.MCRAbstractCommands;
import org.mycore.frontend.cli.MCRCommand;

/**
 * This class builds a google sitemap containing links to all documents and
 * store them to the webapps directory. This can be configured with property
 * variable MCR.GoogleSitemap.Directory. The web.xml file should contain a
 * mapping to /sitemap.xml See
 * http://www.google.com/webmasters/sitemaps/docs/en/protocol.html
 * 
 * @author Frank Luetzenkirchen
 * @author Jens Kupferschmidt
 * @author Thomas Scheffler (yagee)
 * @version $Revision: 13085 $ $Date: 2008-02-06 18:27:24 +0100 (Mi, 06 Feb 2008) $
 */
public final class MCRGoogleSitemapCommands extends MCRAbstractCommands {

    /** The logger */
    private static Logger LOGGER = Logger.getLogger(MCRGoogleSitemapCommands.class.getName());

    /**
     * The empty constructor.
     */
    public MCRGoogleSitemapCommands() {
        super();

        MCRCommand com = null;

        com = new MCRCommand("build google sitemap", "org.mycore.frontend.indexbrowser.MCRGoogleSitemapCommands.buildSitemap", "Create the google sitemap(s) in the webapps directory.");
        command.add(com);
    }

    /**
     * The build and store method.
     */
    public static final void buildSitemap() throws Exception {
        // check time
        LOGGER.debug("Build Google sitemap start.");
        final long start = System.currentTimeMillis();
        // init
        MCRGoogleSitemapCommon common = new MCRGoogleSitemapCommon();
        // remove old files
        common.removeSitemapFiles();
        // compute number of files
        int number = common.checkSitemapFile();
        LOGGER.debug("Build Google number of URL files "+Integer.toString(number)+".");
        if (number == 1) {
            String fn = common.getFileName(1,true);
            File xml = new File(fn);
            Document jdom = common.buildSitemap();
            LOGGER.info("Write Google sitemap file " + fn + ".");
            MCRUtils.writeJDOMToFile(jdom, xml);
        } else {
            String fn = common.getFileName(1,true);
            File xml = new File(fn);
            Document jdom = common.buildSitemapIndex(number);
            LOGGER.info("Write Google sitemap file " + fn + ".");
            MCRUtils.writeJDOMToFile(jdom, xml);
            for (int i = 0; i < number; i++) {
                fn = common.getFileName(i+2,true);
                xml = new File(fn);
                jdom = common.buildSitemap(i);
                LOGGER.info("Write Google sitemap file " + fn + ".");
                MCRUtils.writeJDOMToFile(jdom, xml);
            }
        }
        // check time
        LOGGER.debug("Google sitemap request took " + (System.currentTimeMillis() - start) + "ms.");
    }

}
