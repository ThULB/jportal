/*
 * $RCSfile: MCRMyCommand.java,v $
 * $Revision: 1.5 $ $Date: 2005/09/28 08:00:29 $
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

package org.mycore.frontend.cli;

import org.apache.log4j.Logger;

/**
 * This class implements a Transformer form the Uni Leipzig DOL data to a MyCoRe
 * XML data objects.
 * 
 * @author Jens Kupferschmidt
 * @version $Revision: 1.5 $ $Date: 2005/09/28 08:00:29 $
 */
public class MCRMyCommand extends MCRAbstractCommands {
    // logger
    static Logger logger = Logger.getLogger(MCRMyCommand.class.getName());

    /**
     * The empty constructor.
     */
    public MCRMyCommand() {
        super();

        MCRCommand com = null;

        com = new MCRCommand("convert XML from directory {0} to directory {1} as ID {3}", "org.mycore.frontend.cli.MCRMyCommand.convertData String String String", "The command is a dummy sample.");
        command.add(com);
    }

    /**
     * The method is a sample.
     * 
     * @param indir
     *            the first parameter
     * @param outdir
     *            the second parameter
     * @param ID
     *            the third parameter
     */
    public static void convertData(String indir, String outdir, String ID) {
        logger.info("Transformer from XML");
        logger.info("====================");

        logger.info("Do something.");

        logger.info("Ready.");
        logger.info("");
    }
}
