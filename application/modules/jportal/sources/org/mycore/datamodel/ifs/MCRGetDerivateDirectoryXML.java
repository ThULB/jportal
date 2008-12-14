/**
 * 
 */
package org.mycore.datamodel.ifs;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.mycore.common.MCRException;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRDirectoryXML;
import org.mycore.datamodel.ifs.MCRFile;
import org.mycore.datamodel.ifs.MCRFileNodeServlet;
import org.mycore.datamodel.ifs.MCRFilesystemNode;

/**
 * @author Andreas
 * 
 */
public class MCRGetDerivateDirectoryXML {

	private static Logger LOGGER = Logger
			.getLogger(MCRGetDerivateDirectoryXML.class.getName());

	private static HashMap<String, Element> DER_DIR_XML_CACHE = new HashMap<String, Element>(
			1);

	public static Element getDirXML(String derivID) throws IOException {

		Element dirXML = new Element("root");

		// try to receive from cache
		if (DER_DIR_XML_CACHE.containsKey(derivID)) {
			dirXML = new Element("root").addContent(((Element) DER_DIR_XML_CACHE
					.get(derivID)).detach());
			LOGGER.debug("DirectoryXML of " + derivID + " taken from cache");
		}
		// not contained in cache -> get it
		else {
			// get root (derivate) node
			MCRFilesystemNode root;
			try {
				root = MCRFilesystemNode.getRootNode(derivID);
			} catch (org.mycore.common.MCRPersistenceException e) {
				// Could not get value from JDBC result set
				LOGGER.error(
						"MCRGetDerivateDirectoryXML: Error while getting root node = "
								+ derivID, e);
				root = null;
			}
			// get XML of root node
			dirXML = MCRDirectoryXML.getInstance().getDirectoryXML(
					(MCRDirectory) root).getRootElement();
			LOGGER
					.info("Sending listing of directory "
							+ derivID);
			//(new XMLOutputter()).output(dirXML, System.out);
			// put in cache
			DER_DIR_XML_CACHE.clear();
			DER_DIR_XML_CACHE.put(derivID, dirXML);
			LOGGER.debug("DirectoryXML of " + derivID + " put into cache");
		}

		return dirXML;
	}
}
