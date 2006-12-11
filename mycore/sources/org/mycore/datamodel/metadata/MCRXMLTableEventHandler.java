/*
 * $RCSfile: MCRXMLTableEventHandler.java,v $
 * $Revision: 1.7 $ $Date: 2006/02/10 23:33:48 $
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

package org.mycore.datamodel.metadata;

import java.util.ArrayList;

import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;

/**
 * This class manages all operations of the XMLTables for operations of an
 * object or derivate.
 * 
 * @author Jens Kupferschmidt
 */
public class MCRXMLTableEventHandler extends MCREventHandlerBase {

    static MCRXMLTableManager mcr_xmltable = MCRXMLTableManager.instance();

    /**
     * This method add the data to SQL table of XML data via MCRXMLTableManager.
     * 
     * @param evt
     *            the event that occured
     * @param obj
     *            the MCRObject that caused the event
     */
    protected final void handleObjectCreated(MCREvent evt, MCRObject obj) {
        org.jdom.Document doc = obj.createXML();
        mcr_xmltable.create(obj.getId(), doc);
    }

    /**
     * This method update the data to SQL table of XML data via
     * MCRXMLTableManager.
     * 
     * @param evt
     *            the event that occured
     * @param obj
     *            the MCRObject that caused the event
     */
    protected final void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        mcr_xmltable.delete(obj.getId());
        handleObjectCreated(evt, obj);
    }

    /**
     * This method delete the XML data from SQL table data via
     * MCRXMLTableManager.
     * 
     * @param evt
     *            the event that occured
     * @param obj
     *            the MCRObject that caused the event
     */
    protected final void handleObjectDeleted(MCREvent evt, MCRObject obj) {
        mcr_xmltable.delete(obj.getId());
    }

    /**
     * This method receive the XML data from SQL table data via
     * MCRXMLTableManager and put it in the MCREvent instance.
     * 
     * @param evt
     *            the event that occured
     * @param objid
     *            the MCRObjectID that caused the event
     */
    protected final void handleObjectReceived(MCREvent evt, MCRObjectID objid) {
        byte[] xml = mcr_xmltable.retrieve(objid);
        if (xml == null)
            return;
        evt.put("xml", xml);
    }

    /**
     * This method check exist of the XML data from SQL table data via
     * MCRXMLTableManager and put the boolean result in the MCREvent instance as
     * entry 'exist'.
     * 
     * @param evt
     *            the event that occured
     * @param objid
     *            the MCRObjectID that caused the event
     */
    protected final void handleObjectExist(MCREvent evt, MCRObjectID objid) {
        boolean res = mcr_xmltable.exist(objid);
        evt.put("exist", Boolean.toString(res));
    }

    /**
     * This method put all ID's of the XML data from SQL table data via
     * MCRXMLTableManager in an ArryList and put it in the MCREvent instance as
     * entry 'objectIDs'.
     * 
     * @param evt
     *            the event that occured
     * @param objtype
     *            the MCR type that caused the event
     */
    protected final void handleObjectListIDs(MCREvent evt, String objtype) {
        ArrayList ar = mcr_xmltable.retrieveAllIDs(objtype);
        evt.put("objectIDs", ar);
    }

    /**
     * This method add the data to SQL table of XML data via MCRXMLTableManager.
     * 
     * @param evt
     *            the event that occured
     * @param der
     *            the MCRDerivate that caused the event
     */
    protected final void handleDerivateCreated(MCREvent evt, MCRDerivate der) {
        org.jdom.Document doc = der.createXML();
        mcr_xmltable.create(der.getId(), doc);
    }

    /**
     * This method update the data to SQL table of XML data via
     * MCRXMLTableManager.
     * 
     * @param evt
     *            the event that occured
     * @param der
     *            the MCRObject that caused the event
     */
    protected final void handleDerivateUpdated(MCREvent evt, MCRDerivate der) {
        mcr_xmltable.delete(der.getId());
        handleDerivateCreated(evt, der);
    }

    /**
     * This method delete the XML data from SQL table data via
     * MCRXMLTableManager.
     * 
     * @param evt
     *            the event that occured
     * @param der
     *            the MCRObject that caused the event
     */
    protected final void handleDerivateDeleted(MCREvent evt, MCRDerivate der) {
        mcr_xmltable.delete(der.getId());
    }

    /**
     * This method receive the XML data from SQL table data via
     * MCRXMLTableManager and put it in the MCREvent instance.
     * 
     * @param evt
     *            the event that occured
     * @param objid
     *            the MCRObjectID that caused the event
     */
    protected final void handleDerivateReceived(MCREvent evt, MCRObjectID objid) {
        byte[] xml = mcr_xmltable.retrieve(objid);
        if (xml == null)
            return;
        evt.put("xml", xml);
    }

    /**
     * This method check exist of the XML data from SQL table data via
     * MCRXMLTableManager and put the boolean result in the MCREvent instance.
     * 
     * @param evt
     *            the event that occured
     * @param objid
     *            the MCRObjectID that caused the event
     */
    protected final void handleDerivateExist(MCREvent evt, MCRObjectID objid) {
        boolean res = mcr_xmltable.exist(objid);
        evt.put("exist", Boolean.toString(res));
    }

    /**
     * This method put all ID's of the XML data from SQL table data via
     * MCRXMLTableManager in an ArryList and put it in the MCREvent instance as
     * entry 'derivateIDs'.
     * 
     * @param evt
     *            the event that occured
     * @param dertype
     *            the MCR type that caused the event
     */
    protected final void handleDerivateListIDs(MCREvent evt, String dertype) {
        ArrayList ar = mcr_xmltable.retrieveAllIDs(dertype);
        evt.put("derivateIDs", ar);
    }

}
