package fsu.jportal.domain.model;

/**
 * Created by chi on 16.03.20
 *
 * @author Huu Chi Vu
 */
public class UploadResponse {
    private String uploadID;
    private String derivateID;
    private String md5;

    private UploadResponse() {
    }

    public UploadResponse(String uploadID) {
        this.uploadID = uploadID;
    }

    public String getUploadID() {
        return uploadID;
    }

    public void setUploadID(String uploadID) {
        this.uploadID = uploadID;
    }

    public String getDerivateID() {
        return derivateID;
    }

    public void setDerivateID(String derivateID) {
        this.derivateID = derivateID;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
