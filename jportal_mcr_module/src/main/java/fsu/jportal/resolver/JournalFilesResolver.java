package fsu.jportal.resolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.mycore.common.config.MCRConfiguration;

public class JournalFilesResolver implements URIResolver {

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        href = href.substring(href.indexOf(":")+1);
        try {
            FileInputStream fis = getJournalFile(href);
            return fis != null ? new StreamSource(fis) : null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public FileInputStream getJournalFile(String href) throws FileNotFoundException {
        String journalFileFolderPath = MCRConfiguration.instance().getString("JournalFileFolder");
        if(journalFileFolderPath != null){
            File journalFile = new File(journalFileFolderPath + File.separator + href);
            if(journalFile.exists()){
                return new FileInputStream(journalFile);
            }
        }
        return null;
    }
}
