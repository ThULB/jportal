package org.mycore.frontend.cli;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaLink;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectMetadata;
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

        com = new MCRCommand("check journal {0} for incomplete objects, with important volume {1}", "org.mycore.frontend.cli.MCRJournalSummary.DoIt String String", "");
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
	
	
	private static void ParseObjects(String type, String ObjType, int maxID, String JID, String volumeLevel)
		{ 
		 for(int i = 0;i < maxID; i++)
			{
			 MCRObject object = getActualObject(ObjType,i);
			 if(((float)i*100/(float)maxID) % 20 == 0)
			 	{
				 //System.out.print((char)27 + "[D");
				 System.out.print("Progress: " + ((float)i*100/(float)maxID) +"%... ");
			 	}
			 
			 if(object!=null)
			 	{
				 MCRObjectStructure objStruct = object.getStructure();
				 
				 if(objStruct.getChildSize()==0)
				 	{
					 //check for journal belonging to the object
					 MCRObjectID objParentID = objStruct.getParentID(); 
					 MCRObjectID objParentIDtemp = objStruct.getParentID(); 
					 MCRObject objParent = new MCRObject();
					 boolean checker = true;
					 
					 while(objParentID!=null)
					 	{
						 objParentIDtemp = objParentID;
						 try{objParent.receiveFromDatastore(objParentID);}
						 	catch(Exception e)
						 	{
						 	 logger.info("Error, parent is null!");	
						 	 logger.info(object.getId());
						 	 logger.info(objParentID);
						 	 checker = false;
						 	}
						 objParentID = objParent.getStructure().getParentID(); 
					 	}//while
					 
					 if(JID.equals("all") || JID.equals(objParentIDtemp.toString()))
					 	{
						 if(checker)
							 {
							  //new journal stats object if needed
							  if(!journals.containsKey(objParentIDtemp))
							  	 {
								  logger.info("new Journal found, with ID " + objParentIDtemp);
								  journals.put(objParentIDtemp, new MCRJournalStats(objParentIDtemp, "article"));
							 	 }
							  
							  if(!(journals.get(objParentIDtemp).getObjectFocus().equals("fully") && type.equals("volume")))
							  	 {
								  //check for completeness					 
								  if(objStruct.getDerivateSize()==0)
								 	 {
									  journals.get(objParentIDtemp).incompleteObj(object.getId());
								 	 }
								  else
								 	 {
									  journals.get(objParentIDtemp).completeObj(object.getId());
								 	 }
							  	 }
							 }
					 	}
				 	}
				 
			 	}
			 
			}
		}//DoArticle

	//Print all stats on the screen
	private static void PrintStats() throws IOException
		{
		 Document doc = new Document();
		 Element root = new Element("incompleteObjects");
		 doc.setRootElement(root);
		
		 logger.info("/************************Journal Status Report*************************/");
 		 //go through all journal objects in the hash map
		 for(int k=0; k<=maxJouID; k++)
		 	{
			 MCRObjectID JournalID = new MCRObjectID("jportal_jpjournal_"+Integer.toString(k));
			 if(journals.containsKey(JournalID))
				 {
				  MCRJournalStats journal = journals.get(JournalID);
				  Element XMLjournal = new Element("Journal").setAttribute("type", journal.getType()).setAttribute("ID", JournalID.toString());
				  
				  for(int h = 0; h < journal.getBadCounter(); h++)
				  	{
					 Element XMLobject = new Element("Object").setAttribute("ID", journal.getIncompleteObjects().get(h).toString());
					 XMLjournal.addContent(XMLobject);  
				  	}
				  root.addContent(XMLjournal);
				  
				  logger.info("=================================================================");
				  logger.info("=                                                               =");
				  logger.info("= actual Journal: " + journal.getJournalName() + " with ID " + journal.getJournalID() + " =");
				  logger.info("=                                                               =");
				  logger.info("=================================================================");
				  logger.info("  the important Objects are " + journal.getType());
				  logger.info("");
				  logger.info("");
				  logger.info(" ++++++++++++++++++Details+++++++++++++++++++");
				  logger.info("  Number of " + journal.getType()+"s: " + journal.getAllCounter());
				  logger.info("  Complete: " + journal.getGoodCounter() + " / " + ((float)journal.getGoodCounter()/(float)journal.getAllCounter()*100) +"%");
				  logger.info("  Incomplete: " + journal.getBadCounter() + " / " + ((float)journal.getBadCounter()/(float)journal.getAllCounter()*100) +"%");
				  logger.info("");
				  logger.info("");
				  logger.info("=================================================================");
				  logger.info("");
				  logger.info("");
				 }
		 	}
		
		 logger.info("/************************Journal Status Report*************************/");
		 saveXML(doc, "", "incObjects.xml", true);
		}
	
	private static void saveXML (Document doc, String dir, String targetFile, boolean log) throws IOException, FileNotFoundException 
		{
		 XMLOutputter xmlOut = new XMLOutputter();
		 File directory = new File(dir);
		 directory.mkdirs();
		 FileOutputStream fos = new FileOutputStream(new File(targetFile));
		 
		 xmlOut.output(doc, fos);
		 fos.flush();
		 fos.close();
		 if (log) logger.info("saved "+targetFile+"... ");
		}
	
	public static void DoIt(String JID, String volumeLevel) throws IOException
		{
		 logger.info("Go Go Go!");
	     logger.info("====================");

	     logger.info("Article...");
		 
	     ParseObjects("articles", "jparticle_", maxArtID, JID, volumeLevel);
		 
	     logger.info("Volumes...");
		 
	     ParseObjects("volumes", "jpvolume_", maxVolID, JID, volumeLevel);
		 
	     logger.info("Status...");
		 
	     PrintStats();
		 
		 logger.info("Ready.");
	     logger.info("");
		}
	
	}
