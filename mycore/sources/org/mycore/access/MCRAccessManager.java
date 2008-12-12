/*
 * 
 * $Revision: 14106 $ $Date: 2008-10-09 11:30:08 +0200 (Do, 09. Okt 2008) $
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
package org.mycore.access;

import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;

import org.mycore.access.strategies.MCRAccessCheckStrategy;
import org.mycore.access.strategies.MCRObjectIDStrategy;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRException;
import org.mycore.datamodel.common.MCRLinkTableManager;
import org.mycore.datamodel.metadata.MCRObjectID;


/**
 * 
 * @author Thomas Scheffler
 * 
 * @version $Revision: 14106 $ $Date: 2008-10-09 11:30:08 +0200 (Do, 09. Okt 2008) $
 */
public class MCRAccessManager {

    private static final MCRAccessInterface ACCESS_IMPL = (MCRAccessInterface) MCRConfiguration.instance().getSingleInstanceOf("MCR.Access.Class",
            MCRAccessBaseImpl.class.getName());

    private static final MCRAccessCheckStrategy ACCESS_STRATEGY = (MCRAccessCheckStrategy) MCRConfiguration.instance().getInstanceOf(
            "MCR.Access.Strategy.Class", MCRObjectIDStrategy.class.getName());

    public static final Logger LOGGER = Logger.getLogger(MCRAccessManager.class);

    public static MCRAccessInterface getAccessImpl() {
        return ACCESS_IMPL;
    }

    /**
     * adds an access rule for an MCRObjectID to an access system.
     * 
     * @param id
     *            the MCRObjectID of the object
     * @param permission
     *            the access permission for the rule
     * @param rule
     *            the access rule
     * @param description
     *            description for the given access rule, e.g. "allows public access"
     * @throws MCRException
     *             if an errow was occured
     * @see MCRAccessInterface#addRule(String, String, org.jdom.Element, String)
     */
    public static void addRule(MCRObjectID id, String permission, org.jdom.Element rule, String description) throws MCRException {
        getAccessImpl().addRule(id.getId(), permission, rule, description);
    }

    /**
     * adds an access rule for an ID to an access system.
     * 
     * @param id
     *            the ID of the object as String
     * @param permission
     *            the access permission for the rule
     * @param rule
     *            the access rule
     * @param description
     *            description for the given access rule, e.g. "allows public access"
     * @throws MCRException
     *             if an errow was occured
     * @see MCRAccessInterface#addRule(String, String, org.jdom.Element, String)
     */
    public static void addRule(String id, String permission, org.jdom.Element rule, String description) throws MCRException {
        getAccessImpl().addRule(id, permission, rule, description);
    }

    /**
     * removes the <code>permission</code> rule for the MCRObjectID.
     * 
     * @param id
     *            the MCRObjectID of an object
     * @param permission
     *            the access permission for the rule
     * @throws MCRException
     *             if an errow was occured
     * @see MCRAccessInterface#removeRule(String, String)
     */
    public static void removeRule(MCRObjectID id, String permission) throws MCRException {
        getAccessImpl().removeRule(id.getId(), permission);
    }

    /**
     * removes the <code>permission</code> rule for the ID.
     * 
     * @param id
     *            the ID of an object as String
     * @param permission
     *            the access permission for the rule
     * @throws MCRException
     *             if an errow was occured
     * @see MCRAccessInterface#removeRule(String, String)
     */
    public static void removeRule(String id, String permission) throws MCRException {
        getAccessImpl().removeRule(id, permission);
    }

    /**
     * removes all rules for the MCRObjectID.
     * 
     * @param id
     *            the MCRObjectID of an object
     * @throws MCRException
     *             if an errow was occured
     * @see MCRAccessInterface#removeRule(String)
     */
    public static void removeAllRules(MCRObjectID id) throws MCRException {
        getAccessImpl().removeAllRules(id.getId());
    }

    /**
     * updates an access rule for an MCRObjectID.
     * 
     * @param id
     *            the MCRObjectID of the object
     * @param permission
     *            the access permission for the rule
     * @param rule
     *            the access rule
     * @param description
     *            description for the given access rule, e.g. "allows public access"
     * @throws MCRException
     *             if an errow was occured
     * @see MCRAccessInterface#updateRule(String, String, Element, String)
     */
    public static void updateRule(MCRObjectID id, String permission, org.jdom.Element rule, String description) throws MCRException {
        getAccessImpl().updateRule(id.getId(), permission, rule, description);
    }

