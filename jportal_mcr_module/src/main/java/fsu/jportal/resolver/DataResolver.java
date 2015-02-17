package fsu.jportal.resolver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.mycore.common.config.MCRConfiguration;

/**
 * get the file from .mycore/jportal/data
 * 	getData:{pfad}
 * 
 * bsp .mycore/jportal/data/config/jp-globalmessage.xml 
 *  	getData:config/jpglobalmessage.xml
 */
public class DataResolver implements URIResolver {

    @Override
    public Source resolve(String href, String base) throws TransformerException {
    	String[] uriParts = href.split(":");
    	String mcrDataDir = MCRConfiguration.instance().getString("MCR.datadir");
      java.nio.file.Path confDir = Paths.get(mcrDataDir, uriParts[1]);
    	
    	try {
    		return new StreamSource(getDataFile(confDir));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	return null;
    }
      
    public InputStream getDataFile(java.nio.file.Path href) throws FileNotFoundException {
      if(href != null){
      		InputStream dataFile = null;
					try {
						dataFile = Files.newInputStream(href);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
          return dataFile;
      }
      return null;
    }	
}
