/*
 * $RCSfile: MCRConfiguration.java,v $
 * $Revision: 1.25 $ $Date: 2005/09/02 14:26:23 $
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

import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Frank L�tzenkirchen
 */
class MCRBrowseRequest {
    String index;

    int from = 1;

    int to = Integer.MAX_VALUE;

    StringBuffer path;

    String search;

    String mode;

    MCRBrowseRequest(HttpServletRequest req) {
        StringTokenizer st = new StringTokenizer(req.getPathInfo(), "/-");

        if (!st.hasMoreTokens()) {
            throw new RuntimeException();
        }

        index = st.nextToken();

        path = new StringBuffer(index);
        path.append("/");

        while (st.countTokens() > 1)
            addRange(st.nextToken(), st.nextToken());

        search = req.getParameter("search");
        mode = req.getParameter("mode");
    }

    void addRange(String from, String to) {
        this.from = Integer.parseInt(from);
        this.to = Integer.parseInt(to);
        path.append(this.from);
        path.append("-");
        path.append(this.to);
        path.append("/");
    }

    String getCanonicalRequestPath() {
        return "index/" + path.toString();
    }

    int getFrom() {
        return from;
    }

    int getTo() {
        return to;
    }

    String getIndex() {
        return index;
    }
}
