package fsu.jportal.backend;

/**
 * Created by michel on 07.07.15.
 * @author Michel Büchner
 */
public class ImportFileObject {
    private String path;
    private long size;

    public ImportFileObject (String path, long size) {
        this.path = path;
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }
}
