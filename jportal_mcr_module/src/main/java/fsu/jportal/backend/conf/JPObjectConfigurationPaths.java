package fsu.jportal.backend.conf;

import org.mycore.common.config.MCRConfiguration;

import java.nio.file.Path;
import java.util.Optional;

public class JPObjectConfigurationPaths {
    public static Optional<Path> getJournalFilesFolder(){
        String journalFileFolderPath = MCRConfiguration.instance().getString("JournalFileFolder");
        return Optional.ofNullable(journalFileFolderPath)
                .map(Path::of);
    }

    public static Optional<Path> getConfPath(String journalId){
        return getJournalFilesFolder()
                .map(p -> p.resolve(journalId).resolve("conf"));
    }

    public static Optional<Path> getObjProperties(String journalId, String type){
        return getConfPath(journalId)
                .map(p -> p.resolve(type + ".properties"));
    }

}
