package fsu.jportal.nio;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class FileTools{
    public static Path getJarPath(String path) throws IOException {
        URL jarURL = FileTools.class.getResource(path);
        String[] splittedURL = jarURL.toString().split("!");
        String jarFile = splittedURL[0];
        String jarFolder = splittedURL[1];
        Map<String, String> env = new HashMap<>();
        FileSystem zipFS = FileSystems.newFileSystem(URI.create(jarFile), env);
        
        return zipFS.getPath(jarFolder);
    }
    
    public static DirectoryStream<Path> listFiles(String path) throws IOException{
        return Files.newDirectoryStream(getJarPath(path));
    }
}