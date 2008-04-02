package org.mycore.frontend.cli;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.XMLDocumentParser;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaLink;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectMetadata;
import org.mycore.datamodel.metadata.MCRObjectStructure;
import org.mycore.datamodel.metadata.MCRXMLTableManager;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MCRJournalSummary extends MCRAbstractCommands {

	static MCRXMLTableManager xmltable = MCRXMLTableManager.instance();

	private static int maxArtID = xmltable.getNextFreeIdInt("jportal",
			"jparticle");
	private static int maxVolID = xmltable.getNextFreeIdInt("jportal",
			"jpvolume");
	private static int maxJouID = xmltable.getNextFreeIdInt("jportal",
			"jpjournal");

	private static HashMap<MCRObjectID, MCRJournalStats> journals = new HashMap<MCRObjectID, MCRJournalStats>();

	private static Logger logger = Logger.getLogger(MCRJournalSummary.class
			.getName());

	public MCRJournalSummary() {
		super();

		MCRCommand com = null;

		com = new MCRCommand("check journal {0} for incomplete objects",
				"org.mycore.frontend.cli.MCRJournalSummary.DoIt String", "");
		command.add(com);
		com = new MCRCommand("check all journals for incomplete objects",
				"org.mycore.frontend.cli.MCRJournalSummary.DoIt", "");
		command.add(com);
	}

	private static MCRObject getActualObject(String type, int ID) {
		MCRObjectID MOID = new MCRObjectID("jportal_" + type
				+ Integer.toString(ID));
		MCRObject actObj = new MCRObject();
		try {
			actObj.receiveFromDatastore(MOID);
		} catch (Exception e) {
			actObj = null;
		}

		return actObj;
	}

	private static void ParseObjects(String type, String ObjType, int maxID,
			String JID) {
		// type tells if either articles are need to be checked or volumes
		// ObjType is the corresponding tag of the object id
		// maxID of the objects in the database according to the type
		// if JID is all the programm scans all journals, else only a specific

		for (int i = 0; i < maxID; i++) {
			MCRObject object = getActualObject(ObjType, i);
			if (((float) i * 100 / (float) maxID) % 20 == 0) {
				logger.info("Progress: " + ((float) i * 100 / (float) maxID)
						+ "%... ");
			}

			if (object != null) {
				MCRObjectStructure objStruct = object.getStructure();

				if (objStruct.getChildSize() == 0) {
					// check for journal belonging to the object
					MCRObjectID objParentID = objStruct.getParentID();
					MCRObjectID objParentIDtemp = objStruct.getParentID();
					MCRObject objParent = new MCRObject();
					boolean checker = true;

					// get upstairs through the xml-tree until the root is
					// reached
					while (objParentID != null) {
						objParentIDtemp = objParentID;
						// error if the founded ID has no Object in the database
						try {
							objParent.receiveFromDatastore(objParentID);
						} catch (Exception e) {
							logger.info("Error, parent is null!");
							logger.info(object.getId());
							logger.info(objParentID);
							checker = false;
						}
						objParentID = objParent.getStructure().getParentID();
					}// while

					// if all every object is checked, else only if the journal
					// ID is right
					// checker is true if there was no exception when getting
					// the journal object
					if ((JID.equals("all") || JID.equals(objParentIDtemp
							.toString()))
							&& checker) {
						// create new journal stats object if needed
						if (!journals.containsKey(objParentIDtemp)) {
							logger.info("new Journal found, with ID "
									+ objParentIDtemp);
							journals.put(objParentIDtemp, new MCRJournalStats(
									objParentIDtemp, "article"));
						}

						if ((journals.get(objParentIDtemp).getObjectFocus()
								.equals("fully") && type.equals("articles"))
								|| (journals.get(objParentIDtemp)
										.getObjectFocus().equals("browse") && type
										.equals("volumes"))) {
							// check for completeness
							if (objStruct.getDerivateSize() == 0) {
								journals.get(objParentIDtemp).incompleteObj(
										object.getId());
							} else {
								journals.get(objParentIDtemp).completeObj(
										object.getId());
							}
						}
						if (journals.get(objParentIDtemp).getObjectFocus()
								.equals("fully")
								&& type.equals("volumes")) {
							journals.get(objParentIDtemp).MissingChildObj(
									object.getId());
						}
					}
				}
			}
		}
	}// DoArticle

	// Print all stats on the screen
	private static void PrintStats() throws IOException, JDOMException {

		java.util.Date heute = new java.util.Date();
		Timestamp time = new Timestamp(heute.getTime());
		Long actualTime = time.getTime();
		
		Element date = new Element("statistic").setAttribute("date", actualTime.toString());

		logger
				.info("/************************Journal Status Report*************************/");
		// go through all journal objects in the hash map
		for (int k = 0; k <= maxJouID; k++) {
			MCRObjectID JournalID = new MCRObjectID("jportal_jpjournal_"
					+ Integer.toString(k));
			if (journals.containsKey(JournalID)) {
				MCRJournalStats journal = journals.get(JournalID);
				Element XMLjournal = new Element("journal").setAttribute(
						"name", journal.getJournalName()).setAttribute("type",
						journal.getObjectFocus()).setAttribute("id",
						journal.getJournalID().toString());
				Element XMLobjectsInc = new Element("objectList").setAttribute(
						"type", "incomplete");
				Element XMLobjectsMis = new Element("objectList").setAttribute(
						"type", "missing");

				for (int i = 0; i < journal.getBadCounter(); i++) {
					Element XMLobject = new Element("object").setAttribute(
							"id", journal.getIncompleteObjects().get(i)
									.toString());
					XMLobjectsInc.addContent(XMLobject);
				}
				for (int j = 0; j < journal.getMissingChildrenCounter(); j++) {
					Element XMLobject = new Element("object").setAttribute(
							"id", journal.getMissingChildren().get(j)
									.toString());
					XMLobjectsMis.addContent(XMLobject);
				}

				logger
						.info("=================================================================");
				logger
						.info("=                                                               =");
				logger.info("= actual Journal: " + journal.getJournalName()
						+ " with ID " + journal.getJournalID() + " =");
				logger
						.info("=                                                               =");
				logger
						.info("=================================================================");
				logger.info("  the important Objects are " + journal.getType());
				logger.info("");
				logger.info("");
				logger.info(" ++++++++++++++++++Details+++++++++++++++++++");
				logger.info("  Number of " + journal.getType()
						+ "s          : " + journal.getAllCounter());

				Element NumberOfObjects = new Element("numberOfObjects");

				Element Total = new Element("total").setText(Integer
						.toString(journal.getAllCounter()));

				String complete = journal.getGoodCounter()
						+ " / "
						+ ((float) journal.getGoodCounter()
								/ (float) journal.getAllCounter() * 100) + "%";
				logger.info("  Complete                    : " + complete);
				Element Complete = new Element("complete").setAttribute(
						"percent",
						Float.toString((float) journal.getGoodCounter()
								/ (float) journal.getAllCounter() * 100))
						.setText(Integer.toString(journal.getGoodCounter()));

				String incomplete = journal.getBadCounter()
						+ " / "
						+ ((float) journal.getBadCounter()
								/ (float) journal.getAllCounter() * 100) + "%";
				logger.info("  Incomplete                  : " + incomplete);
				Element Incomplete = new Element("incomplete").setAttribute(
						"percent",
						Float.toString((float) journal.getBadCounter()
								/ (float) journal.getAllCounter() * 100))
						.setText(Integer.toString(journal.getBadCounter()));

				logger.info("  Volume with Missing Articles: "
						+ journal.getMissingChildrenCounter()
						+ " / "
						+ ((float) journal.getMissingChildrenCounter()
								/ (float) journal.getAllCounter() * 100) + "%");
				Element Missing = new Element("missing").setAttribute(
						"percent",
						Float.toString((float) journal
								.getMissingChildrenCounter()
								/ (float) journal.getAllCounter() * 100))
						.setText(
								Integer.toString(journal
										.getMissingChildrenCounter()));

				logger.info("");
				logger.info("");
				logger
						.info("=================================================================");
				logger.info("");
				logger.info("");

				NumberOfObjects.addContent(Total);
				NumberOfObjects.addContent(Complete);
				NumberOfObjects.addContent(Incomplete);
				NumberOfObjects.addContent(Missing);
				XMLjournal.addContent(NumberOfObjects);
				XMLjournal.addContent(XMLobjectsInc);
				XMLjournal.addContent(XMLobjectsMis);
				date.addContent(XMLjournal);
			}
		}

		logger
				.info("/************************Journal Status Report*************************/");
		saveXML(date, "build/webapps", "build/webapps/journalStatistic.xml",
				true);
	}

	private static List<Element> manipulateXML(String targetFile)
			throws JDOMException, IOException {
		boolean empty = true;
		Document doc = new Document();
		List<Element> AllStats = null;
		
		try {
			SAXBuilder sxbuild = new SAXBuilder();
			InputSource is = new InputSource(targetFile);
			doc = sxbuild.build(is);
		}

		catch (Exception e) {
			logger.info("Article...");
			empty = false;
		}

		if (empty) {
			// Lesen des Wurzelelements des JDOM-Dokuments doc
			Element root = doc.getRootElement();

			AllStats = root.removeContent();
			
			int ListSize = AllStats.size();
			
			Element LastStats = AllStats.get(ListSize-1);
			
			List<Element> AllPapers = LastStats.getChildren();

			Iterator<Element> papers = AllPapers.iterator();

			while (papers.hasNext()) {
				Element journal = (Element) papers.next();

				journal.removeChildren("objectList");
				
			}
		}
		
		return AllStats;
	}

	private static void saveXML(Element newStats, String dir, String targetFile,
			boolean log) throws IOException, FileNotFoundException,
			JDOMException {
		Document Addition = new Document();
		Element root = new Element("journalStatistic");
		Addition.setRootElement(root);
		List<Element> oldContent = manipulateXML(targetFile);
		
		
		XMLOutputter xmlOut = new XMLOutputter();
		xmlOut.getFormat().getPrettyFormat();
		File directory = new File(dir);
		directory.mkdirs();
		FileOutputStream fos = new FileOutputStream(new File(targetFile));
				
		root.removeContent();
		
		if(oldContent!=null)
			{
	
			 Iterator<Element> oldContIt = oldContent.iterator();
			
			 while (oldContIt.hasNext()) 
				{
				 Element oldContEl = (Element) oldContIt.next();
			     root.addContent(oldContEl);
				}
			}		
		
		root.addContent(newStats);
		xmlOut.output(Addition, fos);
		fos.flush();
		fos.close();
		if (log)
			logger.info("saved " + targetFile + "... ");
	}

	public static void DoIt(String JID) throws IOException, JDOMException {
		logger.info("Go Go Go!");
		logger.info("====================");

		logger.info("Article...");

		if (JID.equals("")) {
			JID = "all";
		}

		ParseObjects("articles", "jparticle_", maxArtID, JID);

		logger.info("Volumes...");

		ParseObjects("volumes", "jpvolume_", maxVolID, JID);

		logger.info("Status...");

		PrintStats();

		logger.info("Ready.");
		logger.info("");
	}
	public static void DoIt() throws IOException, JDOMException {
		logger.info("Go Go Go!");
		logger.info("====================");

		logger.info("Article...");

		ParseObjects("articles", "jparticle_", maxArtID, "all");

		logger.info("Volumes...");

		ParseObjects("volumes", "jpvolume_", maxVolID, "all");

		logger.info("Status...");

		PrintStats();

		logger.info("Ready.");
		logger.info("");
	}

}
