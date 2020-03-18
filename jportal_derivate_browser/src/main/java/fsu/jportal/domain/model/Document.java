package fsu.jportal.domain.model;

/**
 * Created by chi on 09.03.20
 *
 * @author Huu Chi Vu
 */
public class Document {
    String objId;
    int status;

    private Document() {
    }

    public Document(String objId, int status) {
        this.objId = objId;
        this.status = status;
    }

    public String getObjId() {
        return objId;
    }

    public int getStatus() {
        return status;
    }

    private void setObjId(String objId) {
        this.objId = objId;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String info(){
        return objId + ":" + status;
    }
}
