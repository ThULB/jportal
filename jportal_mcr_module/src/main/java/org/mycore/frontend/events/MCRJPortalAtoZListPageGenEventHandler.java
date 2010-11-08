package org.mycore.frontend.events;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.metadata.MCRObject;

/**
 * This EventHandler generates the AtoZ list if a journal is added, deleted or
 * updated. The data is stored to the journalList.xml file.
 * 
 * @author Matthias Eichner
 * @author Huu Chi Vu
 */
public class MCRJPortalAtoZListPageGenEventHandler extends MCREventHandlerBase {

	private static final String MODIFICATION_TIME = "JP.Journal.Modification.Time";
	private static final Logger LOGGER = Logger
			.getLogger(MCRJPortalAtoZListPageGenEventHandler.class);

	@Override
	protected void handleObjectCreated(MCREvent evt, MCRObject obj) {
		String operation = "create";
		setModificationTime(obj, operation);
	}


	@Override
	protected void handleObjectDeleted(MCREvent evt, MCRObject obj) {
		String operation = "delete";
		setModificationTime(obj, operation);
		
	}

	@Override
	protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
		//update journal list not necessary
	}
	
	public static long getModificationTime(){
		String modTimeStr = System.getProperty(MODIFICATION_TIME);
		if(modTimeStr != null && !modTimeStr.equals("")) {
			return Long.valueOf(modTimeStr);
		}
		
		return -1;
	}
	
	private void setModificationTime(MCRObject obj, String operation) {
		if (isJournal(obj)) {
			long currentTime = System.currentTimeMillis();
			System.setProperty(MODIFICATION_TIME, String.valueOf(currentTime));
			LOGGER.info(operation + " journal " + obj.getId().toString());
		}
	}

	/**
	 * Checks if MCRObject is a journal.
	 * 
	 * @param the
	 *            MCRObject to test
	 * @return true if the MCRObject an journal, otherwise false
	 */
	public boolean isJournal(MCRObject obj) {
		Document doc = obj.createXML();
		String id = doc.getRootElement().getAttributeValue("ID");
		if (id.contains("journal"))
			return true;
		return false;
	}

}