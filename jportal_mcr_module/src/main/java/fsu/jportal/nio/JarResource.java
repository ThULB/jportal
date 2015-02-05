package fsu.jportal.nio;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class JarResource{
    private FileSystem zipFS;
    private Path path;

    public JarResource(String path) throws IOException {
        this(JarResource.class.getResource(path));
    }
    
    public JarResource(URL url) throws IOException {
        init(url);
    }

    public void init(URL jarURL) throws IOException {
        String protocol = jarURL.getProtocol();
        if(protocol.equals("file")){
            try {
                this.path = Paths.get(jarURL.toURI());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
         
            return;
        }
        
        String[] splittedURL = jarURL.toString().split("!");
        String jarFile = splittedURL[0];
        String jarFolder = splittedURL[1];
        Map<String, String> env = new HashMap<>();
        zipFS = FileSystems.newFileSystem(URI.create(jarFile), env);
        
        this.path = zipFS.getPath(jarFolder);
    }
    
    public DirectoryStream<Path> listFiles() throws IOException{
        return listFiles(path);
    }
    
    public DirectoryStream<Path> listFiles(Path path) throws IOException{
        return Files.newDirectoryStream(path);
    }
    
    public Path getPath(){
        return path;
    }

    public void close() throws IOException {
        if(zipFS != null) {
            zipFS.close();
        }
    }
}