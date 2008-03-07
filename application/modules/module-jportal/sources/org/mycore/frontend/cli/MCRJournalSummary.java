package org.mycore.frontend.cli;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.mycore.datamodel.metadata.MCRMetaLink;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectStructure;
import org.mycore.datamodel.metadata.MCRXMLTableManager;

public class MCRJournalSummary extends MCRAbstractCommands {
	
	static MCRXMLTableManager xmltable = MCRXMLTableManager.instance();

	private static int maxArtID = xmltable.getNextFreeIdInt("jportal","jparticle");	
	private static int maxVolID = xmltable.getNextFreeIdInt("jportal","jpvolume");
	private static int maxJouID = xmltable.getNextFreeIdInt("jportal","jpjournal");
	
	private static HashMap<MCRObjectID, MCRJournalStats> journals = new HashMap<MCRObjectID, MCRJournalStats> ();

	private static Logger logger = Logger.getLogger(MCRJournalSummary.class.getName());
	
	public MCRJournalSummary() {
        super();

        MCRCommand com = null;

        com = new MCRCommand("check Journals for incomplete Objects", "org.mycore.frontend.cli.MCRJournalSummary.DoIt", "");
        command.add(com);
    }
	

	
	private static MCRObject getActualObject(String type,int ID)
		{
		 MCRObjectID MOID = new MCRObjectID("jportal_"+type+Integer.toString(ID));
		 MCRObject actObj = new MCRObject();
		 try{actObj.receiveFromDatastore(MOID);}
		 catch(Exception e)
		 {actObj=null;}
		 
		 return actObj;
		}
	
	
	private static void DoArticle()
		{ 
		 for(int i = 0;i < maxArtID; i++)
			{
			 MCRObject article = getActualObject("jparticle_",i);
			 if(i % 1000 == 0)
			 	{
				 System.out.print("Progress: " + ((float)i/(float)maxArtID*100) +"%... ");
			 	}
			 
			 if(article!=null)
			 	{
				 MCRObjectStructure artStruct = article.getStructure();
				 
				 if(artStruct.getChildSize()==0)
				 	{
					 //check for journal belonging to the article
					 MCRObjectID artParentID = artStruct.getParentID(); 
					 MCRObjectID artParentIDtemp = artStruct.getParentID(); 
					 MCRObject artParent = new MCRObject();
					 boolean checker = true;
					 
					 while(artParentID!=null)
					 	{
						 artParentIDtemp = artParentID;
						 try{artParent.receiveFromDatastore(artParentID);}
						 	catch(Exception e)
						 	{
						 	 logger.info("Error, parent is null!");	
						 	 logger.info(article.getId());
						 	 logger.info(artParentID);
						 	 checker = false;
						 	}
						 artParentID = artParent.getStructure().getParentID(); 
					 	}//while
					 
					 if(checker)
						 {
						  //new journal stats object if needed
						  if(!journals.containsKey(artParentIDtemp))
						  	 {
							  logger.info("new Journal found, with ID " + artParentIDtemp);
							  journals.put(artParentIDtemp, new MCRJournalStats(artParentIDtemp, "article"));
						 	 }
						 
						  //check for completeness					 
						  if(artStruct.getDerivateSize()==0)
						 	 {
							  journals.get(artParentIDtemp).incompleteArt(article.getId());
						 	 }
						  else
						 	 {
							  journals.get(artParentIDtemp).completeArt(article.getId());
						 	 }
						 }
				 	}
				 
			 	}
			 
			}
		}//DoArticle
	
	private static void DoVolumes()
	{ 
	
	 for(int i = 0;i < maxVolID; i++)
		{
		 MCRObject volume = getActualObject("jpvolume_",i);
		 
		 if(i % 1000 == 0)
		 	{
			 System.out.print("Progress: " + ((float)i/(float)maxVolID*100) +"%... ");
		 	}
		 
		 if(volume!=null)
		 	{
			 MCRObjectStructure volStruct = volume.getStructure();
			 
			 if(volStruct.getChildSize()==0)
			 	{
				 //check for journal belonging to the article
				 MCRObjectID volParentID = volStruct.getParentID(); 
				 MCRObjectID volParentIDtemp = volStruct.getParentID(); 
				 MCRObject volParent = new MCRObject();
				 boolean checker = true;
				 
				 while(volParentID!=null)
				 	{
					 volParentIDtemp = volParentID;			 
					 try{volParent.receiveFromDatastore(volParentID);}
					 	catch(Exception e)
					 	{
					 	 logger.info("Error, parent is null!");	
					 	 logger.info(volume.getId());
					 	 logger.info(volParentID);
					 	 checker = false;
					 	}
					 volParentID = volParent.getStructure().getParentID();
				 	}//while
				 
				 if(checker)
					 {
					 //new journal stats object if needed
					 if(!journals.containsKey(volParentIDtemp))
					 	{	 
						 logger.info("new Journal found, with ID " + volParentIDtemp);
						 journals.put(volParentIDtemp, new MCRJournalStats(volParentIDtemp, "volume"));
					 	}
					 
					 //check for completeness					 
					 if(volStruct.getDerivateSize()==0)
					 	{
						 journals.get(volParentIDtemp).incompleteArt(volume.getId());
					 	}
					 else
					 	{
						 journals.get(volParentIDtemp).completeArt(volume.getId());
					 	}
					 }
			 	}		 
		 	}		 
		}
	}//DoVolume

	//Print all stats on the screen
	private static void PrintStats()
		{
		 logger.info("/*******************Journal Status Report*******************/");
 		 //go through all journal objects in the hash map
		 for(int k=0; k<=maxJouID; k++)
		 	{
			 MCRObjectID JournalID = new MCRObjectID("jportal_jpjournal_"+Integer.toString(k));
			 if(journals.containsKey(JournalID))
				 {
				  MCRJournalStats journal = journals.get(JournalID);
				  logger.info("");
				  logger.info("actual Journal: " + journal.getJournalName() + " with ID " + journal.getJournalID());
				  logger.info("");
				  logger.info("the important Objects are " + journal.getType());
				  logger.info("");
				  logger.info("");
				  logger.info("++++++++++++++++++Details+++++++++++++++++++");
				  logger.info("Number of " + journal.getType()+"s: " + journal.getAllCounter());
				  logger.info("Complete: " + journal.getGoodCounter() + " / " + ((float)journal.getGoodCounter()/(float)journal.getAllCounter()*100) +"%");
				  logger.info("Incomplete: " + journal.getBadCounter() + " / " + ((float)journal.getBadCounter()/(float)journal.getAllCounter()*100) +"%");
				  logger.info("");
				  logger.info("List of incomplete files is saved in ...");
				  logger.info("");
				 }
		 	}
		
		 logger.info("/*******************Journal Status Report*******************/");
		}
	
	
	public static void DoIt()
		{
		 logger.info("Go Go Go!");
	     logger.info("====================");

	     logger.info("Article...");
		 DoArticle();
		 logger.info("Volumes...");
		 DoVolumes();
		 logger.info("Status...");
		 PrintStats();
		 
		 logger.info("Ready.");
	     logger.info("");
		}
	
	}
