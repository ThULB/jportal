/**
 * 
 * $Revision: 13567 $ $Date: 2008-06-04 14:27:47 +0200 (Mi, 04. Jun 2008) $
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

package org.mycore.datamodel.metadata;

import com.ibm.icu.util.GregorianCalendar;

import org.mycore.common.MCRCalendar;
import org.mycore.common.MCRTestCase;

/**
 * This class is a JUnit test case for org.mycore.datamodel.metadata.MCRMetaHistoryDate.
 * 
 * @author Jens Kupferschmidt
 * @version $Revision: 13567 $ $Date: 2008-06-04 14:27:47 +0200 (Mi, 04. Jun 2008) $
 *
 */
public class MCRMetaHistoryDateTest extends MCRTestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * Test method for 'org.mycore.datamodel.metadata.MCRMetaHistoryDate.setFromDOM(Element)'
     */
    public void testSetFromDOM() {

    }

    /*
     * Test method for 'org.mycore.datamodel.metadata.MCRMetaHistoryDate.createXML()'
     */
    public void testCreateXML() {

    }

    /*
     * Test method for 'org.mycore.datamodel.metadata.MCRMetaHistoryDate.setVonDate(GregorianCalendar)'
     */
    public void testSetVonDateGregorianCalendar() {
        MCRMetaHistoryDate hd = new MCRMetaHistoryDate();
        hd.setVonDate(new GregorianCalendar(1964,1,24));
        assertEquals("Von value is not 24.02.1964 AD","24.02.1964 AD",hd.getVonToGregorianString());
    }

    /*
     * Test method for 'org.mycore.datamodel.metadata.MCRMetaHistoryDate.setVonDate(String)'
     */
    public void testSetVonDateString() {
        MCRMetaHistoryDate hd = new MCRMetaHistoryDate();
        hd.setVonDate("24.02.1964",MCRCalendar.TAG_GREGORIAN);
        assertEquals("Von value is not 24.02.1964 AD","24.02.1964 AD",hd.getVonToGregorianString());
    }

    /*
     * Test method for 'org.mycore.datamodel.metadata.MCRMetaHistoryDate.setBisDate(GregorianCalendar)'
     */
    public void testSetBisDateGregorianCalendar() {
        MCRMetaHistoryDate hd = new MCRMetaHistoryDate();
        hd.setBisDate(new GregorianCalendar(1964,1,24));
        assertEquals("Bis value is not 24.02.1964 AD","24.02.1964 AD",hd.getBisToGregorianString());
    }

    /*
     * Test method for 'org.mycore.datamodel.metadata.MCRMetaHistoryDate.setBisDate(String)'
     */
    public void testSetBisDateString() {
        MCRMetaHistoryDate hd = new MCRMetaHistoryDate();
        hd.setBisDate("24.02.1964",MCRCalendar.TAG_GREGORIAN);
        assertEquals("Bis value is not 24.02.1964 AD","24.02.1964 AD",hd.getBisToGregorianString());
    }

    /*
     * Test method for 'org.mycore.datamodel.metadata.MCRMetaHistoryDate.debug()'
     */
    public void testDebug() {
        MCRMetaHistoryDate hd = new MCRMetaHistoryDate();
        hd.setVonDate("05.10.1582",MCRCalendar.TAG_GREGORIAN);
        hd.setBisDate("15.10.1582",MCRCalendar.TAG_GREGORIAN);
        hd.debug();
    }
}
