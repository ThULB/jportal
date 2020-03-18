package fsu.jportal.domain.model;

/**
 * Created by chi on 09.03.20
 *
 * {"moveTo":{"derivId":"jportal_derivate_00000003","path":"/Foo","type":""},
 *  "files":[
 *          {"derivId":"jportal_derivate_00000003","path":"/TG571022.JPG","type":"file"},
 *          ....
 *          ]}
 *
 * @author Huu Chi Vu
 */
public class DerivateFiles {
    private File target;
    private File[] files;

    public File[] getFiles() {
        return files;
    }

    public void setFiles(File[] files) {
        this.files = files;
    }

    public File getTarget() {
        return target;
    }

    public void setTarget(File target) {
        this.target = target;
    }

    public static class File {
        private String derivId;
        private String path;
        private String type;
        private int status;
        private int exists;
        private String lastModifiedTime;
        private long size;
        private String URN;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDerivId() {
            return derivId;
        }

        public void setDerivId(String derivId) {
            this.derivId = derivId;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public void setExists(int i) {
            this.exists = i;
        }

        public int getExists() {
            return exists;
        }

        public String getLastModifiedTime() {
            return lastModifiedTime;
        }

        public void setLastModifiedTime(String lastModifiedTime) {
            this.lastModifiedTime = lastModifiedTime;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public String getURN() {
            return URN;
        }

        public void setURN(String URN) {
            this.URN = URN;
        }
    }
}
