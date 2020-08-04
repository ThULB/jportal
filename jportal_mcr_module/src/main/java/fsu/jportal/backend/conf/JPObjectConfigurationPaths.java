package fsu.jportal.backend.conf;

import java.nio.file.Path;
import java.util.Optional;

import fsu.jportal.backend.mcr.JPConfig;

public class JPObjectConfigurationPaths {
    public static Optional<Path> getJournalFilesFolder(){
        String journalFileFolderPath = JPConfig.getStringOrThrow("JournalFileFolder");
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

    public static Optional<Path> getObjProperties(String journalId, String propType, String format){
        return getConfPath(journalId)
                .map(p -> p.resolve(propType + "." + format));
    }

}
