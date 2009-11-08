package org.mycore.common.xml;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.mycore.common.MCRCache;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.xml.MCRURIResolver.MCRResolver;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRDirectoryXML;
import org.mycore.datamodel.ifs.MCRFile;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.ifs.MCRGetDerivateDirectoryXML;

public class MCRJPortalGetDerDirXML implements MCRResolver {

	private static String URI = "jportal_getDerDirXML";

	private static final Logger LOGGER = Logger.getLogger(MCRJPortalGetDerDirXML.class);

	/**
	 * Returns a XML directory listing of a derivate and caches the last call.
	 * So, this should speed up results of fulltext search that are within a
	 * file. Syntax: <code>jportal_getDerDirXML:DerivateID
	 * 
	 * @param uri
	 *            URI in the syntax above
	 * 
	 * @return <root><directoryListing</root>
	 * @throws IOException
	 */
	public Element resolveElement(String uri) throws IOException {

		if (!wellURI(uri))
			throw new IllegalArgumentException("Invalid format of uri given to resolve " + URI + "=" + uri);

		// get deriv id
		String[] params = uri.split(":");
		String derivID = params[1];

		LOGGER.info("MCRJPortalGetDerDirXML called with derivate id = " + derivID);

		return MCRGetDerivateDirectoryXML.getDirXML(derivID);

	}

	private boolean wellURI(String uri) {
		String[] parameters = uri.split(":");
		if (parameters.length == 2 && parameters[0].equals(URI) && !parameters[1].equals(""))
			return true;
		return false;
	}
}
