package org.mycore.frontend.cli;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.XMLDocumentParser;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.hibernate.Transaction;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.mycore.backend.hibernate.MCRHIBConnection;
import org.mycore.common.MCRConfiguration;
import org.mycore.datamodel.common.MCRXMLTableManager;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFile;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaLink;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectMetadata;
import org.mycore.datamodel.metadata.MCRObjectStructure;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class MCRJournalSummary extends MCRAbstractCommands {

	static MCRXMLTableManager xmltable;

	private static int maxArtID;

	private static int maxVolID;

	private static int maxJouID;

	private static HashMap<MCRObjectID, MCRJournalStats> journals = new HashMap<MCRObjectID, MCRJournalStats>();

	private static Logger logger = Logger.getLogger(MCRJournalSummary.class
			.getName());

	public static String saveFolder = MCRConfiguration.instance().getString(
			"MCR.basedir")
			+ "build/webapps";
	public static String CSVFileName = "AIValues.csv";

	public MCRJournalSummary() {
		super();

		MCRCommand com = null;

		com = new MCRCommand("check journal {0} for incomplete objects",
				"org.mycore.frontend.cli.MCRJournalSummary.DoIt String", "");
		command.add(com);
		com = new MCRCommand("check all journals for incomplete objects",
				"org.mycore.frontend.cli.MCRJournalSummary.DoIt", "");
		command.add(com);
		com = new MCRCommand(
				"create ActivityIndex-CSV-file for Statistic Tool",
				"org.mycore.frontend.cli.MCRJournalSummary.createCSV", "");
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
		// if JID is all the program scans all journals, else only a specific
		System.out.println(maxID);
		for (int i = 0; i < maxID; i++) {

			System.out.println("-----" + i + "-----");
			MCRObject object = getActualObject(ObjType, i);
			if (((float) i * 100 / (float) maxID) % 20 == 0) {
				logger.info("Progress: " + ((float) i * 100 / (float) maxID)
						+ "%... ");
			}

			if (object != null) {
				MCRObjectStructure objStruct = object.getStructure();

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
				if ((JID.equals("all") || JID
						.equals(objParentIDtemp.toString()))
						&& checker) {
					if (!journals.containsKey(objParentIDtemp)) {
						logger.info("new Journal found, with ID "
								+ objParentIDtemp);
						journals.put(objParentIDtemp, new MCRJournalStats(
								objParentIDtemp, type));
					}
					if (objStruct.getDerivateSize() > 0) {
						logger.debug("ID: " + objParentIDtemp.toString());
						checkForDerivates(objStruct, objParentIDtemp, i);
					}
				}

				if (objStruct.getChildSize() == 0) {

					// if all every object is checked, else only if the journal
					// ID is right
					// checker is true if there was no exception when getting
					// the journal object
					if ((JID.equals("all") || JID.equals(objParentIDtemp
							.toString()))
							&& checker) {

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

	// gets all derivates of an object and starts the derivate parsing
	private static void checkForDerivates(MCRObjectStructure objStruct,
			MCRObjectID objParentIDtemp, int i) {
		MCRFilesystemNode root;
		int size = objStruct.getDerivateSize();

		for (int k = 0; k < size; k++) {
			String derivateID = objStruct.getDerivate(k).getXLinkHref();
			try {
				root = MCRFilesystemNode.getRootNode(derivateID);
			} catch (org.mycore.common.MCRPersistenceException e) {
				// Could not get value from JDBC result
				// set
				logger
						.error(
								"MCRFileNodeServlet: Error while getting root node!",
								e);
				root = null;
			}
			if (root != null) {
				parseDerivate(root, objParentIDtemp, i);
			}
		}
	}

	// counts all types and sizes of the derivate and writes it in the journal
	// object
	private static void parseDerivate(MCRFilesystemNode root,
			MCRObjectID objParentIDtemp, int i) {
		String type = "";
		Long size = 0l;

		MCRFile file = null;
		if (root instanceof MCRFile) {
			file = (MCRFile) root;

			type = file.getContentTypeID().toString();
			size = file.getSize();
			logger.debug("type: " + type + " size: " + size);
			MCRJournalStats test = journals.get(objParentIDtemp);
			test.setDerivates(type, size);
		} else {
			MCRDirectory dir = (MCRDirectory) root;
			MCRFilesystemNode[] children = null;
			boolean checker = true;
			if (dir != null) {
				// logger.info(dir);
				try {
					children = dir.getChildren();
				} catch (Exception e) {
					logger.info("Error in Object ID: " + i);
					logger.info("With Derivate ID: " + dir.getName());
					checker = false;
				}
				if (checker) {
					for (int t = 0; t < children.length; t++) {
						parseDerivate(children[t], objParentIDtemp, i);
					}
				}
			}
		}

	}

	// Print all stats on the screen
	private static void PrintStats() throws IOException, JDOMException {

		int scaleValue = 0;

		java.util.Date heute = new java.util.Date();
		Timestamp time = new Timestamp(heute.getTime());
		Long actualTime = time.getTime();
		// pretty date
		SimpleDateFormat formater = new SimpleDateFormat("EEE, MMM d, ''yy",
				Locale.GERMANY);
		String datePretty = formater.format(actualTime);
		Element date = new Element("statistic");
		date.setAttribute("date", actualTime.toString());
		date.setAttribute("datePretty", datePretty);

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

				String importantType = "";
				if (journal.getObjectFocus().equals("browse")) {
					importantType = "volume";
					scaleValue = journal.getAllCounter();
				}
				if (journal.getObjectFocus().equals("fully")) {
					importantType = "article";
					scaleValue = journal.getAllCounter()
							- journal.getMissingChildrenCounter();
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
				logger.info("  the important Objects are " + importantType);
				logger.info("");
				logger.info("");
				logger.info(" ++++++++++++++++++Details+++++++++++++++++++");
				logger.info("  Number of " + journal.getType()
						+ "s          : " + journal.getAllCounter());

				Element NumberOfObjects = new Element("numberOfObjects");

				Element Total = new Element("total");
				Total.setAttribute("scale", Double.toString(scale(scaleValue)));
				Total
						.setAttribute(
								"percent",
								Double
										.toString(round(
												(((double) (journal
														.getAllCounter() - journal
														.getMissingChildrenCounter()) / (double) MCRJournalStats
														.getAllObjectsCounter()) * 100),
												2, RoundingMode.HALF_EVEN,
												FormatType.fix)));
				Total.setText(Integer.toString(journal.getAllCounter()
						- journal.getMissingChildrenCounter()));

				String complete = journal.getGoodCounter()
						+ " / "
						+ ((float) journal.getGoodCounter()
								/ (float) journal.getAllCounter() * 100) + "%";
				logger.info("  Complete                    : " + complete);
				Element Complete = new Element("complete");
				Complete.setAttribute("percent", Double.toString(round(
						(((double) journal.getGoodCounter() / (double) journal
								.getAllCounter()) * 100), 2,
						RoundingMode.HALF_EVEN, FormatType.fix)));
				Complete.setText(Integer.toString(journal.getGoodCounter()));

				String incomplete = journal.getBadCounter()
						+ " / "
						+ ((float) journal.getBadCounter()
								/ (float) journal.getAllCounter() * 100) + "%";
				logger.info("  Incomplete                  : " + incomplete);
				Element Incomplete = new Element("incomplete").setAttribute(
						"percent",
						Double.toString(round((((double) journal
								.getBadCounter() / (double) journal
								.getAllCounter()) * 100), 2,
								RoundingMode.HALF_EVEN, FormatType.fix)))
						.setText(Integer.toString(journal.getBadCounter()));

				logger.info("  Volume with Missing Articles: "
						+ journal.getMissingChildrenCounter()
						+ " / "
						+ ((float) journal.getMissingChildrenCounter()
								/ (float) journal.getAllCounter() * 100) + "%");
				Element Missing = new Element("missing").setAttribute(
						"percent",
						Double.toString(round(((double) journal
								.getMissingChildrenCounter()
								/ (double) journal.getAllCounter() * 100), 2,
								RoundingMode.HALF_EVEN, FormatType.fix)))
						.setText(
								Integer.toString(journal
										.getMissingChildrenCounter()));

				logger.info("");
				logger.info("");
				logger
						.info("=================================================================");
				logger.info("");
				logger.info("");

				Element derivates = new Element("derivates");
				for (int a = 0; a < journal.getDerivateTypes().size(); a++) {
					Element derivate = new Element("derivate");

					String actualType = journal.getDerivateTypes().get(a);
					long[] values = journal.getDerivateStats().get(actualType);

					String size = Long.toString(values[1]);

					derivate.setAttribute("type", actualType).setAttribute(
							"number", Long.toString(values[0] + 1))
							.setAttribute("size", size);
					derivates.addContent(derivate);
				}
				NumberOfObjects.addContent(Total);
				NumberOfObjects.addContent(Complete);
				NumberOfObjects.addContent(Incomplete);
				NumberOfObjects.addContent(Missing);
				XMLjournal.addContent(NumberOfObjects);
				XMLjournal.addContent(derivates);
				XMLjournal.addContent(XMLobjectsInc);
				XMLjournal.addContent(XMLobjectsMis);
				date.addContent(XMLjournal);
			}
		}

		// save
		logger
				.info("/************************Journal Status Report*************************/");

		String saveFile = saveFolder + "/journalStatistic.xml";
		saveXML(date, saveFolder, saveFile, true);
	}

	private static List<Element> manipulateXML(String targetFile)
			throws JDOMException, IOException {
		boolean empty = true;
		Document doc = new Document();
		List<Element> AllStats = null;
		/*
		 * XMLOutputter xmlOut = new XMLOutputter(); xml =
		 * xmlOut.outputString(AllStats); System.out.println(xml);
		 */

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
			// reading the root element of JDOM doc
			Element root = doc.getRootElement();

			AllStats = root.removeContent();

			int ListSize = AllStats.size();

			Element LastStats = AllStats.get(ListSize - 1);

			List<Element> AllPapers = LastStats.getChildren();

			Iterator<Element> papers = AllPapers.iterator();

			while (papers.hasNext()) {
				Element journal = (Element) papers.next();

				journal.removeChildren("objectList");

			}
		}

		return AllStats;
	}

	private static void saveXML(Element newStats, String dir,
			String targetFile, boolean log) throws IOException,
			FileNotFoundException, JDOMException {
		Document Addition = new Document();
		Element root = new Element("journalStatistic");
		Addition.setRootElement(root);
		List<Element> oldContent = manipulateXML(targetFile);

		XMLOutputter xmlOut = new XMLOutputter();
		// xmlOut.getFormat().getPrettyFormat();
		File directory = new File(dir);
		directory.mkdirs();
		FileOutputStream fos = new FileOutputStream(new File(targetFile));

		root.removeContent();

		if (oldContent != null) {

			Iterator<Element> oldContIt = oldContent.iterator();

			while (oldContIt.hasNext()) {
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

	/**
	 * @param d
	 *            der zu rundende Gleitkommawert.
	 * @param scale
	 *            die Anzahl der Nachkommastellen, falls type = fix, die Anzahl
	 *            der tragenden Stellen - 1, falls type = exp. scale sollte >= 0
	 *            sein (negative Werte werden auf 0 gesetzt).
	 * @param mode
	 *            die Rundungsart: einer der Rundungsarten von BigDecimal, seit
	 *            1.5 in java.math.RoundingMode.
	 * @param type
	 *            ein Element von "enum FormatType {fix, exp}" gibt an, auf
	 *            welche Stellen sich die Rundung beziehen soll. FormatType.exp
	 *            ('Exponential') steht für tragende Stellen, FormatType.fix
	 *            ('Fixkomma') steht für Nachkommastellen.
	 * @return der gerundete Gleitkommawert. Anmerkung: Für die Werte double NaN
	 *         und ±Infinity liefert round den Eingabewert unverändert zurück.
	 */

	enum FormatType {
		fix, exp
	};

	public static double round(double d, int scale, RoundingMode mode,
			FormatType type) {
		if (Double.isNaN(d) || Double.isInfinite(d))
			return d;
		scale = Math.max(scale, 0); // Verhindert negative scale-Werte
		BigDecimal bd = BigDecimal.valueOf(d);
		if (type == FormatType.exp) {
			BigDecimal bc = new BigDecimal(bd.unscaledValue(),
					bd.precision() - 1);
			return ((bc.setScale(scale, mode)).scaleByPowerOfTen(bc.scale()
					- bd.scale())).doubleValue();
		}
		return (bd.setScale(scale, mode)).doubleValue();
	}

	public static void DoIt(String JID) throws IOException, JDOMException {
		logger.info("Go Go Go!");
		logger.info("====================");

		xmltable = MCRXMLTableManager.instance();

		maxArtID = getMaxArtID();
		maxVolID = getMaxVolID();
		maxJouID = getMaxJouID();

		logger.info("Article...");

		if (JID.equals("")) {
			JID = "all";
		}

		ParseObjects("articles", "jparticle_", maxArtID, JID);

		logger.info("Volumes...");

		ParseObjects("volumes", "jpvolume_", maxVolID, JID);

		logger.info("Status...");

		PrintStats();

		logger.info("Creating Activity Index...");
		createCSV();

		logger.info("Ready.");
		logger.info("");
	}

	public static void DoIt() throws IOException, JDOMException {
		logger.info("Go Go Go!");
		logger.info("====================");

		xmltable = MCRXMLTableManager.instance();

		maxArtID = getMaxArtID();
		maxVolID = getMaxVolID();
		maxJouID = getMaxJouID();

		logger.info("Article...");

		ParseObjects("articles", "jparticle_", maxArtID, "all");

		logger.info("Volumes...");

		ParseObjects("volumes", "jpvolume_", maxVolID, "all");

		logger.info("Status...");

		PrintStats();
		logger.info("Creating Activity Index...");
		createCSV();

		logger.info("Ready.");
		logger.info("");
	}

	public static double scale(int number) {
		double scalevalue = java.lang.Math
				.sqrt((double) ((number * (6000 / 997)) + (9250000 / 997)));

		if (scalevalue < 100) {
			scalevalue = 100;
		} else if (scalevalue > 40000) {
			scalevalue = 500;
		}
		return scalevalue;
	}

	private static int getMaxArtID() {
		return xmltable.getNextFreeIdInt("jportal", "jparticle");

	}

	/**
	 * @return the maxVolID
	 */
	private static int getMaxVolID() {
		return xmltable.getNextFreeIdInt("jportal", "jpvolume");
	}

	/**
	 * @return the maxVolID
	 */
	private static int getMaxJouID() {
		return xmltable.getNextFreeIdInt("jportal", "jpjournal");
	}

	public static void createCSV() throws IOException {
		String XMLFile = saveFolder + "/journalStatistic.xml";
		Document doc = new Document();
		HashMap<Integer, String> JournalIDs = new HashMap<Integer, String>();
		boolean first = true;
		int value = 0;
		List allElements = new ArrayList();

		try {
			SAXBuilder sxbuild = new SAXBuilder();
			InputSource is = new InputSource(XMLFile);
			doc = sxbuild.build(is);
		}

		catch (Exception e) {
			logger.info(e);
		}

		Element root = doc.getRootElement();
		Element rootList = (Element) root.clone();
		List statisticTags = root.getChildren();
		List statisticTags2 = rootList.getChildren();

		Iterator<Element> StatisticIt = statisticTags.iterator();
		Iterator<Element> StatisticIt2 = statisticTags2.iterator();

		// count Journals and store IDs
		while (StatisticIt.hasNext()) {
			List journalTags = StatisticIt.next().getChildren();

			Iterator<Element> JournalIt = journalTags.iterator();

			while (JournalIt.hasNext()) {
				String JournalID = JournalIt.next().getAttribute("id")
						.getValue();
				if (!JournalIDs.containsValue(JournalID)) {
					JournalIDs.put(JournalIDs.size(), JournalID);
				}
			}
		}

		// going through the xml and sumarize all activity's
		int JournalCount = JournalIDs.size();

		HashMap<String, Integer> ValueMap = new HashMap<String, Integer>();
		HashMap<String, String> StringOfValues = new HashMap<String, String>();
		HashMap<String, String> JIDChecklist = new HashMap<String, String>();
		String FirstLine = "";
		for (int k = 0; k < JournalCount; k++) {
			String JournalID = JournalIDs.get(k);
			StringOfValues.put(JournalID, JournalID);
		}

		while (StatisticIt2.hasNext()) {
			Element Stats = StatisticIt2.next();
			String date = Stats.getAttribute("date").getValue();

			List journalTags = Stats.getChildren();

			FirstLine = FirstLine + date + "#";

			Iterator<Element> JournalIt = journalTags.iterator();

			value = 0;

			while (JournalIt.hasNext()) {
				Element journal = JournalIt.next();
				String JournalID = journal.getAttribute("id").getValue();
				System.out.println("JID: " + JournalID);
				JIDChecklist.put(JournalID, JournalID);

				value = Integer.valueOf(
						journal.getChild("numberOfObjects").getChild(
								"incomplete").getValue()).intValue();
				value = value
						+ Integer.valueOf(
								journal.getChild("numberOfObjects").getChild(
										"missing").getValue()).intValue();

				List<Element> derivateTags = null;
				boolean hasDerivate = true;

				try {
					derivateTags = journal.getChild("derivates").getChildren();
				} catch (RuntimeException e) {
					// TODO Auto-generated catch block
					hasDerivate = false;
				}
				if (hasDerivate) {
					Iterator<Element> derivateIt = derivateTags.iterator();

					while (derivateIt.hasNext()) {
						value = value
								+ Integer.valueOf(
										derivateIt.next()
												.getAttribute("number")
												.getValue()).intValue();
					}
				}
				if (!first) {
					int valueBefore = ValueMap.get(JournalID);
					System.out.println("value: " + value + "valueBefore: "
							+ valueBefore);
					int activity = valueBefore - value;
					if (activity < 0) {
						activity = activity * (-1);
					}
					StringOfValues.put(JournalID, StringOfValues.get(JournalID)
							+ "#" + Integer.toString(activity));
				}

				ValueMap.put(JournalID, value);
				value = 0;

			}
			if (!first) {
				for (int k = 0; k < JournalCount; k++) {
					String JournalID = JournalIDs.get(k);
					if (!JIDChecklist.containsKey(JournalID)) {
						StringOfValues.put(JournalID, StringOfValues
								.get(JournalID)
								+ "#" + "0");
					}
				}

			}
			// set every value 0
			for (int k = 0; k < JournalCount; k++) {
				String JournalID = JournalIDs.get(k);
				if (!JIDChecklist.containsKey(JournalID)) {
					ValueMap.put(JournalID, 0);
				}
			}
			JIDChecklist.clear();
			first = false;
		}

		// create new file if needed
		File file = new File(saveFolder + "/" + CSVFileName);
		if (!file.exists()) {
			file.createNewFile();
		}

		// new CSVWriter
		CSVWriter writer = new CSVWriter(new FileWriter(saveFolder + "/"
				+ CSVFileName), '#', ' ');

		// create a arrayList from which the CSV will be feeded
		String[] entries = FirstLine.split("#");
		allElements.add(entries);

		for (int i = 0; i < JournalIDs.size(); i++) {
			String actualID = JournalIDs.get(i);
			String[] entries2 = StringOfValues.get(actualID).split("#");
			allElements.add(entries2);
		}
		writer.writeAll(allElements);
		writer.close();

		System.out.println("AI-CSV file created!");
	}
}
