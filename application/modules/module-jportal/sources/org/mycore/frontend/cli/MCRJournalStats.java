package org.mycore.frontend.cli;

import java.util.HashMap;

import org.mycore.datamodel.metadata.MCRMetaLink;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

public class MCRJournalStats extends MCRAbstractCommands {
	
	private MCRObjectID ID;
	private String name;
	private String JournalType; 
	private String ObjectFocus;
	
	private HashMap<Integer, MCRObjectID> incompleteObjects = new HashMap<Integer, MCRObjectID> ();
	
	private int GoodObjectCounter = 0;
	private int BadObjectCounter = 0;
	private int OverallCounter = 0;
	
	MCRJournalStats(MCRObjectID ID, String JournalType)
		{
		 MCRObject Journal = new MCRObject();
		 Journal.receiveFromDatastore(ID);
		 this.ID = ID;
		 this.name = Journal.getMetadata().createXML().getChild("maintitles").getChild("maintitle").getText();
		 this.ObjectFocus = Journal.getMetadata().createXML().getChild("dataModelCoverages").getChild("dataModelCoverage").getAttributeValue("categid");
		 this.JournalType = JournalType;
		}
	
	public void incompleteObj(MCRObjectID ID)
		{
		 incompleteObjects.put(BadObjectCounter, ID);
		 
		 this.BadObjectCounter++;
		 this.OverallCounter++;		
		}
	
	public void completeObj(MCRObjectID ID)
		{
		 this.GoodObjectCounter++;
		 this.OverallCounter++;
		}
	
	public int getGoodCounter()
		{
		 return this.GoodObjectCounter;
		}
	
	public int getBadCounter()
		{
		 return this.BadObjectCounter;
		}
	
	public int getAllCounter()
		{
		 return this.OverallCounter;
		}
	
	public MCRObjectID getJournalID()
		{
		 return this.ID;
		}
	
	public String getJournalName()
		{
		 return this.name;
		}
	
	public String getType()
		{
		 return this.JournalType;
		}
	
	public String getObjectFocus()
		{
		 return this.ObjectFocus;
		}
	
	public HashMap<Integer, MCRObjectID> getIncompleteObjects()
	{
	 return this.incompleteObjects;
	}
}
