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

public class JarResource{
    private FileSystem zipFS;
    private Path path;

    public JarResource(String path) throws IOException {
        init(path);
    }
    
    public void init(String path) throws IOException {
        URL jarURL = JarResource.class.getResource(path);
        String[] splittedURL = jarURL.toString().split("!");
        String jarFile = splittedURL[0];
        String jarFolder = splittedURL[1];
        Map<String, String> env = new HashMap<>();
        zipFS = FileSystems.newFileSystem(URI.create(jarFile), env);
        
        this.path = zipFS.getPath(jarFolder);
    }
    
    public DirectoryStream<Path> listFiles() throws IOException{
        return Files.newDirectoryStream(path);
    }

    public void close() throws IOException {
        zipFS.close();
    }
}