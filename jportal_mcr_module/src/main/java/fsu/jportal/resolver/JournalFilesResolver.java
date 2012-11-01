package fsu.jportal.resolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.mycore.common.MCRConfiguration;

public class JournalFilesResolver implements URIResolver {
    @Override
    public Source resolve(String href, String base) throws TransformerException {
        href = href.substring(href.indexOf(":")+1);
        String journalFileFolderPath = MCRConfiguration.instance().getString("JournalFileFolder");
        if(journalFileFolderPath != null){
            File journalFile = new File(journalFileFolderPath + File.separator + href);
            
            if(!journalFile.exists()){
                return null;
            }
            
            try {
                return new StreamSource(new FileInputStream(journalFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}