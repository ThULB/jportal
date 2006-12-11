/**
 * $RCSfile: EditorItemComparator.java,v $
 * $Revision: 1.2 $ $Date: 2006/11/24 15:39:10 $
 *
 * This file is part of ** M y C o R e **
 * Visit our homepage at http://www.mycore.de/ for details.
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
 * along with this program, normally in the file license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 *
 **/
package org.mycore.datamodel.classifications.query;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;
import org.jdom.Namespace;

import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;

public class EditorItemComparator implements Comparator<Element> {
    
    public static final EditorItemComparator CURRENT_LANG_TEXT_ORDER=new EditorItemComparator();

    private EditorItemComparator() {
        super();
    }

    public int compare(Element o1, Element o2) {
        if (!((o1.getName().equals("item"))&&(o2.getName().equals("item")))){
            //NO Editor Items
            return 0;
        }
        return String.CASE_INSENSITIVE_ORDER.compare(getCurrentLangLabel(o1),getCurrentLangLabel(o2));
    }
    
    @SuppressWarnings("unchecked")
    private static String getCurrentLangLabel(Element item){
        MCRSession session=MCRSessionMgr.getCurrentSession();
        String currentLang=session.getCurrentLanguage();
        List<Element> labels=item.getChildren("label");
        Iterator<Element> it=labels.iterator();
        while (it.hasNext()){
            Element label=it.next();
            if (label.getAttributeValue("lang",Namespace.XML_NAMESPACE).equals(currentLang)){
                return label.getText();
            }
        }
        if (labels.size()>0){
            //fallback to first label if currentLang label is not found
            return labels.get(0).getText();
        }
        return "";
    }

}
