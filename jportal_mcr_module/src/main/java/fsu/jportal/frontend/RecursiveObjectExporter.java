package fsu.jportal.frontend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.utils.MCRCategoryTransformer;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRMetaDerivateLink;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaInterface;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.MCRClassification2Commands;
import org.mycore.frontend.cli.MCRDerivateCommands;
import org.mycore.frontend.cli.MCRObjectCommands;

import fsu.jportal.backend.ImportDerivateObject;
import fsu.jportal.backend.io.ImportSink;
import fsu.jportal.backend.io.ImportSource;

public class RecursiveObjectExporter {
	public static class ExporterSink implements ImportSink {
		private static Logger LOGGER = Logger.getLogger(ExporterSink.class);
		private Path saveTo;
		private List<Link> derivateLinkList = new ArrayList<>();
		
		public ExporterSink(Path dest) {
	    this.saveTo = dest;
		}
		
    private class Link {
      String document;
      String file;

      public Link(String document, String file) {
          this.document = document;
          this.file = file;
      }
    }
		
		@Override
		public void save(Document objXML) {
			MCRObject mcrObject = new MCRObject(objXML);
			String type = mcrObject.getId().getTypeId();
			Path path = saveTo.resolve(type);
			createDir(path);
			MCRObjectCommands.export(mcrObject.getId().toString(), path.toAbsolutePath().toString(), "");
			saveDerivateLink(mcrObject);
		}

		@Override
		public void saveClassification(Document classificationXML) {
			try {
				Path path = saveTo.resolve("classification");
				createDir(path);
				String id = classificationXML.getRootElement().getAttributeValue("ID");
				MCRClassification2Commands.export(id, path.toAbsolutePath().toString(), "");
			} catch (Exception e) {
				LOGGER.error("Couldn't save Classification");
				e.printStackTrace();
			}
		}

		@Override
		public void saveDerivate(ImportDerivateObject deriObj) {
			MCRDerivateCommands.export(deriObj.getDerivateID(), saveTo.resolve(deriObj.getDocumentID().split("_")[1]).toAbsolutePath().toString(), "");
		}

    @Override
    public void saveDerivateLinks() {
        for (Link link : derivateLinkList) {
            try {
            	Path path = saveTo.resolve(link.document.split("_")[1]);
      				createDir(path);
            	MCRDerivateCommands.export(link.file, path.toAbsolutePath().toString(), "");
            } catch (Exception e) {
                LOGGER.error("Error while saving DerivateLinks from " + link.document + " with file " + link.file);
                e.printStackTrace();
            }
        }
    }

    protected void saveDerivateLink(MCRObject obj) {
        MCRMetaElement deriLinks = obj.getMetadata().getMetadataElement("derivateLinks");
        String href = "";
        if (deriLinks != null) {
            for (MCRMetaInterface link : deriLinks) {
            	  href = ((MCRMetaDerivateLink) link).getXLinkHref().split("/")[0];
                derivateLinkList.add(new Link(obj.getId().toString(), href));
            }
        }
        obj.getMetadata().removeMetadataElement("derivateLinks");
    }
    
		public void createDir(Path dest) {
			if(!Files.exists(dest)) {
				try {
					Files.createDirectories(dest);
				} catch (IOException e) {
					LOGGER.error("Unable to create Directory: " + e.getMessage());
				}
			}
		}
		
	}
	
	public static class ExporterSource implements ImportSource {
	  private List<Document> objs;
		
		public ExporterSource(String id) {
			Document objXML = getObj(id);
			getObjs().add(objXML);
		}
		
		@Override
		public List<Document> getObjs() {
	    if (objs == null) {
	      objs = new ArrayList<Document>();
	    }
	    return objs;
		}

		@Override
		public Document getObj(String objID) {
			try {
				return MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(objID));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public Document getClassification(String classID) {
      MCRCategory cl = MCRCategoryDAOFactory.getInstance().getCategory(MCRCategoryID.rootID(classID), -1);
			return MCRCategoryTransformer.getMetaDataDocument(cl, false);
		}

		@Override
		public Document getDerivateFiles(String path) {
			return getObj(path);
		}
		
	}
}