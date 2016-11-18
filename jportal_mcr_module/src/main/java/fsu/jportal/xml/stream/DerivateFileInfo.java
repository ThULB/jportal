package fsu.jportal.xml.stream;

import java.util.UUID;

/**
 * Created by chi on 07.10.16.
 * @author Huu Chi Vu
 */
public class DerivateFileInfo {

    private String url;

    private String contentType;

    private String uri;

    private String fileName;

    private String uuid;

    public DerivateFileInfo(String url, String contentType, String fileName, String uri) {
        this.url = url;
        this.contentType = contentType;
        this.fileName = fileName;
        this.uri = uri;
        this.uuid = UUID.randomUUID().toString();
    }

    public String getContentType() {
        return contentType;
    }

    public String getFileName() {
        return fileName;
    }

    public String getUri() {
        return uri;
    }

    public String getUuid() {
        return uuid;
    }

    public String getUrl() {
        return url;
    }

}
