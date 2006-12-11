/*
 * $RCSfile: PluginLister.java,v $
 * $Revision: 1.4 $ $Date: 2005/09/28 08:00:29 $
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

package org.mycore.sample;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import org.mycore.common.MCRConfiguration;
import org.mycore.services.plugins.TextFilterPlugin;
import org.mycore.services.plugins.TextFilterPluginManager;

/**
 * @author Thomas Scheffler (yagee)
 * 
 * This class is to test Plugin support of mycore At first it just tries to
 * invoke the FilterPluginManager to load all available Plugins an then print
 * out their names and infos
 * 
 */
public class PluginLister {
    TextFilterPlugin filter;

    TextFilterPluginManager pMan;

    Collection pBag;

    /**
     * 
     */
    public PluginLister() {
        super();
        pMan = TextFilterPluginManager.getInstance();
        pBag = pMan.getPlugins();
        System.out.println("\n\n==================================\n" + "Yagee's magic MyCoRe PluginLister:\n" + "==================================\n\n");
    }

    public void listPlugins() {
        Iterator iterator = pBag.iterator();

        while (iterator.hasNext()) {
            filter = (TextFilterPlugin) iterator.next();
            System.out.println("Plugin name: " + filter.getName() + " v:" + filter.getMajorNumber() + "." + filter.getMinorNumber() + "\n_______________________________________________________________________________");
            System.out.println(filter.getInfo() + "\n\n");
        }
    }

    public static void main(String[] args) {
        PluginLister pLister = new PluginLister();
        pLister.listPlugins();

        TextFilterPluginManager pMan = pLister.pMan;
        File srcDir = new File(MCRConfiguration.instance().getString("MCR.PluginDirectory"));

        if (!srcDir.exists()) {
            System.err.println("No MCR.PluginDirectory exists");
            System.exit(1);
        }
    }
}
