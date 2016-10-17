package fsu.jportal.xml.stream;

import java.util.UUID;

/**
 * Created by chi on 07.10.16.
 * @author Huu Chi Vu
 */
public class DerivateFileInfo {

    private String mimeType;

    private String href;

    private String fileName;

    private String uuid;

    public DerivateFileInfo(String mimeType, String fileName, String href) {
        this.mimeType = mimeType;
        this.fileName = fileName;
        this.href = href;
        this.uuid = UUID.randomUUID()
                        .toString();
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getFileName() {
        return fileName;
    }

    public String getHref() {
        return href;
    }

    public String getUuid() {
        return uuid;
    }
}