    /**
     * updates an access rule for an ID.
     * 
     * @param id
     *            the ID of the object
     * @param permission
     *            the access permission for the rule
     * @param rule
     *            the access rule
     * @param description
     *            description for the given access rule, e.g. "allows public access"
     * @throws MCRException
     *             if an errow was occured
     * @see MCRAccessInterface#updateRule(String, String, Element, String)
     */
    public static void updateRule(String id, String permission, org.jdom.Element rule, String description) throws MCRException {
        getAccessImpl().updateRule(id, permission, rule, description);
    }

    /**
     * determines whether the current user has the permission to perform a
     * certain action.
     * 
     * @param id
     *            the MCRObjectID of the object
     * @param permission
     *            the access permission for the rule
     * @return true if the access is allowed otherwise it return
     * @see MCRAccessInterface#checkPermission(String, String)
     */
    public static boolean checkPermission(MCRObjectID id, String permission) {
        return checkPermission(id.getId(),permission);
    }
    
    /**
     * determines whether the current user has the permission to perform a
     * certain action.
     * 
     * @param id
     *            the MCRObjectID of the object
     * @param permission
     *            the access permission for the rule
     * @return true if the permission for the id is given
     */
    public static boolean checkPermission(String id, String permission) {
      return ACCESS_STRATEGY.checkPermission(id, permission);   
  }

    /**
     * determines whether the current user has the permission to perform a
     * certain action.
     * 
     * @param permission
     *            the access permission for the rule
     * @return true if the permission exist
     */
    public static boolean checkPermission(String permission) {
      return getAccessImpl().checkPermission(permission);   
  }

    /**
     * checks whether the current user has the permission to read/see a derivate
     *        check is also against the mcrobject, the derivate belongs to
     *        both checks must return true <br />
     *        it is needed in MCRFileNodeServlet and MCRZipServlet
     * @param derID
     *        String ID of a MyCoRe-Derivate
     * @return true if the access is allowed otherwise it return false
     */
    public static boolean checkPermissionForReadingDerivate(String derID){
    	// derID must be a derivate ID
    	boolean accessAllowed = false;
    	List l = MCRLinkTableManager.instance().getSourceOf(derID,"derivate");
		if(l != null && l.size() > 0) {
			accessAllowed = checkPermission((String)l.get(0),"read") && checkPermission(derID,"read");
		}else {
			accessAllowed = checkPermission(derID,"read");
			Logger.getLogger("MCRAccessManager.class").warn("no mcrobject could be found for derivate: " + derID);
		}
		return accessAllowed;
    }
    
    /**
     * lists all permissions defined for the <code>id</code>.
     * 
     * @param id
     *           the ID of the object as String
     * @return a <code>List</code> of all for <code>id</code> defined
     *         permissions
     */ 
    public static List getPermissionsForID(String id) {
        return getAccessImpl().getPermissionsForID(id);
    } 
    
    /**
     * lists all permissions defined for the <code>id</code>.
     * 
     * @param id
     *           the MCRObjectID of the object
     * @return a <code>List</code> of all for <code>id</code> defined
     *         permissions
     */ 
    public static List getPermissionsForID(MCRObjectID id) {
        return getAccessImpl().getPermissionsForID(id.getId());
    } 
    
    /**
     * return a rule, that allows something for everybody
     * 
     * @return a rule, that allows something for everybody
     */
    public static Element getTrueRule(){
    	Element condition = new Element("condition");
    	condition.setAttribute("format","xml");
    	Element booleanOp = new Element("boolean");
    	booleanOp.setAttribute("operator", "true");
    	condition.addContent(booleanOp);
    	return condition;
    }
    
    /**
     * return a rule, that forbids something for all, but superuser
     * 
     * @return a rule, that forbids something for all, but superuser
     */
    public static Element getFalseRule(){
    	Element condition = new Element("condition");
    	condition.setAttribute("format","xml");
    	Element booleanOp = new Element("boolean");
    	booleanOp.setAttribute("operator", "false");
    	condition.addContent(booleanOp);
    	return condition;
    }
    
    /**
     * return true if a rule for the id exist
     * 
     * @param id
     *           the MCRObjectID of the object
     * @param permission
     *            the access permission for the rule
     */
    public static boolean hasRule(String id, String permission){
        return getPermissionsForID(id).contains(permission);
    }

}