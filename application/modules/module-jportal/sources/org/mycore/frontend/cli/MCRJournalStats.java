package org.mycore.frontend.cli;

import java.util.HashMap;

import org.mycore.datamodel.metadata.MCRMetaLink;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

public class MCRJournalStats extends MCRAbstractCommands {
	
	private MCRObjectID ID;
	private String name;
	private String JournalType; 
	
	private HashMap<Integer, MCRObjectID> incompleteArticle = new HashMap<Integer, MCRObjectID> ();
	
	private int GoodObjectCounter = 0;
	private int BadObjectCounter = 0;
	private int OverallCounter = 0;
	
	MCRJournalStats(MCRObjectID ID, String JournalType)
		{
		 MCRObject Journal = new MCRObject();
		 Journal.receiveFromDatastore(ID);
		 this.ID = ID;
		 this.name = Journal.getLabel();
		 this.JournalType = JournalType;
		}
	
	public void incompleteArt(MCRObjectID ID)
		{
		 incompleteArticle.put(BadObjectCounter, ID);
		 
		 this.BadObjectCounter++;
		 this.OverallCounter++;		
		}
	
	public void completeArt(MCRObjectID ID)
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
}
